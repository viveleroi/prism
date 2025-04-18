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

package network.darkhelmet.prism.bukkit.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;

import network.darkhelmet.prism.bukkit.services.modifications.state.BlockStateChange;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.util.BoundingBox;

@UtilityClass
public class BlockUtils {
    /**
     * List all *side* block faces.
     */
    private static final BlockFace[] attachmentFacesSides = {
        BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };

    /**
     * Get the block material a bucket material would place/break.
     *
     * @param bucket The bucket material
     * @return The block material
     */
    public static Material blockMaterialFromBucket(Material bucket) {
        if (TagLib.WATER_BUCKETS.isTagged(bucket)) {
            return Material.WATER;
        } else if (TagLib.LAVA_BUCKETS.isTagged(bucket)) {
            return Material.LAVA;
        } else if (bucket.equals(Material.POWDER_SNOW_BUCKET)) {
            return Material.POWDER_SNOW;
        }

        return null;
    }

    /**
     * Remove blocks matching a list of materials.
     *
     * @param world The world
     * @param boundingBox The bounding box
     * @param materials The materials
     * @return A list of block state changes
     */
    public static List<BlockStateChange> removeBlocksByMaterial(
            World world, BoundingBox boundingBox, List<Material> materials) {
        List<BlockStateChange> stateChanges = new ArrayList<>();
        for (int x = (int) boundingBox.getMinX(); x < boundingBox.getMaxX(); x++) {
            for (int y = (int) boundingBox.getMinY(); y < boundingBox.getMaxY(); y++) {
                for (int z = (int) boundingBox.getMinZ(); z < boundingBox.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (materials.contains(block.getType())) {
                        // Capture the old state
                        BlockState oldState = block.getState();

                        // Set to air
                        block.setType(Material.AIR);

                        // Capture the new state
                        BlockState newState = block.getState();

                        stateChanges.add(new BlockStateChange(oldState, newState));
                    }
                }
            }
        }

        return stateChanges;
    }

    /**
     * Gets the "root" block of connected block. If not a
     * double block, the passed block is returned.
     *
     * @param block Block
     */
    public static Block rootBlock(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Bed bed) {
            if (bed.getPart() == Bed.Part.HEAD) {
                return block.getRelative(bed.getFacing().getOppositeFace());
            }
        } else if (data instanceof Bisected bisected && !(data instanceof Stairs) && !(data instanceof TrapDoor)) {
            if (bisected.getHalf() == Bisected.Half.TOP) {
                return block.getRelative(BlockFace.DOWN);
            }
        }

        return block;
    }

    /**
     * Query all gravity-affected blocks on top of a given block.
     *
     * @param accumulator Accumulation list as there may be recursion
     * @param startBlock The start block
     * @return A list of any blocks that are considered "fallers"
     */
    public static List<Block> gravityAffectedBlocksAbove(List<Block> accumulator, Block startBlock) {
        Block neighbor = startBlock.getRelative(BlockFace.UP);
        if (TagLib.GRAVITY_AFFECTED.isTagged(neighbor.getType())) {
            accumulator.add(neighbor);

            // Recurse upwards
            gravityAffectedBlocksAbove(accumulator, neighbor);
        }

        return accumulator;
    }

    /**
     * Query all blocks that can detach from a given start block.
     *
     * @param accumulator Accumulation list as there may be recursion
     * @param startBlock The start block
     * @return A list of any detachable blocks
     */
    public static List<Block> detachables(List<Block> accumulator, Block startBlock) {
        // We can avoid a ton of useless checks by checking for the limited number
        // of materials that can have "recursive detachables on all sides" at all.
        if (TagLib.RECURSIVE_DETACHABLE_HOLDERS.isTagged(startBlock.getType())) {
            allSideDetachables(accumulator, new ArrayList<>(), startBlock);
        }

        sideDetachables(accumulator, startBlock);
        topDetachables(accumulator, startBlock);
        bottomDetachables(accumulator, startBlock);

        return accumulator;
    }

    /**
     * Query all "detachable" blocks on all sides of a given block.
     *
     * <p>This only checks recursive materials because any all-side
     * detachable that is not recursive is checked in side-specific methods.</p>
     *
     * @param matchAccumulator Accumulation list as there may be recursion
     * @param startBlock The start block
     * @return A list of any blocks that are considered "detachable"
     */
    protected static List<Block> allSideDetachables(
            List<Block> matchAccumulator, List<Location> rejectAccumulator, Block startBlock) {
        for (BlockFace face : BlockFace.values()) {
            Block neighbor = startBlock.getRelative(face);

            // Skip visited
            if (matchAccumulator.contains(neighbor) || rejectAccumulator.contains(neighbor.getLocation())) {
                continue;
            }

            if (TagLib.RECURSIVE_DETACHABLES.isTagged(neighbor.getType())) {
                matchAccumulator.add(neighbor);

                // Recurse
                allSideDetachables(matchAccumulator, rejectAccumulator, neighbor);
            } else {
                rejectAccumulator.add(neighbor.getLocation());
            }
        }

        return matchAccumulator;
    }

    /**
     * Query all "detachable" blocks on the bottom of a given block.
     *
     * @param accumulator Accumulation list as there may be recursion
     * @param startBlock The start block
     * @return A list of any blocks that are considered "detachable"
     */
    protected static List<Block> bottomDetachables(List<Block> accumulator, Block startBlock) {
        Block neighbor = startBlock.getRelative(BlockFace.DOWN);
        if (TagLib.BOTTOM_DETACHABLES.isTagged(neighbor.getType())) {
            accumulator.add(neighbor);

            // Recurse downwards
            if (TagLib.RECURSIVE_BOTTOM_DETACHABLES.isTagged(neighbor.getType())) {
                bottomDetachables(accumulator, neighbor);
            }
        }

        return accumulator;
    }

    /**
     * Query all "detachable" blocks on top of a given block.
     *
     * @param accumulator Accumulation list as there may be recursion
     * @param startBlock The start block
     * @return A list of any blocks that are considered "detachable"
     */
    protected static List<Block> topDetachables(List<Block> accumulator, Block startBlock) {
        Block neighbor = startBlock.getRelative(BlockFace.UP);
        if (TagLib.TOP_DETACHABLES.isTagged(neighbor.getType())) {
            // Some detachables are also bisected so if both are, don't count the neighbor
            // because it's really the other half of the bisected block
            if (!(startBlock.getBlockData() instanceof Bisected) && !(neighbor.getBlockData() instanceof Bisected)) {
                accumulator.add(neighbor);
            }

            // Recurse upwards
            if (TagLib.RECURSIVE_TOP_DETACHABLES.isTagged(neighbor.getType())) {
                topDetachables(accumulator, neighbor);
            }
        }

        return accumulator;
    }

    /**
     * Query all "detachable" blocks on the sides of a given block.
     *
     * @param accumulator Accumulation list as there may be recursion
     * @param startBlock The start block
     * @return A list of any blocks that are considered "detachable"
     */
    protected static List<Block> sideDetachables(List<Block> accumulator, Block startBlock) {
        if (startBlock.getType().isSolid()) {
            for (BlockFace face : attachmentFacesSides) {
                Block neighbor = startBlock.getRelative(face);
                if (TagLib.SIDE_DETACHABLES.isTagged(neighbor.getType())) {
                    // Only record if the detachable is attached to us
                    if (neighbor.getBlockData() instanceof Directional directional
                            && directional.getFacing().equals(face)) {
                        accumulator.add(neighbor);

                        // Vines can extend down from a side attachment
                        if (neighbor.getType().equals(Material.VINE)) {
                            bottomDetachables(accumulator, neighbor);
                        }
                    }
                }
            }
        }

        return accumulator;
    }

    /**
     * Get the "harvested" age for a specific block.
     *
     * @param material The material
     * @return The age, or null
     */
    public Integer harvestedAge(Material material) {
        if (material.equals(Material.SWEET_BERRY_BUSH)) {
            return 1;
        }

        return null;
    }
}
