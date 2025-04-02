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

package network.darkhelmet.prism.listeners.player;

import com.google.inject.Inject;

import java.util.Optional;

import network.darkhelmet.prism.actions.ActionFactory;
import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.services.wands.IWand;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.listeners.AbstractListener;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.recording.RecordingService;
import network.darkhelmet.prism.services.wands.WandService;
import network.darkhelmet.prism.utils.LocationUtils;
import network.darkhelmet.prism.utils.MaterialTag;
import network.darkhelmet.prism.utils.TagLib;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener extends AbstractListener implements Listener {
    /**
     * The wand service.
     */
    private final WandService wandService;

    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     * @param wandService The wand service
     */
    @Inject
    public PlayerInteractListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService,
            WandService wandService) {
        super(configurationService, actionFactory, expectationService, recordingService);
        this.wandService = wandService;
    }

    /**
     * Listen to player interact events (only for internal logic, not monitoring).
     *
     * @param event Tne event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        // Ignore if block is null (can't get location)
        // or if the event is fired for an off-hand click.
        // (Block will be null when clicking air)
        if (block == null || (event.getHand() != null && !event.getHand().equals(EquipmentSlot.HAND))) {
            return;
        }

        // Check if the player has a wand
        Optional<IWand> wand = wandService.getWand(player);
        if (wand.isPresent()) {
            // Left click = block's location
            // Right click = location of block connected to the clicked block face
            Location targetLocation = block.getLocation();
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                targetLocation = block.getRelative(event.getBlockFace()).getLocation();
            }

            // Use the wand
            wand.get().use(LocationUtils.locToWorldCoordinate(targetLocation));

            // Cancel the event
            event.setCancelled(true);
        }
    }

    /**
     * Listen to player interact events (monitoring).
     *
     * @param event Tne event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractMonitor(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        // Ignore if block is null (can't get location)
        // or if the event is fired for an off-hand click.
        // (Block will be null when clicking air)
        if (block == null || (event.getHand() != null && !event.getHand().equals(EquipmentSlot.HAND))) {
            return;
        }

        if (event.useInteractedBlock().equals(Event.Result.DENY) || event.useItemInHand().equals(Event.Result.DENY)) {
            return;
        }

        if (event.getAction().equals(Action.PHYSICAL) && block.getType().equals(Material.FARMLAND)) {
            // Record block break for crop
            Block blockAbove = block.getRelative(BlockFace.UP);
            if (MaterialTag.CROPS.isTagged(blockAbove.getType())) {
                processBlockBreak(blockAbove, event.getPlayer());
            }

            return;
        }

        WorldCoordinate at = LocationUtils.locToWorldCoordinate(event.getClickedBlock().getLocation());

        if (event.getClickedBlock().getState() instanceof Jukebox jukebox) {
            recordJukeboxActivity(jukebox, at, player);
        } else if (event.getClickedBlock().getState() instanceof InventoryHolder) {
            // Ignore if this event is disabled
            if (!configurationService.prismConfig().actions().inventoryOpen()) {
                return;
            }

            // Build the action
            final IAction action = actionFactory
                .createBlockStateAction(ActionTypeRegistry.INVENTORY_OPEN, event.getClickedBlock().getState());

            // Build the activity
            ISingleActivity activity = Activity.builder()
                .action(action)
                .player(player.getUniqueId(), player.getName())
                .location(at)
                .build();

            recordingService.addToQueue(activity);
        } else if (TagLib.USABLE.isTagged(event.getMaterial())) {
            // Ignore if this event is disabled
            if (!configurationService.prismConfig().actions().blockUse()) {
                return;
            }

            // Build the action
            final IAction action = actionFactory
                .createBlockStateAction(ActionTypeRegistry.BLOCK_USE, event.getClickedBlock().getState());

            // Build the activity
            ISingleActivity activity = Activity.builder()
                .action(action)
                .player(player.getUniqueId(), player.getName())
                .location(at)
                .build();

            recordingService.addToQueue(activity);
        }
    }

    /**
     * Helper to record interactions with a jukebox.
     *
     * @param jukebox The jukebox
     * @param at The location
     * @param player The player
     */
    private void recordJukeboxActivity(Jukebox jukebox, WorldCoordinate at, Player player) {
        final IAction action;
        if (jukebox.isPlaying()) {
            // Ignore if this event is disabled
            if (!configurationService.prismConfig().actions().itemRemove()) {
                return;
            }

            action = actionFactory.createItemStackAction(
                ActionTypeRegistry.ITEM_REMOVE, new ItemStack(jukebox.getPlaying()));
        } else {
            // Ignore if this event is disabled
            if (!configurationService.prismConfig().actions().itemInsert()) {
                return;
            }

            action = actionFactory.createItemStackAction(
                ActionTypeRegistry.ITEM_INSERT, player.getInventory().getItemInMainHand());
        }

        // Build the activity
        ISingleActivity activity = Activity.builder()
            .action(action)
            .player(player.getUniqueId(), player.getName())
            .location(at)
            .build();

        recordingService.addToQueue(activity);
    }
}
