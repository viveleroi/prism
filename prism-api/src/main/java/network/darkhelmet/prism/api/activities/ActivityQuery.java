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

package network.darkhelmet.prism.api.activities;

import java.util.Collection;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.Tolerate;

import network.darkhelmet.prism.api.actions.types.ActionType;
import network.darkhelmet.prism.api.util.Coordinate;

@Builder(toBuilder = true)
@Getter
@ToString
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
    private Collection<ActionType> actionTypes;

    /**
     * The activity id.
     */
    private Integer activityId;

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
     * The default parameters used.
     */
    @Singular("defaultUsed")
    private Collection<String> defaultsUsed;

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
     * The coordinate.
     */
    private Coordinate coordinate;

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
     * The reference coordinate.
     * If defined, this location will be used as the center for the Radius, In, and World parameters.
     */
    private Coordinate referenceCoordinate;

    /**
     * The reversed state.
     */
    private Boolean reversed;

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

    /**
     * Get whether this query is for a modifier.
     *
     * @return True if lookup and grouped are false
     */
    public boolean modification() {
        return !lookup && !grouped;
    }

    public static class ActivityQueryBuilder {
        /**
         * Set the coordinate corners of a bounding box.
         *
         * @param minCoordinate The min coordinate
         * @param maxCoordinate The max coordinate
         * @return The builder
         */
        public ActivityQueryBuilder boundingCoordinates(Coordinate minCoordinate, Coordinate maxCoordinate) {
            this.minCoordinate = minCoordinate;
            this.maxCoordinate = maxCoordinate;
            return this;
        }

        /**
         * Set the coordinate.
         *
         * @param x The x coordinate
         * @param y The y coordinate
         * @param z The z coordinate
         * @return The builder
         */
        @Tolerate
        public ActivityQueryBuilder coordinate(double x, double y, double z) {
            this.coordinate = new Coordinate(x, y, z);
            return this;
        }

        /**
         * Use the reference coordinate as the search location.
         *
         * @return The builder
         */
        public ActivityQueryBuilder coordinateFromReferenceCoordinate() {
            this.coordinate = referenceCoordinate;

            return this;
        }

        /**
         * Indicate this query is for use with modifiers.
         *
         * <p>Sets lookup and grouped to false.</p>
         *
         * @return The builder
         */
        public ActivityQueryBuilder modification() {
            this.lookup(false);
            this.grouped(false);

            return this;
        }

        /**
         * Set the radius around the current reference coordinate.
         *
         * @param radius The radius
         * @return The builder
         */
        public ActivityQueryBuilder radius(int radius) {
            Coordinate minCoordinate = new Coordinate(
                referenceCoordinate.intX() - radius,
                referenceCoordinate.intY() - radius,
                referenceCoordinate.intZ() - radius);

            Coordinate maxCoordinate = new Coordinate(
                referenceCoordinate.intX() + radius,
                referenceCoordinate.intY() + radius,
                referenceCoordinate.intZ() + radius);

            this.boundingCoordinates(minCoordinate, maxCoordinate);

            return this;
        }

        /**
         * Indicate this query is for use with a restore modifier.
         *
         * <p>Sets lookup and grouped to false,
         * sort direction to ascending,
         * reversed boolean to true.</p>
         *
         * @return The builder
         */
        public ActivityQueryBuilder restore() {
            this.lookup(false);
            this.grouped(false);
            this.sort(Sort.ASCENDING);
            this.reversed(true);

            return this;
        }

        /**
         * Indicate this query is for use with modifiers.
         *
         * <p>Sets lookup and grouped to false,
         * sort direction to descending,
         * reversed boolean to false.</p>
         *
         * @return The builder
         */
        public ActivityQueryBuilder rollback() {
            this.lookup(false);
            this.grouped(false);
            this.sort(Sort.DESCENDING);
            this.reversed(false);

            return this;
        }
    }
}
