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

package network.darkhelmet.prism.bukkit.listeners.inventory;

import com.google.inject.Inject;

import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.bukkit.utils.ItemUtils;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public InventoryClickListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService) {
        super(configurationService, expectationService, recordingService);
    }

    /**
     * Listens to inventory click events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        // Ignore clicks outside the inventory
        if (event.getClickedInventory() == null) {
            return;
        }

        // Get the unique slot number for the view
        int slot = event.getRawSlot();

        // Anything negative is outside the window
        if (slot < 0) {
            return;
        }

        // Reassign some things for clarity
        ItemStack heldItem = event.getCursor();
        ItemStack slotItem = event.getCurrentItem();
        boolean clickedTopInventory = slot < event.getInventory().getSize();

        // Ignore null slot items. This used to only happen when clicking inventories
        // opened via code. It may not be used anymore, but would never be useful to us.
        if (slotItem == null) {
            return;
        }

        // Who clicks in an inv that isn't a player? Seriously! Just to be safe...
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Get the location of the top inventory
        Location location = event.getInventory().getLocation();

        // If null, it's virtual/transient so we can ignore
        if (location == null) {
            return;
        }

        // Ignore player's working only with their own inventory
        if (event.getInventory().getHolder() instanceof Player other && other.equals(player)) {
            return;
        }

        if (event.getClick().equals(ClickType.LEFT)) {
            if (clickedTopInventory) {
                if (ItemUtils.nullOrAir(heldItem)) {
                    if (!ItemUtils.nullOrAir(slotItem)) {
                        // Taking the full stack out
                        recordItemRemoveActivity(location, player, slotItem, slotItem.getAmount(), slot);
                    }
                } else {
                    int amount = 0;

                    if (ItemUtils.nullOrAir(slotItem) && heldItem.getAmount() <= heldItem.getMaxStackSize()) {
                        // Placing full stack
                        amount = heldItem.getAmount();
                    } else if (slotItem.getType().equals(heldItem.getType())) {
                        // Adding to existing stack
                        amount = Math.min(heldItem.getMaxStackSize() - slotItem.getAmount(), heldItem.getAmount());
                    }

                    if (amount > 0) {
                        // Placing items
                        recordItemInsertActivity(location, player, heldItem, amount, slot);
                    } else if (!ItemUtils.nullOrAir(slotItem) && !slotItem.getType().equals(heldItem.getType())) {
                        // "Exchanging" items
                        recordItemRemoveActivity(location, player, slotItem, slotItem.getAmount(), slot);
                        recordItemInsertActivity(location, player, heldItem, heldItem.getAmount(), slot);
                    }
                }
            }
        } else if (event.getClick().equals(ClickType.RIGHT)) {
            if (clickedTopInventory) {
                if (ItemUtils.nullOrAir(heldItem)) {
                    if (!ItemUtils.nullOrAir(slotItem)) {
                        // Splitting stack in half
                        int amount = (slotItem.getAmount() + 1) / 2;
                        recordItemRemoveActivity(location, player, slotItem, amount, slot);
                    }
                } else {
                    if ((ItemUtils.nullOrAir(slotItem) || slotItem.equals(heldItem))
                            && slotItem.getAmount() < slotItem.getType().getMaxStackSize()) {
                        // Placing a single item
                        recordItemInsertActivity(location, player, slotItem, 1, slot);
                    }
                }
            }
        } else if (event.getClick().equals(ClickType.NUMBER_KEY)) {
            if (clickedTopInventory) {
                if (!ItemUtils.nullOrAir(slotItem)) {
                    recordItemRemoveActivity(location, player, slotItem, slotItem.getAmount(), slot);
                }

                ItemStack swapItem = player.getInventory().getItem(event.getHotbarButton());
                if (!ItemUtils.nullOrAir(swapItem)) {
                    recordItemInsertActivity(location, player, swapItem, swapItem.getAmount(), slot);
                }
            }
        } else if (event.getClick().equals(ClickType.DOUBLE_CLICK)) {
            for (ItemStack slotStack : event.getInventory().getStorageContents()) {
                if (!ItemUtils.nullOrAir(slotStack) && slotStack.isSimilar(heldItem)) {
                    recordItemRemoveActivity(location, player, slotStack, slotStack.getAmount(), slot);
                }
            }
        } else if (event.getClick().equals(ClickType.SHIFT_LEFT) || event.getClick().equals(ClickType.SHIFT_RIGHT)) {
            if (clickedTopInventory) {
                if (!ItemUtils.nullOrAir(slotItem)) {
                    int maxStackSize = slotItem.getType().getMaxStackSize();
                    int remaining = slotItem.getAmount();

                    for (ItemStack slotStack : event.getView().getBottomInventory().getStorageContents()) {
                        if (ItemUtils.nullOrAir(slotStack)) {
                            remaining -= maxStackSize;
                        } else if (slotStack.isSimilar(slotItem)) {
                            remaining -= (maxStackSize - Math.min(slotStack.getAmount(), maxStackSize));
                        }

                        if (remaining <= 0) {
                            remaining = 0;
                            break;
                        }
                    }

                    int amount = slotItem.getAmount() - remaining;
                    recordItemRemoveActivity(location, player, slotItem, amount, slot);
                }
            } else {
                int maxStackSize = slotItem.getType().getMaxStackSize();
                int amountRemaining = slotItem.getAmount();

                ItemStack[] contents = event.getInventory().getStorageContents();

                // Fill item stacks first
                for (ItemStack slotStack : contents) {
                    if (slotItem.isSimilar(slotStack)) {
                        amountRemaining -= Math.min(maxStackSize - slotStack.getAmount(), amountRemaining);

                        if (amountRemaining <= 0) {
                            break;
                        }
                    }
                }

                int firstEmpty = event.getInventory().firstEmpty();
                if (firstEmpty > -1) {
                    amountRemaining -= Math.min(maxStackSize, amountRemaining);
                }

                int amount = slotItem.getAmount() - amountRemaining;
                recordItemInsertActivity(location, player, slotItem, amount, slot);
            }
        } else if (event.getClick().equals(ClickType.DROP)) {
            if (!ItemUtils.nullOrAir(slotItem)) {
                recordItemRemoveActivity(location, player, slotItem, 1, slot);
            }
        } else if (event.getClick().equals(ClickType.CONTROL_DROP)) {
            if (!ItemUtils.nullOrAir(slotItem)) {
                recordItemRemoveActivity(location, player, slotItem, slotItem.getAmount(), slot);
            }
        }
    }
}
