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

package network.darkhelmet.prism.bukkit.actions;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;

import java.util.Locale;
import java.util.UUID;

import network.darkhelmet.prism.api.actions.BlockAction;
import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.ActionType;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.api.services.modifications.ModificationSkipReason;
import network.darkhelmet.prism.api.services.modifications.StateChange;
import network.darkhelmet.prism.api.util.Coordinate;
import network.darkhelmet.prism.bukkit.services.modifications.state.BlockStateChange;
import network.darkhelmet.prism.bukkit.utils.TagLib;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BukkitBlockAction extends BukkitMaterialAction implements BlockAction {
    /**
     * The block data.
     */
    private final BlockData blockData;

    /**
     * The read/write nbt.
     */
    private final ReadWriteNBT readWriteNbt;

    /**
     * The replaced material.
     */
    private final Material replacedMaterial;

    /**
     * The replaced block data.
     */
    private final BlockData replacedBlockData;

    /**
     * Construct a block state action.
     *
     * @param type The action type
     * @param blockState The block state
     */
    public BukkitBlockAction(ActionType type, BlockState blockState) {
        this(type, blockState, null);
    }

    /**
     * Construct a block state action.
     *
     * @param type The action type
     * @param blockState The block state
     * @param replacedBlockState The replaced block state
     */
    public BukkitBlockAction(ActionType type, BlockState blockState, @Nullable BlockState replacedBlockState) {
        super(type, blockState.getType());

        // Set new block data
        this.blockData = blockState.getBlockData();
        if (blockState instanceof TileState) {
            readWriteNbt = NBT.createNBTObject();
            NBT.get(blockState, readWriteNbt::mergeCompound);
        } else {
            this.readWriteNbt = null;
        }

        // Set old block data
        if (replacedBlockState != null) {
            this.replacedBlockData = replacedBlockState.getBlockData();
            this.replacedMaterial = replacedBlockState.getType();
        } else {
            this.replacedBlockData = null;
            this.replacedMaterial = Material.AIR;
        }
    }

    /**
     * Construct a block state action.
     *
     * @param type The action type
     * @param material The material
     * @param replacedMaterial The replaced material
     */
    public BukkitBlockAction(ActionType type, Material material, @Nullable Material replacedMaterial) {
        super(type, material);
        this.replacedMaterial = replacedMaterial;
        this.blockData = null;
        this.replacedBlockData = null;
        this.readWriteNbt = null;
    }

    /**
     * Construct a block state action.
     *
     * @param type The action type
     * @param blockData The block data
     * @param replacedBlockData The replaced block data
     */
    public BukkitBlockAction(ActionType type, BlockData blockData, @Nullable BlockData replacedBlockData) {
        super(type, blockData.getMaterial());

        // Set new block data
        this.blockData = blockData;
        this.readWriteNbt = null;

        // Set old block data
        if (replacedBlockData != null) {
            this.replacedBlockData = replacedBlockData;
            this.replacedMaterial = replacedBlockData.getMaterial();
        } else {
            this.replacedBlockData = null;
            this.replacedMaterial = Material.AIR;
        }
    }

    /**
     * Construct a block state action.
     *
     * @param type The action type
     * @param material The material
     * @param blockData The block data
     * @param teData The custom data
     * @param replacedMaterial The replaced material
     * @param replacedBlockData The replaced block data
     */
    public BukkitBlockAction(
            ActionType type,
            Material material,
            BlockData blockData,
            ReadWriteNBT teData,
            Material replacedMaterial,
            BlockData replacedBlockData,
            String descriptor) {
        super(type, material, descriptor);

        this.blockData = blockData;
        this.readWriteNbt = teData;
        this.replacedMaterial = replacedMaterial;
        this.replacedBlockData = replacedBlockData;
    }

    @Override
    public @Nullable String serializeBlockData() {
        return this.blockData != null ? this.blockData.getAsString().replaceAll("^[^\\[]+", "") : "";
    }

    @Override
    public boolean hasCustomData() {
        return this.readWriteNbt != null;
    }

    /**
     * Allow merging in custom NBT data if we need to override something.
     *
     * @param nbtString The nbt string
     */
    public void mergeCompound(String nbtString) {
        if (readWriteNbt != null) {
            readWriteNbt.mergeCompound(NBT.parseNBT(nbtString));
        }
    }

    @Override
    public @Nullable String serializeCustomData() {
        if (this.readWriteNbt != null) {
            return this.readWriteNbt.toString();
        }

        return null;
    }

    @Override
    public @Nullable String serializeReplacedMaterial() {
        if (replacedMaterial == null) {
            return null;
        }

        return replacedMaterial.toString().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public @Nullable String serializeReplacedBlockData() {
        if (replacedBlockData == null) {
            return null;
        }

        return this.replacedBlockData.getAsString().replaceAll("^[^\\[]+", "");
    }

    @Override
    public ModificationResult applyRollback(
            ModificationRuleset modificationRuleset,
            Object owner,
            Activity activityContext,
            ModificationQueueMode mode) {
        // Skip if either material is in the blacklist
        if (modificationRuleset.blockBlacklistContainsAny(material.toString())) {
            return ModificationResult.builder()
                .activity(activityContext).skipReason(ModificationSkipReason.BLACKLISTED).build();
        }

        if (replacedMaterial != null && modificationRuleset.blockBlacklistContainsAny(replacedMaterial.toString())) {
            return ModificationResult.builder()
                .activity(activityContext).skipReason(ModificationSkipReason.BLACKLISTED).build();
        }

        var location = location(activityContext.worldUuid(), activityContext.coordinate());
        var block = location.getWorld().getBlockAt(location);

        StateChange<BlockState> stateChange = null;
        if (type().resultType().equals(ActionResultType.REMOVES)) {
            var canSet = canSet(block, blockData, modificationRuleset, activityContext);
            if (canSet != null) {
                return canSet;
            }

            if (mode.equals(ModificationQueueMode.COMPLETING)) {
                // If rolling back a removal, we need to place the top half of a bisected block
                // This happens first otherwise the block will break again
                if (blockData instanceof Bisected bisected) {
                    setBisectedTop(block, bisected, bisected.getMaterial());
                }
            }

            // If the action type removes a block, rollback means we re-set it
            stateChange = setBlock(
                block,
                location,
                blockData,
                replacedBlockData,
                readWriteNbt,
                owner,
                mode);
        } else if (type().resultType().equals(ActionResultType.CREATES)) {
            var canSet = canSet(block, replacedBlockData, modificationRuleset, activityContext);
            if (canSet != null) {
                return canSet;
            }

            // If the action type creates a block, rollback means we remove it
            stateChange = setBlock(block, location, replacedBlockData, blockData, null, owner, mode);
        }

        return ModificationResult.builder()
            .activity(activityContext).statusFromMode(mode).stateChange(stateChange).build();
    }

    @Override
    public ModificationResult applyRestore(
            ModificationRuleset modificationRuleset,
            Object owner,
            Activity activityContext,
            ModificationQueueMode mode) {
        // Skip if either material is in the blacklist
        if (modificationRuleset.blockBlacklistContainsAny(material.toString())) {
            return ModificationResult.builder()
                .activity(activityContext).skipReason(ModificationSkipReason.BLACKLISTED).build();
        }

        if (replacedMaterial != null && modificationRuleset.blockBlacklistContainsAny(replacedMaterial.toString())) {
            return ModificationResult.builder()
                .activity(activityContext).skipReason(ModificationSkipReason.BLACKLISTED).build();
        }

        var location = location(activityContext.worldUuid(), activityContext.coordinate());
        var block = location.getWorld().getBlockAt(location);

        StateChange<BlockState> stateChange = null;
        if (type().resultType().equals(ActionResultType.CREATES)) {
            var canSet = canSet(block, blockData, modificationRuleset, activityContext);
            if (canSet != null) {
                return canSet;
            }

            if (mode.equals(ModificationQueueMode.COMPLETING)) {
                // If rolling back a removal, we need to place the top half of a bisected block
                // This happens first otherwise the block will break again
                if (blockData instanceof Bisected bisected) {
                    setBisectedTop(block, bisected, bisected.getMaterial());
                }
            }

            // If the action type creates a block, restore means we re-set it
            stateChange = setBlock(block, location, blockData, replacedBlockData, readWriteNbt, owner, mode);
        } else if (type().resultType().equals(ActionResultType.REMOVES)) {
            var canSet = canSet(block, replacedBlockData, modificationRuleset, activityContext);
            if (canSet != null) {
                return canSet;
            }

            // If the action type removes a block, restore means we remove it again
            stateChange = setBlock(block, location, replacedBlockData, blockData, null, owner, mode);
        }

        return ModificationResult.builder()
            .activity(activityContext).statusFromMode(mode).stateChange(stateChange).build();
    }

    /**
     * A convenience method for getting a location.
     *
     * @param worldUuid The world uuid
     * @param coordinate The coordinate
     * @return The location
     */
    protected Location location(UUID worldUuid, Coordinate coordinate) {
        World world = Bukkit.getWorld(worldUuid);
        return new Location(world, coordinate.x(), coordinate.y(), coordinate.z());
    }

    /**
     * Compare the current block to our planned change and determine if a change is warranted.
     *
     * @param block The block
     * @param newBlockData The new block data
     * @param modificationRuleset The modification ruleset
     * @param activityContext The activity context
     * @return True if already set
     */
    protected ModificationResult canSet(
            Block block, BlockData newBlockData, ModificationRuleset modificationRuleset, Activity activityContext) {
        if (!modificationRuleset.overwrite() && (TagLib.REQUIRES_OVERWRITE.isTagged(block.getType())
                || block.getBlockData().matches(newBlockData))) {
            return ModificationResult.builder()
                .activity(activityContext).skipReason(ModificationSkipReason.ALREADY_SET).build();
        }

        return null;
    }

    /**
     * Sets an in-world block to this block data.
     */
    protected StateChange<BlockState> setBlock(
        Block block,
        Location location,
        BlockData newBlockData,
        BlockData oldBlockData,
        ReadWriteNBT readWriteNbt,
        Object owner,
        ModificationQueueMode mode
    ) {
        // Capture existing state for reporting/reversing needs
        final BlockState oldState = block.getState();

        if (mode.equals(ModificationQueueMode.COMPLETING)) {
            // Set the bed head part before applying the root block change
            // otherwise the bed will just re-break.
            if (newBlockData instanceof Bed bed) {
                setBedHead(block, bed);
            } else if (oldBlockData instanceof Bed bed) {
                setBedHead(block, bed);
            }
        }

        // Send block change or change world
        if (mode.equals(ModificationQueueMode.PLANNING) && owner instanceof Player player) {
            player.sendBlockChange(location, newBlockData);
        } else if (mode.equals(ModificationQueueMode.COMPLETING)) {
            block.setBlockData(newBlockData);
        }

        // Set NBT
        if (block.getType() != Material.AIR && mode.equals(ModificationQueueMode.COMPLETING) && readWriteNbt != null) {
            NBT.modify(block.getState(), nbt -> {
                nbt.mergeCompound(readWriteNbt);
            });
        }

        return new BlockStateChange(oldState, block.getState());
    }

    /**
     * Set the HEAD part of a bed.
     *
     * @param block The block being changed
     * @param bed The bed block data
     */
    protected void setBedHead(Block block, Bed bed) {
        // Bed activities will always be the FOOT part
        Block relative = block.getRelative(bed.getFacing());

        if (type().resultType().equals(ActionResultType.CREATES)) {
            relative.setType(Material.AIR);
        } else {
            relative.setType(bed.getMaterial());

            if (bed.clone() instanceof Bed siblingBed) {
                siblingBed.setPart(Bed.Part.HEAD);
                relative.setBlockData(siblingBed);
            }
        }
    }

    /**
     * Set the TOP part of a bisected block.
     *
     * @param block The block being changed
     * @param bisected The bisected block data
     * @param material The material
     */
    protected void setBisectedTop(Block block, Bisected bisected, Material material) {
        // Some bisected blocks don't need help
        if (bisected instanceof Stairs || bisected instanceof TrapDoor) {
            return;
        }

        // Bisected activities will always be the BOTTOM part
        Block relative = block.getRelative(BlockFace.UP);

        relative.setType(material);
        if (relative.getBlockData().clone() instanceof Bisected siblingBisected) {
            siblingBisected.setHalf(Bisected.Half.TOP);
            relative.setBlockData(siblingBisected);
        }
    }

    @Override
    public String toString() {
        return String.format("BlockAction{type=%s,material=%s,blockData=%s,replacedMaterial=%s,replacedBlockData=%s}",
            type, material, blockData, replacedMaterial, replacedBlockData);
    }
}
