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

import lombok.Getter;

import network.darkhelmet.prism.api.actions.ItemAction;
import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.ActionType;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.services.modifications.state.ItemStackStateChange;
import network.darkhelmet.prism.bukkit.utils.ItemUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BukkitItemStackAction extends BukkitMaterialAction implements ItemAction {
    /**
     * The item stack.
     */
    @Getter
    private final ItemStack itemStack;

    /**
     * The read/write nbt.
     */
    private final ReadWriteNBT readWriteNbt;

    /**
     * Construct a new item stack action.
     *
     * @param type The action type
     * @param itemStack The item stack
     */
    public BukkitItemStackAction(ActionType type, ItemStack itemStack) {
        this(type, itemStack, ItemUtils.getItemStackDescriptor(itemStack));

        // Fix descriptors to include the item stack quantity
        if (itemStack.getAmount() > 1) {
            this.descriptor = itemStack.getAmount() + " " + this.descriptor;
        }
    }

    /**
     * Construct a new item stack action.
     *
     * @param type The action type
     * @param itemStack The item stack
     * @param descriptor The descriptor
     */
    public BukkitItemStackAction(ActionType type, ItemStack itemStack, String descriptor) {
        super(type, itemStack.getType(), descriptor);

        this.itemStack = itemStack;
        this.readWriteNbt = NBT.itemStackToNBT(itemStack);
    }

    @Override
    public boolean hasCustomData() {
        return this.readWriteNbt != null;
    }

    @Override
    public @Nullable String serializeCustomData() {
        return readWriteNbt.toString();
    }

    @Override
    public ModificationResult applyRollback(
            ModificationRuleset modificationRuleset,
            Object owner,
            Activity activityContext,
            ModificationQueueMode mode) {
        activityContext.player();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(activityContext.player().key());

        // The only time we give items back to a player's personal inventory is when they dropped it
        if (type().equals(BukkitActionTypeRegistry.ITEM_DROP) && offlinePlayer.isOnline()) {
            // Give item back to player
            Player player = (Player) offlinePlayer;
            player.getInventory().addItem(itemStack.clone());

            ItemStackStateChange stateChange = new ItemStackStateChange(itemStack.clone(), null);

            return ModificationResult.builder().activity(activityContext).applied().stateChange(stateChange).build();
        } else if (type().resultType().equals(ActionResultType.REMOVES)) {
            var world = Bukkit.getServer().getWorld(activityContext.world().key());
            var location = new Location(world,
                activityContext.coordinate().intX(),
                activityContext.coordinate().intY(),
                activityContext.coordinate().intZ());

            if (location.getBlock().getState() instanceof InventoryHolder holder) {
                holder.getInventory().addItem(itemStack);

                ItemStackStateChange stateChange = new ItemStackStateChange(itemStack.clone(), null);

                return ModificationResult.builder()
                        .activity(activityContext).applied().stateChange(stateChange).build();
            }
        }

        return ModificationResult.builder().activity(activityContext).build();
    }

    @Override
    public ModificationResult applyRestore(
            ModificationRuleset modificationRuleset,
            Object owner,
            Activity activityContext,
            ModificationQueueMode mode) {
        return ModificationResult.builder().activity(activityContext).build();
    }

    @Override
    public String toString() {
        return String.format("ItemStackAction{type=%s,material=%s,itemStack=%s}", type, material, itemStack);
    }
}
