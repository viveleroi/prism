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

package org.prism_mc.prism.paper.services.filters;

import java.util.Locale;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.prism_mc.prism.api.activities.Activity;
import org.prism_mc.prism.api.containers.StringContainer;
import org.prism_mc.prism.api.services.filters.FilterBehavior;
import org.prism_mc.prism.api.util.Coordinate;
import org.prism_mc.prism.loader.services.logging.LoggingService;
import org.prism_mc.prism.paper.actions.PaperBlockAction;
import org.prism_mc.prism.paper.actions.PaperEntityAction;
import org.prism_mc.prism.paper.actions.PaperMaterialAction;
import org.prism_mc.prism.paper.actions.PaperPlayerAction;
import org.prism_mc.prism.paper.api.containers.PaperBlockContainer;
import org.prism_mc.prism.paper.api.containers.PaperEntityContainer;
import org.prism_mc.prism.paper.api.containers.PaperPlayerContainer;
import org.prism_mc.prism.paper.utils.CustomTag;

@Builder
public class ActivityFilter {

    /**
     * The filter name.
     */
    private final String name;

    /**
     * Actions.
     */
    private final Set<String> actions;

    /**
     * Actions to exclude.
     */
    private final Set<String> actionsExcluded;

    /**
     * The behavior of this filter.
     */
    @Getter
    private final FilterBehavior behavior;

    /**
     * Causes.
     */
    private final Set<String> namedCauses;

    /**
     * Causes to exclude.
     */
    private final Set<String> namedCausesExcluded;

    /**
     * The cause container types.
     */
    private final Set<CauseContainerType> causeTypes;

    /**
     * The cause container types to exclude.
     */
    private final Set<CauseContainerType> causeTypesExcluded;

    /**
     * The affected block tags.
     */
    private final CustomTag<Material> affectedBlockTags;

    /**
     * The affected block tags to exclude.
     */
    private final CustomTag<Material> affectedBlockExcludedTags;

    /**
     * The cause block tags.
     */
    private final CustomTag<Material> causeBlockTags;

    /**
     * The cause block tags to exclude.
     */
    private final CustomTag<Material> causeBlockExcludedTags;

    /**
     * The affected entity type tag (entity types, entity types tags).
     */
    private final CustomTag<EntityType> affectedEntityTypeTags;

    /**
     * The affected entity type tags to exclude.
     */
    private final CustomTag<EntityType> affectedEntityTypeExcludedTags;

    /**
     * The cause entity type tag (entity types, entity types tags).
     */
    private final CustomTag<EntityType> causeEntityTypeTags;

    /**
     * The cause entity type tags to exclude.
     */
    private final CustomTag<EntityType> causeEntityTypeExcludedTags;

    /**
     * The player's game mode(s).
     */
    private final Set<GameMode> gameModes;

    /**
     * The item tags.
     */
    private final CustomTag<Material> itemTags;

    /**
     * The item tags to exclude.
     */
    private final CustomTag<Material> itemExcludedTags;

    /**
     * All permissions.
     */
    private final Set<String> permissions;

    /**
     * All world names.
     */
    private final Set<String> worldNames;

    /**
     * World names to exclude.
     */
    private final Set<String> worldNamesExcluded;

    /**
     * The cause player names.
     */
    private final Set<String> causePlayerNames;

    /**
     * The cause player names to exclude.
     */
    private final Set<String> causePlayerNamesExcluded;

    /**
     * The affected player names.
     */
    private final Set<String> affectedPlayerNames;

    /**
     * The affected player names to exclude.
     */
    private final Set<String> affectedPlayerNamesExcluded;

    /**
     * The minimum Y coordinate an activity must be at or above.
     */
    private final Integer above;

    /**
     * The maximum Y coordinate an activity must be at or below.
     */
    private final Integer below;

    /**
     * The minimum corner of the bounding box an activity must fall within.
     */
    private final Coordinate boundsMin;

    /**
     * The maximum corner of the bounding box an activity must fall within.
     */
    private final Coordinate boundsMax;

    /**
     * The center of the radius an activity must fall within.
     */
    private final Coordinate radiusCenter;

    /**
     * The radius, in blocks, around the center an activity must fall within.
     */
    private final Integer radiusDistance;

    /**
     * Check if every configured condition of this filter matches the activity.
     *
     * <p>This is behavior-independent; the caller applies ALLOW/IGNORE semantics to the
     * result.</p>
     *
     * @param activity The activity
     * @param loggingService The logging service
     * @param debug Whether filters are in debug mode
     * @return True if all configured conditions matched
     */
    public boolean matches(Activity activity, LoggingService loggingService, boolean debug) {
        if (debug) {
            loggingService.debug("Filter ({0}) Check for Activity: {1}", name, activity);
            loggingService.debug("Behavior: {0}", behavior);
        }

        int total = 0;
        int matched = 0;
        int notMatched = 0;

        var actionResult = actionsMatch(activity);
        total++;
        if (actionResult == ConditionResult.MATCHED) {
            matched++;
        } else if (actionResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!actions.isEmpty() || !actionsExcluded.isEmpty())) {
            loggingService.debug("Action result: {0}", actionResult);
        }

        var namedCauseResult = namedCausesMatch(activity);
        total++;
        if (namedCauseResult == ConditionResult.MATCHED) {
            matched++;
        } else if (namedCauseResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!namedCauses.isEmpty() || !namedCausesExcluded.isEmpty())) {
            loggingService.debug("Named Cause result: {0}", namedCauseResult);
        }

        var causeTypeResult = causeTypesMatch(activity);
        total++;
        if (causeTypeResult == ConditionResult.MATCHED) {
            matched++;
        } else if (causeTypeResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!causeTypes.isEmpty() || !causeTypesExcluded.isEmpty())) {
            loggingService.debug("Cause Type result: {0}", causeTypeResult);
        }

        var affectedBlockResult = affectedBlocksMatched(activity);
        total++;
        if (affectedBlockResult == ConditionResult.MATCHED) {
            matched++;
        } else if (affectedBlockResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!affectedBlockTags.isEmpty() || !affectedBlockExcludedTags.isEmpty())) {
            loggingService.debug("Affected Blocks result: {0}", affectedBlockResult);
        }

        var causeBlockResult = causeBlocksMatched(activity);
        total++;
        if (causeBlockResult == ConditionResult.MATCHED) {
            matched++;
        } else if (causeBlockResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!causeBlockTags.isEmpty() || !causeBlockExcludedTags.isEmpty())) {
            loggingService.debug("Cause Blocks result: {0}", causeBlockResult);
        }

        var affectedEntityTypeResult = affectedEntityTypesMatched(activity);
        total++;
        if (affectedEntityTypeResult == ConditionResult.MATCHED) {
            matched++;
        } else if (affectedEntityTypeResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!affectedEntityTypeTags.isEmpty() || !affectedEntityTypeExcludedTags.isEmpty())) {
            loggingService.debug("Affected Entity Type result: {0}", affectedEntityTypeResult);
        }

        var causeEntityTypeResult = causeEntityTypesMatched(activity);
        total++;
        if (causeEntityTypeResult == ConditionResult.MATCHED) {
            matched++;
        } else if (causeEntityTypeResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!causeEntityTypeTags.isEmpty() || !causeEntityTypeExcludedTags.isEmpty())) {
            loggingService.debug("Cause Entity Type result: {0}", causeEntityTypeResult);
        }

        var gameModeResult = gameModesMatched(activity);
        total++;
        if (gameModeResult == ConditionResult.MATCHED) {
            matched++;
        } else if (gameModeResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && !gameModes.isEmpty()) {
            loggingService.debug("Game mode result: {0}", gameModeResult);
        }

        var itemsResult = itemsMatched(activity);
        total++;
        if (itemsResult == ConditionResult.MATCHED) {
            matched++;
        } else if (itemsResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!itemTags.isEmpty() || !itemExcludedTags.isEmpty())) {
            loggingService.debug("Items result: {0}", itemsResult);
        }

        var permissionResult = permissionsMatch(activity);
        total++;
        if (permissionResult == ConditionResult.MATCHED) {
            matched++;
        } else if (permissionResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && !permissions.isEmpty()) {
            loggingService.debug("Permission result: {0}", permissionResult);
        }

        var worldsResult = worldsMatch(activity);
        total++;
        if (worldsResult == ConditionResult.MATCHED) {
            matched++;
        } else if (worldsResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!worldNames.isEmpty() || !worldNamesExcluded.isEmpty())) {
            loggingService.debug("Worlds result: {0}", worldsResult);
        }

        var causePlayerNameResult = causePlayerNamesMatch(activity);
        total++;
        if (causePlayerNameResult == ConditionResult.MATCHED) {
            matched++;
        } else if (causePlayerNameResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!causePlayerNames.isEmpty() || !causePlayerNamesExcluded.isEmpty())) {
            loggingService.debug("Cause Player Name result: {0}", causePlayerNameResult);
        }

        var affectedPlayerNameResult = affectedPlayerNamesMatch(activity);
        total++;
        if (affectedPlayerNameResult == ConditionResult.MATCHED) {
            matched++;
        } else if (affectedPlayerNameResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (!affectedPlayerNames.isEmpty() || !affectedPlayerNamesExcluded.isEmpty())) {
            loggingService.debug("Affected Player Name result: {0}", affectedPlayerNameResult);
        }

        var yBoundsResult = yBoundsMatch(activity);
        total++;
        if (yBoundsResult == ConditionResult.MATCHED) {
            matched++;
        } else if (yBoundsResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && (above != null || below != null)) {
            loggingService.debug("Y Bounds result: {0}", yBoundsResult);
        }

        var boundsResult = boundsMatch(activity);
        total++;
        if (boundsResult == ConditionResult.MATCHED) {
            matched++;
        } else if (boundsResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && boundsMin != null) {
            loggingService.debug("Bounds result: {0}", boundsResult);
        }

        var radiusResult = radiusMatch(activity);
        total++;
        if (radiusResult == ConditionResult.MATCHED) {
            matched++;
        } else if (radiusResult == ConditionResult.NOT_MATCHED) {
            notMatched++;
        }
        if (debug && radiusCenter != null) {
            loggingService.debug("Radius result: {0}", radiusResult);
        }

        int nonApplicable = total - matched - notMatched;

        // The filter matches only when at least one condition applied and none of the
        // applicable conditions failed.
        var allMatched = matched > 0 && notMatched == 0;
        if (debug) {
            loggingService.debug(
                "All: {0}; Not Applicable: {1}; Matched: {2}; Not Matched: {3}; Filter matched: {4}",
                total,
                nonApplicable,
                matched,
                notMatched,
                allMatched
            );
        }

        return allMatched;
    }

    /**
     * Evaluate an include/exclude set pair against a value.
     *
     * <p>The value is not matched if it is excluded, or if includes are listed and it is not
     * among them. Otherwise it matches.</p>
     *
     * @param included The included values
     * @param excluded The excluded values
     * @param value The value
     * @return ConditionResult
     */
    private ConditionResult setMatch(Set<String> included, Set<String> excluded, String value) {
        if (!excluded.isEmpty() && excluded.contains(value)) {
            return ConditionResult.NOT_MATCHED;
        }

        if (!included.isEmpty() && !included.contains(value)) {
            return ConditionResult.NOT_MATCHED;
        }

        return ConditionResult.MATCHED;
    }

    /**
     * Evaluate an include/exclude tag pair against a value.
     *
     * <p>The value is not matched if it is in the excluded tags, or if included tags are listed
     * and it is not among them. Otherwise it matches.</p>
     *
     * @param included The included tags
     * @param excluded The excluded tags
     * @param value The value
     * @param <T> The tag value type
     * @return ConditionResult
     */
    private <T extends Enum<T> & Keyed> ConditionResult tagMatch(
        CustomTag<T> included,
        CustomTag<T> excluded,
        T value
    ) {
        if (!excluded.isEmpty() && excluded.isTagged(value)) {
            return ConditionResult.NOT_MATCHED;
        }

        if (!included.isEmpty() && !included.isTagged(value)) {
            return ConditionResult.NOT_MATCHED;
        }

        return ConditionResult.MATCHED;
    }

    /**
     * Check if any actions match the activity action.
     *
     * <p>If neither includes nor excludes are listed, this condition is skipped. A value may be
     * a full action key (e.g. {@code block-break}) or a category (e.g. {@code block}), which
     * matches every action key sharing that prefix.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult actionsMatch(Activity activity) {
        if (actions.isEmpty() && actionsExcluded.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        var key = activity.action().type().key();
        var category = actionCategory(key);

        if (!actionsExcluded.isEmpty() && (actionsExcluded.contains(key) || actionsExcluded.contains(category))) {
            return ConditionResult.NOT_MATCHED;
        }

        if (!actions.isEmpty() && !actions.contains(key) && !actions.contains(category)) {
            return ConditionResult.NOT_MATCHED;
        }

        return ConditionResult.MATCHED;
    }

    /**
     * Get the category of an action type key, which is the segment before the first hyphen
     * (e.g. {@code block} for {@code block-break}).
     *
     * @param key The action type key
     * @return The category
     */
    private String actionCategory(String key) {
        int index = key.indexOf('-');

        return index > 0 ? key.substring(0, index) : key;
    }

    /**
     * Check if any named causes match the activity action.
     *
     * <p>If none listed, the filter will match all.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult namedCausesMatch(Activity activity) {
        if (namedCauses.isEmpty() && namedCausesExcluded.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.cause().container() instanceof StringContainer stringContainer) {
            return setMatch(namedCauses, namedCausesExcluded, stringContainer.value());
        }

        return ConditionResult.NOT_MATCHED;
    }

    /**
     * Check if the activity's cause matches any of the listed cause types.
     *
     * <p>If none listed, this condition is skipped. {@link CauseContainerType#ENTITY}
     * matches non-player entities only; players are matched by
     * {@link CauseContainerType#PLAYER}.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult causeTypesMatch(Activity activity) {
        if (causeTypes.isEmpty() && causeTypesExcluded.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        var type = causeContainerType(activity.cause().container());
        if (type == null) {
            return ConditionResult.NOT_MATCHED;
        }

        if (!causeTypesExcluded.isEmpty() && causeTypesExcluded.contains(type)) {
            return ConditionResult.NOT_MATCHED;
        }

        if (!causeTypes.isEmpty() && !causeTypes.contains(type)) {
            return ConditionResult.NOT_MATCHED;
        }

        return ConditionResult.MATCHED;
    }

    /**
     * Resolve the cause container category for an activity's cause.
     *
     * @param container The cause container
     * @return The cause container type, or null if it is of an unrecognized kind
     */
    private CauseContainerType causeContainerType(Object container) {
        if (container instanceof PaperPlayerContainer) {
            return CauseContainerType.PLAYER;
        } else if (container instanceof PaperEntityContainer) {
            return CauseContainerType.ENTITY;
        } else if (container instanceof PaperBlockContainer) {
            return CauseContainerType.BLOCK;
        } else if (container instanceof StringContainer) {
            return CauseContainerType.NAMED;
        }

        return null;
    }

    /**
     * Check if any affected blocks match the activity action.
     *
     * <p>If none listed, this condition is skipped. Otherwise an activity that does not match it,
     * including one whose cause or action is of an unrelated type, counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult affectedBlocksMatched(Activity activity) {
        if (affectedBlockTags.isEmpty() && affectedBlockExcludedTags.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.action() instanceof PaperBlockAction blockAction) {
            return tagMatch(
                affectedBlockTags,
                affectedBlockExcludedTags,
                blockAction.blockContainer().blockData().getMaterial()
            );
        } else {
            return ConditionResult.NOT_MATCHED;
        }
    }

    /**
     * Check if any cause blocks match the activity action.
     *
     * <p>If none listed, this condition is skipped. Otherwise an activity that does not match it,
     * including one whose cause or action is of an unrelated type, counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult causeBlocksMatched(Activity activity) {
        if (causeBlockTags.isEmpty() && causeBlockExcludedTags.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.cause().container() instanceof PaperBlockContainer blockContainer) {
            return tagMatch(causeBlockTags, causeBlockExcludedTags, blockContainer.blockData().getMaterial());
        } else {
            return ConditionResult.NOT_MATCHED;
        }
    }

    /**
     * Check if any entity types match the activity action.
     *
     * <p>If none listed, this condition is skipped. Otherwise an activity that does not match it,
     * including one whose cause or action is of an unrelated type, counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult affectedEntityTypesMatched(Activity activity) {
        if (affectedEntityTypeTags.isEmpty() && affectedEntityTypeExcludedTags.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.action() instanceof PaperEntityAction entityAction) {
            return tagMatch(
                affectedEntityTypeTags,
                affectedEntityTypeExcludedTags,
                entityAction.entityContainer().entityType()
            );
        } else {
            return ConditionResult.NOT_MATCHED;
        }
    }

    /**
     * Check if any entity types match the activity action.
     *
     * <p>If none listed, this condition is skipped. Otherwise an activity that does not match it,
     * including one whose cause or action is of an unrelated type, counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult causeEntityTypesMatched(Activity activity) {
        if (causeEntityTypeTags.isEmpty() && causeEntityTypeExcludedTags.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.cause().container() instanceof PaperEntityContainer entityContainer) {
            return tagMatch(causeEntityTypeTags, causeEntityTypeExcludedTags, entityContainer.entityType());
        } else {
            return ConditionResult.NOT_MATCHED;
        }
    }

    /**
     * Check if any game modes match the activity action.
     *
     * <p>If none listed, this condition is skipped. Otherwise an activity that does not match it,
     * including one whose cause is not a player, counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult gameModesMatched(Activity activity) {
        if (gameModes.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.cause().container() instanceof PaperPlayerContainer paperPlayerContainer) {
            if (gameModes.contains(paperPlayerContainer.player().getGameMode())) {
                return ConditionResult.MATCHED;
            }

            return ConditionResult.NOT_MATCHED;
        } else {
            return ConditionResult.NOT_MATCHED;
        }
    }

    /**
     * Check if any materials match the activity action.
     *
     * <p>If none listed, this condition is skipped. Otherwise an activity that does not match it,
     * including one whose cause or action is of an unrelated type, counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult itemsMatched(Activity activity) {
        if (itemTags.isEmpty() && itemExcludedTags.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.action() instanceof PaperMaterialAction materialAction) {
            return tagMatch(itemTags, itemExcludedTags, materialAction.material());
        } else {
            return ConditionResult.NOT_MATCHED;
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
    private ConditionResult permissionsMatch(Activity activity) {
        if (permissions.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.cause().container() instanceof PaperPlayerContainer paperPlayerContainer) {
            var player = paperPlayerContainer.player();
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
    private ConditionResult worldsMatch(Activity activity) {
        if (worldNames.isEmpty() && worldNamesExcluded.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        return setMatch(worldNames, worldNamesExcluded, activity.world().value());
    }

    /**
     * Check if any cause player names match the activity.
     *
     * <p>If none listed, this condition is skipped. An activity whose cause is not a player
     * counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult causePlayerNamesMatch(Activity activity) {
        if (causePlayerNames.isEmpty() && causePlayerNamesExcluded.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.cause().container() instanceof PaperPlayerContainer paperPlayerContainer) {
            return setMatch(
                causePlayerNames,
                causePlayerNamesExcluded,
                paperPlayerContainer.name().toLowerCase(Locale.ENGLISH)
            );
        }

        return ConditionResult.NOT_MATCHED;
    }

    /**
     * Check if any affected player names match the activity.
     *
     * <p>If none listed, this condition is skipped. An activity whose action does not affect
     * a player counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult affectedPlayerNamesMatch(Activity activity) {
        if (affectedPlayerNames.isEmpty() && affectedPlayerNamesExcluded.isEmpty()) {
            return ConditionResult.NOT_APPLICABLE;
        }

        if (activity.action() instanceof PaperPlayerAction playerAction) {
            return setMatch(
                affectedPlayerNames,
                affectedPlayerNamesExcluded,
                playerAction.playerContainer().name().toLowerCase(Locale.ENGLISH)
            );
        }

        return ConditionResult.NOT_MATCHED;
    }

    /**
     * Check if the activity falls within the configured Y bounds.
     *
     * <p>If neither above nor below is set, this condition is skipped. An activity with no
     * coordinate counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult yBoundsMatch(Activity activity) {
        if (above == null && below == null) {
            return ConditionResult.NOT_APPLICABLE;
        }

        var coordinate = activity.coordinate();
        if (coordinate == null) {
            return ConditionResult.NOT_MATCHED;
        }

        if (above != null && coordinate.y() < above) {
            return ConditionResult.NOT_MATCHED;
        }

        if (below != null && coordinate.y() > below) {
            return ConditionResult.NOT_MATCHED;
        }

        return ConditionResult.MATCHED;
    }

    /**
     * Check if the activity falls within the configured bounding box.
     *
     * <p>If no bounds are set, this condition is skipped. An activity with no coordinate
     * counts as not matched.</p>
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult boundsMatch(Activity activity) {
        if (boundsMin == null || boundsMax == null) {
            return ConditionResult.NOT_APPLICABLE;
        }

        var coordinate = activity.coordinate();
        if (coordinate == null) {
            return ConditionResult.NOT_MATCHED;
        }

        if (
            coordinate.x() >= boundsMin.x() &&
            coordinate.x() <= boundsMax.x() &&
            coordinate.y() >= boundsMin.y() &&
            coordinate.y() <= boundsMax.y() &&
            coordinate.z() >= boundsMin.z() &&
            coordinate.z() <= boundsMax.z()
        ) {
            return ConditionResult.MATCHED;
        }

        return ConditionResult.NOT_MATCHED;
    }

    /**
     * Check if the activity falls within the configured radius of a center coordinate.
     *
     * @param activity The activity
     * @return ConditionResult
     */
    private ConditionResult radiusMatch(Activity activity) {
        if (radiusCenter == null || radiusDistance == null) {
            return ConditionResult.NOT_APPLICABLE;
        }

        var coordinate = activity.coordinate();
        if (coordinate == null) {
            return ConditionResult.NOT_MATCHED;
        }

        if (
            Math.abs(coordinate.x() - radiusCenter.x()) <= radiusDistance &&
            Math.abs(coordinate.y() - radiusCenter.y()) <= radiusDistance &&
            Math.abs(coordinate.z() - radiusCenter.z()) <= radiusDistance
        ) {
            return ConditionResult.MATCHED;
        }

        return ConditionResult.NOT_MATCHED;
    }
}
