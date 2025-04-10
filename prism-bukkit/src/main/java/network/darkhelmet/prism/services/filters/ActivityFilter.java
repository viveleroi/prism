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

import java.util.List;

import network.darkhelmet.prism.actions.EntityAction;
import network.darkhelmet.prism.actions.MaterialAction;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.filters.FilterBehavior;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

public class ActivityFilter {
    /**
     * Actions.
     */
    private final List<String> actions;

    /**
     * The behavior of this filter.
     */
    private final FilterBehavior behavior;

    /**
     * Causes.
     */
    private final List<String> causes;

    /**
     * The entity type tags (entity types, entity types tags).
     */
    private final List<Tag<EntityType>> entityTypeTags;

    /**
     * The material tags (materials, block-tags, item-tags).
     */
    private final List<Tag<Material>> materialTags;

    /**
     * All permissions.
     */
    private final List<String> permissions;

    /**
     * All world names.
     */
    private final List<String> worldNames;

    /**
     * Construct a new activity filter.
     *
     * @param behavior The behavior
     * @param actions The actions
     * @param causes The causes
     * @param entityTypeTags The entity type tags
     * @param materialTags The material tags
     * @param permissions The permissions
     * @param worldNames The world names
     */
    public ActivityFilter(
            FilterBehavior behavior,
            List<String> actions,
            List<String> causes,
            List<Tag<EntityType>> entityTypeTags,
            List<Tag<Material>> materialTags,
            List<String> permissions,
            List<String> worldNames) {
        this.actions = actions;
        this.behavior = behavior;
        this.causes = causes;
        this.entityTypeTags = entityTypeTags;
        this.permissions = permissions;
        this.materialTags = materialTags;
        this.worldNames = worldNames;
    }

    /**
     * Check if this filter allows the activity.
     *
     * @param activity The activity
     * @param loggingService The logging service
     * @param debug Whether filters are in debug mode
     * @return True if the filter allows it
     */
    public boolean shouldRecord(IActivity activity, LoggingService loggingService, boolean debug) {
        if (debug) {
            loggingService.debug("Filter Check for Activity: %s", activity);
            loggingService.debug("Behavior: %s", behavior);
        }

        var actionResult = actionsMatch(activity);
        if (debug) {
            loggingService.debug("Action result: %s", actionResult);
        }

        var causeResult = causesMatch(activity);
        if (debug) {
            loggingService.debug("Cause result: %s", causeResult);
        }

        var entityTypeResult = entityTypesMatched(activity);
        if (debug) {
            loggingService.debug("Entity type result: %s", entityTypeResult);
        }

        var materialsResult = materialsMatched(activity);
        if (debug) {
            loggingService.debug("Materials result: %s", materialsResult);
        }

        var permissionResult = permissionsMatch(activity);
        if (debug) {
            loggingService.debug("Permission result: %s", permissionResult);
        }

        var worldsResult = worldsMatch(activity);
        if (debug) {
            loggingService.debug("Worlds result: %s", worldsResult);
        }

        var finalDecision = true;

        // If everything was not applicable, allow this.
        if (actionResult.equals(ConditionResult.NOT_APPLICABLE)
            && causeResult.equals(ConditionResult.NOT_APPLICABLE)
            && entityTypeResult.equals(ConditionResult.NOT_APPLICABLE)
            && materialsResult.equals(ConditionResult.NOT_APPLICABLE)
            && permissionResult.equals(ConditionResult.NOT_APPLICABLE)
            && worldsResult.equals(ConditionResult.NOT_APPLICABLE)) {
            finalDecision = true;
        } else if (!actionResult.equals(ConditionResult.NOT_MATCHED)
            && !causeResult.equals(ConditionResult.NOT_MATCHED)
            && !entityTypeResult.equals(ConditionResult.NOT_MATCHED)
            && !materialsResult.equals(ConditionResult.NOT_MATCHED)
            && !permissionResult.equals(ConditionResult.NOT_MATCHED)
            && !worldsResult.equals(ConditionResult.NOT_MATCHED)) {
            finalDecision = allowing();
        } else {
            finalDecision = ignoring();
        }

        if (debug) {
            loggingService.debug("Final decision: %s", finalDecision);
        }

        return finalDecision;
    }

    /**
     * Check if filter mode is "allow".
     *
     * @return True if mode is "allow"
     */
    private boolean allowing() {
        return behavior.equals(FilterBehavior.ALLOW);
    }

    /**
     * Check if filter mode is "ignore".
     *
     * @return True if mode is "ignore"
     */
    private boolean ignoring() {
        return behavior.equals(FilterBehavior.IGNORE);
    }

    /**
     * Check if any actions match the activity action.
     *
     * <p>If none listed, the filter will match all.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult actionsMatch(IActivity activity) {
        if (actions.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (actions.contains(activity.action().type().key())) {
            return ConditionResult.MATCHED;
        }

        return ConditionResult.NOT_MATCHED;
    }

    /**
     * Check if any causes match the activity action.
     *
     * <p>If none listed, the filter will match all.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult causesMatch(IActivity activity) {
        if (causes.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (causes.contains(activity.cause())) {
            return ConditionResult.MATCHED;
        }

        return ConditionResult.NOT_MATCHED;
    }

    /**
     * Check if any entity types match the activity action.
     *
     * <p>If none listed, the filter will match all. Ignores non-entity actions.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult entityTypesMatched(IActivity activity) {
        if (entityTypeTags.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.action() instanceof EntityAction entityAction) {
            for (Tag<EntityType> entityTypeTag : entityTypeTags) {
                if (entityTypeTag.isTagged(entityAction.entityType())) {
                    return ConditionResult.MATCHED;
                }
            }

            return ConditionResult.NOT_MATCHED;
        } else {
            return ConditionResult.NOT_APPLICABLE;
        }
    }

    /**
     * Check if any materials match the activity action.
     *
     * <p>If none listed, the filter will match all. Ignores non-material actions.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult materialsMatched(IActivity activity) {
        if (materialTags.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.action() instanceof MaterialAction materialAction) {
            for (Tag<Material> materialTag : materialTags) {
                if (materialTag.isTagged(materialAction.material())) {
                    return ConditionResult.MATCHED;
                }
            }

            return ConditionResult.NOT_MATCHED;
        } else {
            return ConditionResult.NOT_APPLICABLE;
        }
    }

    /**
     * Check if any permissions match a player in the activity.
     *
     * <p>If none listed, the filter will match all.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult permissionsMatch(IActivity activity) {
        if (permissions.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        var playerIdentity = activity.player();

        if (playerIdentity != null) {
            var player = Bukkit.getServer().getPlayer(playerIdentity.uuid());
            if (player != null) {
                for (String permission : permissions) {
                    if (player.hasPermission(permission)) {
                        return ConditionResult.MATCHED;
                    }
                }
            }
        }

        return ConditionResult.NOT_MATCHED;
    }

    /**
     * Check if any worlds match the activity.
     *
     * <p>If none listed, the filter will match all.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult worldsMatch(IActivity activity) {
        if (worldNames.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (worldNames.contains(activity.location().world().name())) {
            return ConditionResult.MATCHED;
        }

        return ConditionResult.NOT_MATCHED;
    }
}
