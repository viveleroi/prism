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

package network.darkhelmet.prism.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

import org.bukkit.Location;
import org.bukkit.TreeSpecies;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.util.BoundingBox;

@UtilityClass
public class EntityUtils {
    /**
     * Gets hanging entities within a given range of a starting location.
     *
     * @param startLoc The start location
     * @param range The range
     * @return A list of all hanging entities
     */
    public static List<Entity> hangingEntities(final Location startLoc, int range) {
        return Arrays.stream(startLoc.getChunk().getEntities()).filter(entity -> {
            if (isHanging(entity.getType()) && startLoc.getWorld().equals(entity.getWorld())) {
                return startLoc.distance(entity.getLocation()) < range;
            }

            return false;
        }).collect(Collectors.toList());
    }

    /**
     * Checks if an entity type is a hanging entity.
     *
     * @param entityType The entity type
     * @return True if entity type is hanging
     */
    private static boolean isHanging(EntityType entityType) {
        return entityType.equals(EntityType.ITEM_FRAME)
            || entityType.equals(EntityType.GLOW_ITEM_FRAME)
            || entityType.equals(EntityType.PAINTING);
    }

    /**
     * Remove drops (items + experience orbs) within a bounding box.
     *
     * @param world The world
     * @param boundingBox The bounding box
     * @return The count of drops removed
     */
    public static int removeDropsInRange(World world, BoundingBox boundingBox) {
        Collection<Entity> nearbyDrops = entitiesInRangeByClass(world, boundingBox, Item.class, ExperienceOrb.class);
        for (Entity e : nearbyDrops) {
            e.remove();
        }

        return nearbyDrops.size();
    }

    /**
     * Find all entities within a bounding box by their class.
     *
     * @param world The world
     * @param boundingBox The bounding box
     * @param entities The entities
     * @return A list of matched entities
     */
    public static List<Entity> entitiesInRangeByClass(World world, BoundingBox boundingBox, Class<?>... entities) {
        return world.getNearbyEntities(boundingBox).stream().filter(
            e -> Arrays.stream(entities).anyMatch(clazz -> clazz.isInstance(e))).toList();
    }

    /**
     * Converts a tree species to a common term descriptor.
     *
     * @param treeSpecies Tree species
     * @return Common term descriptor
     */
    public static String treeSpeciesToDescriptor(TreeSpecies treeSpecies) {
        return switch (treeSpecies) {
            case GENERIC -> "oak";
            case REDWOOD -> "spruce";
            default -> treeSpecies.name().toLowerCase(Locale.ENGLISH).replace("_", " ");
        };
    }
}
