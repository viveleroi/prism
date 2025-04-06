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

import network.darkhelmet.prism.actions.MaterialAction;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.filters.FilterBehavior;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;

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
     * @param materialTags The material tags
     * @param permissions The permissions
     * @param worldNames The world names
     */
    public ActivityFilter(
            FilterBehavior behavior,
            List<String> actions,
            List<Tag<Material>> materialTags,
            List<String> permissions,
            List<String> worldNames) {
        this.actions = actions;
        this.behavior = behavior;
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

        if (!actionResult.equals(ConditionResult.NOT_MATCHED)
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
