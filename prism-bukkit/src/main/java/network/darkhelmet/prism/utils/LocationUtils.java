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

package network.darkhelmet.prism.utils;

import lombok.experimental.UtilityClass;

import network.darkhelmet.prism.api.util.Coordinate;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

@UtilityClass
public class LocationUtils {
    /**
     * Returns the minimum coordinate for the chunk.
     *
     * @param chunk The chunk
     * @return The minimum coordinate
     */
    public static Coordinate getChunkMinCoordinate(Chunk chunk) {
        int blockMinX = chunk.getX() * 16;
        int blockMinZ = chunk.getZ() * 16;
        return new Coordinate(blockMinX, 0, blockMinZ);
    }

    /**
     * Returns the maximum coordinate for the chunk.
     *
     * @param chunk The chunk
     * @return The maximum coordinate
     */
    public static Coordinate getChunkMaxCoordinate(Chunk chunk) {
        int blockMinX = chunk.getX() * 16;
        int blockMinZ = chunk.getZ() * 16;
        int blockMaxX = blockMinX + 15;
        int blockMaxZ = blockMinZ + 15;
        return new Coordinate(blockMaxX, chunk.getWorld().getMaxHeight(), blockMaxZ);
    }

    /**
     * Get a max coordinate from a location/radius. This is the max corner of a bounding box.
     *
     * @param location The center location
     * @param radius The radius (in blocks)
     * @return The coordinate
     */
    public static Coordinate getMaxCoordinate(Location location, int radius) {
        return new Coordinate(location.getX() + radius, location.getY() + radius, location.getZ() + radius);
    }

    /**
     * Get a min coordinate from a location/radius. This is the min corner of a bounding box.
     *
     * @param location The center location
     * @param radius The radius (in blocks)
     * @return The coordinate
     */
    public static Coordinate getMinCoordinate(Location location, int radius) {
        return new Coordinate(location.getX() - radius, location.getY() - radius, location.getZ() - radius);
    }

    /**
     * Get a world coordinate from a location.
     *
     * @param location The location
     * @return The world coordinate
     */
    public static WorldCoordinate locToWorldCoordinate(Location location) {
        World world = location.getWorld();
        NamedIdentity worldIdentity = new NamedIdentity(world.getUID(), world.getName());
        return new WorldCoordinate(worldIdentity, location.getX(), location.getY(), location.getZ());
    }

    /**
     * Get a location from a world coordinate.
     *
     * @param coordinate The coordinate
     * @return The location
     */
    public static Location worldCoordToLocation(WorldCoordinate coordinate) {
        World world = Bukkit.getWorld(coordinate.world().uuid());
        return new Location(world, coordinate.x(), coordinate.y(), coordinate.z());
    }
}
