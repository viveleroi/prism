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

import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.util.Coordinate;
import network.darkhelmet.prism.api.util.WorldCoordinate;

public final class ActivityQuery {
    /**
     * The action types.
     */
    private final Collection<IActionType> actionTypes = new ArrayList<>();

    /**
     * The lower-bound timestamp.
     */
    private Long after;

    /**
     * The upper-bound timestamp.
     */
    private Long before;

    /**
     * The entity types.
     */
    private final Collection<String> entityTypes = new ArrayList<>();

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
    private final Collection<String> materials = new ArrayList<>();

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
    private final Collection<String> playerNames = new ArrayList<>();

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
     * Add an action type.
     *
     * @param actionType The action type
     * @return The query
     */
    public ActivityQuery actionType(IActionType actionType) {
        this.actionTypes.add(actionType);
        return this;
    }

    /**
     * Get the action types.
     *
     * @return The action types.
     */
    public Collection<IActionType> actionTypes() {
        return this.actionTypes;
    }

    /**
     * Add action types.
     *
     * @param actionTypes The action types
     * @return The query
     */
    public ActivityQuery actionTypes(Collection<IActionType> actionTypes) {
        this.actionTypes.addAll(actionTypes);
        return this;
    }

    /**
     * Get the lower-bound timestamp.
     *
     * @return The lower-bound timestamp
     */
    public Long after() {
        return after;
    }

    /**
     * Set the lower-bound timestamp.
     *
     * @param after The timestamp
     * @return The query
     */
    public ActivityQuery after(long after) {
        this.after = after;
        return this;
    }

    /**
     * Get the upper-bound timestamp.
     *
     * @return The upper-bound timestamp.
     */
    public Long before() {
        return before;
    }

    /**
     * Set the upper-bound timestamp.
     *
     * @param before The timestamp
     * @return The query
     */
    public ActivityQuery before(long before) {
        this.before = before;
        return this;
    }

    /**
     * Add an entity type.
     *
     * @param entityType The entity type
     * @return The query
     */
    public ActivityQuery entityType(String entityType) {
        this.entityTypes.add(entityType);
        return this;
    }

    /**
     * Get the entity types.
     *
     * @return The entity types
     */
    public Collection<String> entityTypes() {
        return entityTypes;
    }

    /**
     * Add an entity type.
     *
     * @param entityTypes The entity types
     * @return The query
     */
    public ActivityQuery entityTypes(Collection<String> entityTypes) {
        this.entityTypes.addAll(entityTypes);
        return this;
    }

    /**
     * Whether records should be grouped.
     *
     * @return True if grouped
     */
    public boolean grouped() {
        return grouped;
    }

    /**
     * Set whether results should be grouped.
     *
     * @param grouped If grouped
     * @return The query
     */
    public ActivityQuery grouped(boolean grouped) {
        this.grouped = grouped;
        return this;
    }

    /**
     * Get the limit.
     *
     * @return The limit
     */
    public int limit() {
        return limit;
    }

    /**
     * Set the limit.
     *
     * @param limit The limit
     * @return The query
     */
    public ActivityQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Get the location.
     *
     * @return The location
     */
    public WorldCoordinate location() {
        return location;
    }

    /**
     * Set the location.
     *
     * @return The query
     */
    public ActivityQuery location(WorldCoordinate worldCoordinate) {
        this.location = worldCoordinate;
        return this;
    }

    /**
     * Whether query is a lookup.
     *
     * @return True if lookup
     */
    public boolean lookup() {
        return lookup;
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
     * Add a material.
     *
     * @param material The material
     * @return The query
     */
    public ActivityQuery material(String material) {
        this.materials.add(material);
        return this;
    }

    /**
     * Get the materials.
     *
     * @return The materials
     */
    public Collection<String> materials() {
        return materials;
    }

    /**
     * Add materials.
     *
     * @param materials The materials
     * @return The query
     */
    public ActivityQuery materials(Collection<String> materials) {
        this.materials.addAll(materials);
        return this;
    }

    /**
     * Get the max coordinate.
     *
     * @return The max coordinate.
     */
    public Coordinate maxCoordinate() {
        return maxCoordinate;
    }

    /**
     * Set the max coordinate - the max corner of a bounding box.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The query
     */
    public ActivityQuery maxCoordinate(double x, double y, double z) {
        this.maxCoordinate = new Coordinate(x, y, z);
        return this;
    }

    /**
     * Set the max coordinate - the max corner of a bounding box.
     *
     * @param coordinate The max coordinate
     * @return The query
     */
    public ActivityQuery maxCoordinate(Coordinate coordinate) {
        this.maxCoordinate = coordinate;
        return this;
    }

    /**
     * Get the min coordinate.
     *
     * @return The min coordinate.
     */
    public Coordinate minCoordinate() {
        return minCoordinate;
    }

    /**
     * Set the min coordinate - the min corner of a bounding box.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The query
     */
    public ActivityQuery minCoordinate(double x, double y, double z) {
        this.minCoordinate = new Coordinate(x, y, z);
        return this;
    }

    /**
     * Set the min coordinate - the min corner of a bounding box.
     *
     * @param coordinate The min coordinate
     * @return The query
     */
    public ActivityQuery minCoordinate(Coordinate coordinate) {
        this.minCoordinate = coordinate;
        return this;
    }

    /**
     * Get the offset.
     *
     * @return The offset
     */
    public int offset() {
        return offset;
    }

    /**
     * Set the offset.
     *
     * @param offset The offset
     * @return The query
     */
    public ActivityQuery offset(int offset) {
        this.offset = offset;
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

    /**
     * Get the player names.
     *
     * @return The player names
     */
    public Collection<String> playerNames() {
        return playerNames;
    }

    /**
     * Add a list of player names.
     *
     * @param playerNames The player names.
     * @return The query
     */
    public ActivityQuery playerNames(Collection<String> playerNames) {
        this.playerNames.addAll(playerNames);
        return this;
    }

    /**
     * Get the sort.
     *
     * @return The sort
     */
    public Sort sort() {
        return sort;
    }

    /**
     * Set the sort direction. Defaults to descending.
     *
     * @param sort The sort direction.
     * @return The query
     */
    public ActivityQuery sort(Sort sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Get the world uuid.
     *
     * @return The world uuid
     */
    public UUID worldUuid() {
        return worldUuid;
    }

    /**
     * Set the world by uuid.
     *
     * @param worldUuid The world uuid
     * @return The query
     */
    public ActivityQuery worldUuid(UUID worldUuid) {
        this.worldUuid = worldUuid;
        return this;
    }
}
