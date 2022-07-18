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

import java.util.Collection;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.Tolerate;

import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.util.Coordinate;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;

@Builder(toBuilder = true)
@Getter
public final class ActivityQuery {
    /**
     * The action type keys.
     */
    @Singular
    private Collection<String> actionTypeKeys;

    /**
     * The action types.
     */
    @Singular
    private Collection<IActionType> actionTypes;

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
    @Singular
    private Collection<String> entityTypes;

    /**
     * Grouped.
     */
    @Builder.Default
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
    @Builder.Default
    private boolean lookup = true;

    /**
     * The materials.
     */
    @Singular
    private Collection<String> materials;

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
    @Builder.Default
    private int offset = 0;

    /**
     * The player names.
     */
    @Singular
    private Collection<String> playerNames;

    /**
     * The sort direction.
     */
    @Builder.Default
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

    public static class ActivityQueryBuilder {
        /**
         * Set the coordinate corners of a bounding box.
         *
         * @param minCoordinate The min coordinate
         * @param maxCoordinate The max coordinate
         * @return The query
         */
        public ActivityQueryBuilder boundingCoordinates(Coordinate minCoordinate, Coordinate maxCoordinate) {
            this.minCoordinate = minCoordinate;
            this.maxCoordinate = maxCoordinate;
            return this;
        }

        /**
         * Set the location.
         *
         * @param worldUuid The world uuid
         * @param worldName The world name
         * @param x The x coordinate
         * @param y The y coordinate
         * @param z The z coordinate
         * @return The builder
         */
        @Tolerate
        public ActivityQueryBuilder location(UUID worldUuid, String worldName, double x, double y, double z) {
            this.location = new WorldCoordinate(new NamedIdentity(worldUuid, worldName), x, y, z);
            return this;
        }

        /**
         * Set whether this is a lookup. The query results can be grouped
         * and the order by is different.
         *
         * @param lookup If lookup
         * @return The query
         */
        public ActivityQueryBuilder lookup(boolean lookup) {
            this.lookup$set = lookup;

            if (!lookup) {
                grouped(false);
            }

            return this;
        }
    }
}
