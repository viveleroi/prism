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

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;

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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
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

        StateChange<BlockState> stateChange = null;
        if (type().resultType().equals(ActionResultType.REMOVES)) {
            // If the action type removes a block, rollback means we re-set it
            stateChange = setBlock(
                activityContext.worldUuid(), activityContext.coordinate(), blockData, readWriteNbt, owner, mode);
        } else if (type().resultType().equals(ActionResultType.CREATES)) {
            // If the action type creates a block, rollback means we remove it
            stateChange = setBlock(activityContext.worldUuid(),
                activityContext.coordinate(), replacedBlockData, null, owner, mode);
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

        StateChange<BlockState> stateChange = null;
        if (type().resultType().equals(ActionResultType.CREATES)) {
            // If the action type creates a block, restore means we re-set it
            stateChange = setBlock(activityContext.worldUuid(),
                activityContext.coordinate(), blockData, readWriteNbt, owner, mode);
        } else if (type().resultType().equals(ActionResultType.REMOVES)) {
            // If the action type removes a block, restore means we remove it again
            stateChange = setBlock(activityContext.worldUuid(),
                 activityContext.coordinate(), replacedBlockData, null, owner, mode);
        }

        return ModificationResult.builder()
            .activity(activityContext).statusFromMode(mode).stateChange(stateChange).build();
    }

    /**
     * Sets an in-world block to this block data.
     */
    protected StateChange<BlockState> setBlock(
        UUID worldUuid,
        Coordinate coordinate,
        BlockData newBlockData,
        ReadWriteNBT readWriteNbt,
        Object owner,
        ModificationQueueMode mode
    ) {
        World world = Bukkit.getWorld(worldUuid);
        Location loc = new Location(world, coordinate.x(), coordinate.y(), coordinate.z());
        final Block block = loc.getWorld().getBlockAt(loc);

        // Capture existing state for reporting/reversing needs
        final BlockState oldState = block.getState();

        // Send block change or change world
        if (mode.equals(ModificationQueueMode.PLANNING) && owner instanceof Player player) {
            player.sendBlockChange(loc, newBlockData);
        } else if (mode.equals(ModificationQueueMode.COMPLETING)) {
            block.setBlockData(newBlockData, true);
        }

        // Set NBT
        if (mode.equals(ModificationQueueMode.COMPLETING) && readWriteNbt != null) {
            NBT.modify(block.getState(), nbt -> {
                nbt.mergeCompound(readWriteNbt);
            });
        }

        return new BlockStateChange(oldState, block.getState());
    }

    @Override
    public String toString() {
        return String.format("BlockAction{type=%s,material=%s,blockData=%s,replacedMaterial=%s,replacedBlockData=%s}",
            type, material, blockData, replacedMaterial, replacedBlockData);
    }
}
