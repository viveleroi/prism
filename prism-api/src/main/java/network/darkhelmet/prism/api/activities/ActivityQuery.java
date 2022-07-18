/*
 * Prism (Refracted)
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

package network.darkhelmet.prism.api.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.util.Coordinate;
import network.darkhelmet.prism.api.util.WorldCoordinate;

@Accessors(chain = true)
@Getter
@Setter
public final class ActivityQuery {
    /**
     * The action types.
     */
    private Collection<IActionType> actionTypes = new ArrayList<>();

    /**
     * The lower-bound timestamp.
     */
    private Long after;

    /**
     * The upper-bound timestamp.
     */
    private Long before;

    /**
     * The cause.
     */
    private String cause;

    /**
     * The entity types.
     */
    private Collection<String> entityTypes = new ArrayList<>();

    /**
     * Grouped.
     */
    private boolean grouped = true;

    /**
     * Limit the number of records.
     */
    private int limit;

    /**
     * The location.
     */
    private WorldCoordinate location;

    /**
     * Is lookup.
     */
    private boolean lookup = true;

    /**
     * The materials.
     */
    private Collection<String> materials = new ArrayList<>();

    /**
     * The max x coordinate.
     */
    private Coordinate maxCoordinate;

    /**
     * The min z coordinate.
     */
    private Coordinate minCoordinate;

    /**
     * The record index offset.
     */
    private int offset = 0;

    /**
     * The player names.
     */
    private Collection<String> playerNames = new ArrayList<>();

    /**
     * The sort direction.
     */
    private Sort sort = Sort.DESCENDING;

    /**
     * The world uuid.
     */
    private UUID worldUuid;

    /**
     * Describe the sort directions.
     */
    public enum Sort {
        ASCENDING, DESCENDING
    }

    /**
     * Add a single action type.
     *
     * @param actionType The action type
     * @return The query
     */
    public ActivityQuery actionType(IActionType actionType) {
        actionTypes.add(actionType);
        return this;
    }

    /**
     * Set whether this is a lookup. The query results can be grouped
     * and the order by is different.
     *
     * @param lookup If lookup
     * @return The query
     */
    public ActivityQuery lookup(boolean lookup) {
        this.lookup = lookup;

        if (!lookup) {
            grouped(false);
        }

        return this;
    }

    /**
     * Set the coordinate corners of a bounding box.
     *
     * @param minCoordinate The min coordinate
     * @param maxCoordinate The max coordinate
     * @return The query
     */
    public ActivityQuery boundingCoordinates(Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.minCoordinate = minCoordinate;
        this.maxCoordinate = maxCoordinate;
        return this;
    }

    /**
     * Add a player by name.
     *
     * @param playerName The player name
     * @return The query
     */
    public ActivityQuery playerByName(String playerName) {
        this.playerNames.add(playerName);
        return this;
    }
}
