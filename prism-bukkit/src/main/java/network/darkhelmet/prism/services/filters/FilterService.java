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

package network.darkhelmet.prism.services.filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.filters.IFilterService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.configuration.FilterConfiguartion;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.utils.MaterialTag;

import org.bukkit.Material;

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
     * Cache all filters.
     */
    private final List<ActivityFilter> filters = new ArrayList<>();

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
        filters.clear();

        // Convert all configured filters into Filter objects
        for (FilterConfiguartion config : configurationService.prismConfig().filters()) {
            // Behavior
            if (config.behavior() == null) {
                loggingService.logger()
                    .warn("Filter error: No behavior defined. Behavior must be either IGNORE or ALLOW.");

                continue;
            }

            // Worlds
            // Note: Worlds may not be loaded here and users type world names so we'll
            // just rely on the name for comparison. No need for UUIDs otherwise we'd need
            // to monitor world load/unload events.
            // Unfortunately that also means we can't error when an invalid world is configured.
            List<String> worldNames = config.worlds();

            // Materials
            MaterialTag materialTag = new MaterialTag();
            for (String materialKey : config.materials()) {
                try {
                    Material material = Material.valueOf(materialKey.toUpperCase(Locale.ENGLISH));
                    materialTag.append(material);
                } catch (IllegalArgumentException e) {
                    loggingService.logger().warn("Filter error: No material matching {}", materialKey);
                }
            }

            filters.add(new ActivityFilter(config.behavior(), worldNames, config.actions(), materialTag));
        }
    }

    /**
     * Pass an activity through filters. If any disallow it, reject.
     *
     * @param activity The activity
     * @return True if filters rejected the activity
     */
    public boolean allows(IActivity activity) {
        for (ActivityFilter filter : filters) {
            // If any filter rejects this activity, it's done for.
            if (!filter.allows(activity)) {
                return false;
            }
        }

        return true;
    }
}
