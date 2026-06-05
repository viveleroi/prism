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

package org.prism_mc.prism.paper.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.prism_mc.prism.paper.services.modifications.state.BlockStateChange;

@UtilityClass
public class BlockUtils {

    /**
     * List all *side* block faces.
     */
    private static final BlockFace[] attachmentFacesSides = {
        BlockFace.EAST,
        BlockFace.WEST,
        BlockFace.NORTH,
        BlockFace.SOUTH,
    };

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
     * Rotate a horizontal block face 90 degrees clockwise.
     *
     * @param face The face
     * @return The clockwise face, or the original face if not horizontal
     */
    public static BlockFace clockwise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> face;
        };
    }

    /**
     * Rotate a horizontal block face 90 degrees counter-clockwise.
     *
     * @param face The face
     * @return The counter-clockwise face, or the original face if not horizontal
     */
    public static BlockFace counterClockwise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case WEST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.EAST;
            case EAST -> BlockFace.NORTH;
            default -> face;
        };
    }

    /**
     * Get light level.
     *
     * @param block Block
     * @return int
     */
    public static int getLightLevel(Block block) {
        int light = 0;
        final BlockFace[] blockFaces = new BlockFace[] {
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN,
        };
        for (BlockFace blockFace : blockFaces) {
            light = Math.max(light, block.getRelative(blockFace).getLightLevel());
            if (light >= 15) {
                break;
            }
        }

        return (light * 100) / 15;
    }

    /**
     * Remove blocks matching a list of materials within the bounding box.
     *
     * @param world The world
     * @param boundingBox The bounding box
     * @param materials The materials
     * @return A list of block state changes
     */
    public static List<BlockStateChange> removeBlocksByMaterial(
        World world,
        BoundingBox boundingBox,
        List<Material> materials
    ) {
        List<BlockStateChange> stateChanges = new ArrayList<>();

        int minX = (int) boundingBox.getMinX();
        int minY = (int) boundingBox.getMinY();
        int minZ = (int) boundingBox.getMinZ();
        int maxX = (int) boundingBox.getMaxX();
        int maxY = (int) boundingBox.getMaxY();
        int maxZ = (int) boundingBox.getMaxZ();

        if (minX >= maxX || minY >= maxY || minZ >= maxZ) {
            return stateChanges;
        }

        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = (maxX - 1) >> 4;
        int maxChunkZ = (maxZ - 1) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }

                int chunkXMin = Math.max(minX, chunkX << 4);
                int chunkXMax = Math.min(maxX, (chunkX + 1) << 4);
                int chunkZMin = Math.max(minZ, chunkZ << 4);
                int chunkZMax = Math.min(maxZ, (chunkZ + 1) << 4);

                for (int x = chunkXMin; x < chunkXMax; x++) {
                    for (int y = minY; y < maxY; y++) {
                        for (int z = chunkZMin; z < chunkZMax; z++) {
                            Block block = world.getBlockAt(x, y, z);
                            if (materials.contains(block.getType())) {
                                BlockState oldState = block.getState();
                                block.setType(Material.AIR);
                                BlockState newState = block.getState();
                                stateChanges.add(new BlockStateChange(oldState, newState));
                            }
                        }
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
     * Repair double-chest connection state after (re)placing a chest.
     *
     * <p>A chest's LEFT/RIGHT connection type is contextual rather than intrinsic. When a player
     * builds a double chest, only the second half placed is recorded with its LEFT/RIGHT type; the
     * first half silently flips from SINGLE to its complementary type via a neighbor update, which
     * fires no place event and is therefore stored as SINGLE. Applying that data verbatim yields a
     * half-connected chest (one double-chest half sitting beside a plain single chest).</p>
     *
     * <p>This reconciles both directions, so the result is correct regardless of the order the two
     * halves are restored in:</p>
     *
     * <ul>
     *   <li>A half carrying an explicit LEFT/RIGHT type stamps its partner block (if already a
     *   matching chest) to the complementary type.</li>
     *   <li>A SINGLE half adopts the complementary type when an adjacent half already points back
     *   at it, which is what fixes the never-recorded first half.</li>
     * </ul>
     *
     * @param block The chest block just placed
     * @param chest The chest block data applied to that block
     * @param applyPhysics Whether to apply neighbor physics
     */
    public static void reconcileChestConnection(Block block, Chest chest, boolean applyPhysics) {
        BlockFace partnerFace = chestPartnerFace(chest);

        // This half already knows which side its partner sits on; stamp that partner to match.
        if (partnerFace != null) {
            Block partner = block.getRelative(partnerFace);
            if (
                partner.getType() == block.getType() &&
                partner.getBlockData() instanceof Chest partnerChest &&
                partnerChest.getFacing() == chest.getFacing()
            ) {
                Chest.Type expected = complementaryChestType(chest.getType());
                if (partnerChest.getType() != expected) {
                    partnerChest.setType(expected);
                    partner.setBlockData(partnerChest, applyPhysics);
                }
            }

            return;
        }

        // This half is SINGLE; adopt a connection only if an adjacent half claims this block.
        for (BlockFace face : attachmentFacesSides) {
            Block neighbor = block.getRelative(face);
            if (
                neighbor.getType() == block.getType() &&
                neighbor.getBlockData() instanceof Chest neighborChest &&
                neighborChest.getFacing() == chest.getFacing() &&
                face.getOppositeFace().equals(chestPartnerFace(neighborChest))
            ) {
                chest.setType(complementaryChestType(neighborChest.getType()));
                block.setBlockData(chest, applyPhysics);

                return;
            }
        }
    }

    /**
     * Downgrade a double-chest's surviving half to SINGLE after removing the other half.
     *
     * <p>When one half of a double chest is removed, Minecraft normally flips the surviving half
     * back to SINGLE via a neighbor update. That update is suppressed when physics is disabled, so
     * the surviving half would otherwise be left rendering as a connected double-chest half beside
     * an empty space. We perform that downgrade explicitly so removals are correct regardless of
     * whether neighbor physics is applied.</p>
     *
     * @param block The block the chest was just removed from
     * @param removedChest The chest block data that was removed
     * @param applyPhysics Whether to apply neighbor physics
     */
    public static void downgradeChestPartner(Block block, Chest removedChest, boolean applyPhysics) {
        BlockFace partnerFace = chestPartnerFace(removedChest);
        if (partnerFace == null) {
            return;
        }

        Block partner = block.getRelative(partnerFace);
        if (
            partner.getType() == removedChest.getMaterial() &&
            partner.getBlockData() instanceof Chest partnerChest &&
            partnerChest.getFacing() == removedChest.getFacing() &&
            partnerChest.getType() != Chest.Type.SINGLE
        ) {
            partnerChest.setType(Chest.Type.SINGLE);
            partner.setBlockData(partnerChest, applyPhysics);
        }
    }

    /**
     * Reconcile the upper half of a double-height bisected block (doors, tall plants and flowers)
     * with the lower half being written at {@code lowerBlock}.
     *
     * @param lowerBlock The lower-half block being written
     * @param writtenData The block data written to the lower half (null/air on removal)
     * @param replacedData The block data previously at the lower half
     * @param applyPhysics Whether to apply neighbor physics
     */
    public static void reconcileBisectedPartner(
        Block lowerBlock,
        @Nullable BlockData writtenData,
        @Nullable BlockData replacedData,
        boolean applyPhysics
    ) {
        Block above = lowerBlock.getRelative(BlockFace.UP);

        if (isDoubleHeightBottom(writtenData)) {
            // Placing the lower half: build the matching upper half.
            above.setType(writtenData.getMaterial(), applyPhysics);
            if (above.getBlockData() instanceof Bisected upper) {
                upper.setHalf(Bisected.Half.TOP);
                above.setBlockData(upper, applyPhysics);
            }
        } else if (isDoubleHeightBottom(replacedData)) {
            // Removing the lower half: clear the orphaned upper half, but only if it is actually
            // the matching top so an unrelated neighbor above is never deleted.
            if (
                above.getType() == replacedData.getMaterial() &&
                above.getBlockData() instanceof Bisected upper &&
                upper.getHalf() == Bisected.Half.TOP
            ) {
                above.setType(Material.AIR, applyPhysics);
            }
        }
    }

    /**
     * Reconcile the head half of a bed with the foot half being written at {@code footBlock}.
     *
     * @param footBlock The foot block being written
     * @param writtenData The block data written to the foot (null/air on removal)
     * @param replacedData The block data previously at the foot
     * @param applyPhysics Whether to apply neighbor physics
     */
    public static void reconcileBedPartner(
        Block footBlock,
        @Nullable BlockData writtenData,
        @Nullable BlockData replacedData,
        boolean applyPhysics
    ) {
        if (writtenData instanceof Bed foot) {
            // The foot's facing points toward the head.
            Block head = footBlock.getRelative(foot.getFacing());
            head.setType(foot.getMaterial(), applyPhysics);
            if (head.getBlockData() instanceof Bed headData) {
                headData.setPart(Bed.Part.HEAD);
                head.setBlockData(headData, applyPhysics);
            }
        } else if (replacedData instanceof Bed foot) {
            Block head = footBlock.getRelative(foot.getFacing());
            if (
                head.getType() == foot.getMaterial() &&
                head.getBlockData() instanceof Bed headData &&
                headData.getPart() == Bed.Part.HEAD
            ) {
                head.setType(Material.AIR, applyPhysics);
            }
        }
    }

    /**
     * Whether the given block data is the bottom half of a double-height bisected block (door, tall
     * plant/flower). Stairs and trapdoors implement {@link Bisected} but are only one block tall.
     *
     * @param data The block data
     * @return True if the data is the bottom half of a double-height bisected block
     */
    private static boolean isDoubleHeightBottom(@Nullable BlockData data) {
        return (
            data instanceof Bisected bisected &&
            !(data instanceof Stairs) &&
            !(data instanceof TrapDoor) &&
            bisected.getHalf() == Bisected.Half.BOTTOM
        );
    }

    /**
     * Resolve the block face pointing toward a chest's partner half.
     *
     * <p>Mirrors Minecraft's own connection rule: a LEFT chest pairs with the block clockwise of
     * its facing direction, a RIGHT chest with the block counter-clockwise of it. SINGLE chests
     * have no partner. If in-game testing shows halves pairing toward the wrong neighbor, the
     * LEFT/RIGHT cases here are the single place to swap.</p>
     *
     * @param chest The chest block data
     * @return The face of the partner half, or null if the chest is single
     */
    @Nullable
    private static BlockFace chestPartnerFace(Chest chest) {
        return switch (chest.getType()) {
            case LEFT -> clockwise(chest.getFacing());
            case RIGHT -> counterClockwise(chest.getFacing());
            default -> null;
        };
    }

    /**
     * Get the complementary half of a double-chest connection type.
     *
     * @param type The chest type
     * @return RIGHT for LEFT, otherwise LEFT
     */
    private static Chest.Type complementaryChestType(Chest.Type type) {
        return type == Chest.Type.LEFT ? Chest.Type.RIGHT : Chest.Type.LEFT;
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
        List<Block> matchAccumulator,
        List<Location> rejectAccumulator,
        Block startBlock
    ) {
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
            // Some detachables are also bisected so only count this if the neighbor is the bottom half
            if (
                !(neighbor.getBlockData() instanceof Bisected bisected) ||
                bisected.getHalf().equals(Bisected.Half.BOTTOM)
            ) {
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
                    if (neighbor.getBlockData() instanceof Directional directional) {
                        // Only record if the detachable, directional block is attached to us
                        if (directional.getFacing().equals(face)) {
                            accumulator.add(neighbor);
                        }
                    } else if (neighbor.getBlockData() instanceof MultipleFacing multipleFacing) {
                        for (var facing : multipleFacing.getFaces()) {
                            if (facing.getOppositeFace().equals(face)) {
                                accumulator.add(neighbor);

                                // Vines can extend down from a side attachment
                                if (neighbor.getType().equals(Material.VINE)) {
                                    bottomDetachables(accumulator, neighbor);
                                }

                                break;
                            }
                        }
                    } else {
                        accumulator.add(neighbor);
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
