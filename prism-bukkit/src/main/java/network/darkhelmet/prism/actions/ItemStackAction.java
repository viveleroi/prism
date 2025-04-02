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

package network.darkhelmet.prism.actions;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;

import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.IItemAction;
import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.services.modifications.state.ItemStackStateChange;
import network.darkhelmet.prism.utils.LocationUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemStackAction extends MaterialAction implements IItemAction {
    /**
     * The item stack.
     */
    private final ItemStack itemStack;

    /**
     * The nbt container.
     */
    private final NBTContainer nbtContainer;

    /**
     * Construct a new item stack action.
     * @param type The action type
     * @param itemStack The item stack
     */
    public ItemStackAction(IActionType type, ItemStack itemStack) {
        this(type, itemStack, null);

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
    public ItemStackAction(IActionType type, ItemStack itemStack, String descriptor) {
        super(type, itemStack.getType(), descriptor);

        this.itemStack = itemStack;
        this.nbtContainer = NBTItem.convertItemtoNBT(itemStack);
    }

    @Override
    public boolean hasCustomData() {
        return this.nbtContainer != null;
    }

    @Override
    public @Nullable String serializeCustomData() {
        return nbtContainer.toString();
    }

    @Override
    public ModificationResult applyRollback(
            ModificationRuleset modificationRuleset,
            Object owner,
            IActivity activityContext,
            ModificationQueueMode mode) {
        activityContext.player();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(activityContext.player().uuid());

        // The only time we give items back to a player's personal inventory is when they dropped it
        if (type().equals(ActionTypeRegistry.ITEM_DROP) && offlinePlayer.isOnline()) {
            // Give item back to player
            Player player = (Player) offlinePlayer;
            player.getInventory().addItem(itemStack.clone());

            ItemStackStateChange stateChange = new ItemStackStateChange(itemStack.clone(), null);

            return ModificationResult.builder().activity(activityContext).applied().stateChange(stateChange).build();
        } else if (type().resultType().equals(ActionResultType.REMOVES)) {
            Location loc = LocationUtils.worldCoordToLocation(activityContext.location());
            if (loc.getBlock().getState() instanceof InventoryHolder holder) {
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
            IActivity activityContext,
            ModificationQueueMode mode) {
        return ModificationResult.builder().activity(activityContext).build();
    }
}
