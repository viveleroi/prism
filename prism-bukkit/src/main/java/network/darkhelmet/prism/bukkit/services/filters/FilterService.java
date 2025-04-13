/*
 * prism
 *
 * Copyright (c) 2022 M Botsko (viveleroi)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package network.darkhelmet.prism.bukkit.services.filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.filters.FilterBehavior;
import network.darkhelmet.prism.api.services.filters.IFilterService;
import network.darkhelmet.prism.bukkit.utils.CustomTag;
import network.darkhelmet.prism.bukkit.utils.ListUtils;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.configuration.filters.FilterConditionsConfiguration;
import network.darkhelmet.prism.loader.services.configuration.filters.FilterConfiguartion;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

@Singleton
public class FilterService implements IFilterService {
    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * Cache all "IGNORE" filters.
     */
    private final List<ActivityFilter> ignoreFilters = new ArrayList<>();

    /**
     * Cache all "ALLOW" filters.
     */
    private final List<ActivityFilter> allowFilters = new ArrayList<>();

    /**
     * Construct a new filter service.
     *
     * @param loggingService The logging service
     * @param configurationService The configuration service
     */
    @Inject
    public FilterService(LoggingService loggingService, ConfigurationService configurationService) {
        this.loggingService = loggingService;
        this.configurationService = configurationService;

        loadFilters();
    }

    /**
     * Load all filters from the config.
     */
    public void loadFilters() {
        ignoreFilters.clear();
        allowFilters.clear();

        // Convert all configured filters into Filter objects
        for (FilterConfiguartion config : configurationService.prismConfig().filters()) {
            loadFilter(config.name().isEmpty() ? "Unnamed" : config.name(), config.behavior(), config.conditions());
        }
    }

    /**
     * Load all filters from the config.
     */
    public void loadFilter(String filterName, FilterBehavior behavior, FilterConditionsConfiguration config) {
        // Behavior
        if (behavior == null) {
            loggingService.logger()
                .warn("Filter error: No behavior defined in filter {}. Behavior must be either IGNORE or ALLOW.",
                    filterName);

            return;
        }

        boolean conditionExists = false;

        // Worlds
        // Note: Worlds may not be loaded here and users type world names so we'll
        // just rely on the name for comparison. No need for UUIDs otherwise we'd need
        // to monitor world load/unload events.
        // Unfortunately that also means we can't error when an invalid world is configured.
        List<String> worldNames = config.worlds();

        if (!ListUtils.isNullOrEmpty(worldNames)
                || !ListUtils.isNullOrEmpty(config.permissions()) || !ListUtils.isNullOrEmpty(config.actions())) {
            conditionExists = true;
        }

        var entityTypeTags = new ArrayList<Tag<EntityType>>();

        // Entity Types
        if (!ListUtils.isNullOrEmpty(config.entityTypes())) {
            var entityTag = new CustomTag<>(EntityType.class);
            for (String entityTypeKey : config.entityTypes()) {
                try {
                    EntityType entityType = EntityType.valueOf(entityTypeKey.toUpperCase(Locale.ENGLISH));
                    entityTag.append(entityType);
                } catch (IllegalArgumentException e) {
                    loggingService.logger().warn(
                        "Filter error in {}: No entity type matching {}", filterName, entityTypeKey);
                }
            }

            conditionExists = true;
            entityTypeTags.add(entityTag);
        }

        // Entity type tags
        if (!ListUtils.isNullOrEmpty(config.entityTypesTags())) {
            for (String entityTypeTag : config.entityTypesTags()) {
                var namespacedKey = NamespacedKey.fromString(entityTypeTag);
                if (namespacedKey != null) {
                    var tag = Bukkit.getTag("entity_types", namespacedKey, EntityType.class);
                    if (tag != null) {
                        conditionExists = true;
                        entityTypeTags.add(tag);

                        continue;
                    }
                }

                loggingService.logger().warn(
                    "Filter error in {}: Invalid entity type tag {}", filterName, entityTypeTag);
            }
        }

        var materialTags = new ArrayList<Tag<Material>>();

        // Materials
        if (!ListUtils.isNullOrEmpty(config.materials())) {
            CustomTag<Material> materialTag = new CustomTag<>(Material.class);
            for (String materialKey : config.materials()) {
                try {
                    Material material = Material.valueOf(materialKey.toUpperCase(Locale.ENGLISH));
                    materialTag.append(material);
                } catch (IllegalArgumentException e) {
                    loggingService.logger().warn(
                        "Filter error in {}: No material matching {}", filterName, materialKey);
                }
            }

            conditionExists = true;
            materialTags.add(materialTag);
        }

        // Block material tags
        if (!ListUtils.isNullOrEmpty(config.blockTags())) {
            for (String blockTag : config.blockTags()) {
                var namespacedKey = NamespacedKey.fromString(blockTag);
                if (namespacedKey != null) {
                    var tag = Bukkit.getTag("blocks", namespacedKey, Material.class);
                    if (tag != null) {
                        conditionExists = true;
                        materialTags.add(tag);

                        continue;
                    }
                }

                loggingService.logger().warn("Filter error in {}: Invalid block tag {}", filterName, blockTag);
            }
        }

        // Item material tags
        if (!ListUtils.isNullOrEmpty(config.itemTags())) {
            for (String itemTag : config.itemTags()) {
                var namespacedKey = NamespacedKey.fromString(itemTag);
                if (namespacedKey != null) {
                    var tag = Bukkit.getTag("items", namespacedKey, Material.class);

                    if (tag != null) {
                        conditionExists = true;
                        materialTags.add(tag);

                        continue;
                    }
                }

                loggingService.logger().warn("Filter error in {}: Invalid item tag {}", filterName, itemTag);
            }
        }

        if (conditionExists) {
            var filter = new ActivityFilter(
                filterName,
                behavior,
                ListUtils.isNullOrEmpty(config.actions()) ? new ArrayList<>() : config.actions(),
                ListUtils.isNullOrEmpty(config.causes()) ? new ArrayList<>() : config.causes(),
                ListUtils.isNullOrEmpty(entityTypeTags) ? new ArrayList<>() : entityTypeTags,
                ListUtils.isNullOrEmpty(materialTags) ? new ArrayList<>() : materialTags,
                ListUtils.isNullOrEmpty(config.permissions()) ? new ArrayList<>() : config.permissions(),
                ListUtils.isNullOrEmpty(worldNames) ? new ArrayList<>() : worldNames
            );

            if (behavior.equals(FilterBehavior.ALLOW)) {
                allowFilters.add(filter);
            } else {
                ignoreFilters.add(filter);
            }
        } else {
            loggingService.logger().warn("Filter error in {}: Not enough conditions", filterName);
        }
    }

    /**
     * Pass an activity through filters. If any disallow it, reject.
     *
     * @param activity The activity
     * @return True if filters rejected the activity
     */
    public boolean shouldRecord(IActivity activity) {
        // If ANY "IGNORE" filter rejects this activity, disallow recording and stop looking
        for (ActivityFilter filter : ignoreFilters) {
            if (!filter.shouldRecord(activity, loggingService, configurationService.prismConfig().debugFilters())) {
                return false;
            }
        }

        // If ANY "ALLOW" filter rejects this activity, we have to keep looking to ensure no others do
        for (ActivityFilter filter : allowFilters) {
            if (filter.shouldRecord(activity, loggingService, configurationService.prismConfig().debugFilters())) {
                return true;
            }
        }

        // If "ALLOW" filters exist, we have to deny this by default, otherwise we can allow.
        return allowFilters.isEmpty();
    }
}
