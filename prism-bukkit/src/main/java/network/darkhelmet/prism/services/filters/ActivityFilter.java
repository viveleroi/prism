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

import org.bukkit.Material;
import org.bukkit.Tag;

public class ActivityFilter {
    /**
     * The behavior of this filter.
     */
    private FilterBehavior behavior;

    /**
     * All world UUIDs.
     */
    private final List<String> worldNames;

    /**
     * Actions.
     */
    private final List<String> actions;

    /**
     * The material tag.
     */
    private final List<Tag<Material>> materialTags;

    /**
     * Construct a new activity filter.
     *
     * @param behavior The behavior
     * @param worldNames The world names
     * @param actions The actions
     * @param materialTags The material tags
     */
    public ActivityFilter(
            FilterBehavior behavior,
            List<String> worldNames,
            List<String> actions,
            List<Tag<Material>> materialTags) {
        this.behavior = behavior;
        this.worldNames = worldNames;
        this.actions = actions;
        this.materialTags = materialTags;
    }

    /**
     * Check if this filter allows the activity.
     *
     * @param activity The activity
     * @return True if the filter allows it
     */
    public boolean allows(IActivity activity) {
        boolean actionMatched = actionsMatch(activity);
        boolean worldMatched = worldsMatch(activity);
        boolean materialMatched = materialsMatched(activity);

        // If this filter exists we're guaranteed to require matches.
        // The filter can be either "ALLOW" or "IGNORE" but not both.
        // If any of the criteria are empty, they automatically match.
        // If any criteria were set, we compare against the activity for a match.
        if (allowing()) {
            // If ALLOW mode, all filters must match to approve this
            return worldMatched && actionMatched && materialMatched;
        } else {
            // If IGNORE mode, we *reject* this if all filters match
            return !(worldMatched && actionMatched && materialMatched);
        }
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
     * Check if any worlds match the activity.
     *
     * <p>If none listed, the filter will match all.</p>
     *
     * @param activity The activity
     * @return True if world name matched
     */
    private boolean worldsMatch(IActivity activity) {
        return worldNames.isEmpty() || worldNames.contains(activity.location().world().name());
    }

    /**
     * Check if any actions match the activity action.
     *
     * <p>If none listed, the filter will match all.</p>
     *
     * @param activity The activity
     * @return True if action key matches
     */
    private boolean actionsMatch(IActivity activity) {
        return actions.isEmpty() || actions.contains(activity.action().type().key());
    }

    /**
     * Check if any materials match the activity action.
     *
     * <p>If none listed, the filter will match all. Ignores non-material actions.</p>
     *
     * @param activity The activity
     * @return True if action material matches
     */
    private boolean materialsMatched(IActivity activity) {
        if (materialTags.isEmpty()) {
            return true;
        }

        if (activity.action() instanceof MaterialAction materialAction) {
            for (Tag<Material> materialTag : materialTags) {
                System.out.println("comparing tag " + materialTag.getKey() + " to " + materialAction.material());
                if (materialTag.isTagged(materialAction.material())) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }
}
