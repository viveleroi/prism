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

package network.darkhelmet.prism.bukkit.listeners.player;

import com.google.inject.Inject;

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.bukkit.actions.ActionFactory;
import network.darkhelmet.prism.bukkit.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.RecordingService;
import network.darkhelmet.prism.bukkit.utils.LocationUtils;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class PlayerBucketFillListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public PlayerBucketFillListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService) {
        super(configurationService, actionFactory, expectationService, recordingService);
    }

    /**
     * Listens to player bucket fill events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        // Ignore if this event is disabled
        if (event.getItemStack() == null || !configurationService.prismConfig().actions().bucketFill()) {
            return;
        }

        Player player = event.getPlayer();
        WorldCoordinate at = LocationUtils.locToWorldCoordinate(event.getBlock().getLocation());

        // Build the action
        final IAction bucketEmptyAction = actionFactory.createItemStackAction(
            ActionTypeRegistry.BUCKET_FILL, event.getItemStack());

        // Build the activity
        final ISingleActivity bucketEmptyActivity = Activity.builder()
            .action(bucketEmptyAction)
            .location(at)
            .player(player.getUniqueId(), player.getName())
            .build();

        recordingService.addToQueue(bucketEmptyActivity);

        // No block data
        if (event.getBlock().getType().equals(Material.AIR)) {
            return;
        }

        BlockData blockData = event.getBlock().getBlockData();
        if (event.getBlockClicked().getBlockData() instanceof Waterlogged waterlogged) {
            blockData = waterlogged;

            // Fake the waterlogged block now being dry
            waterlogged.setWaterlogged(false);
        }

        // Build the block break action
        final IAction blockPlaceAction = actionFactory.createBlockDataAction(
            ActionTypeRegistry.BLOCK_BREAK, blockData, null);

        // Build the bucket fill activity
        final ISingleActivity blockPlaceActivity = Activity.builder()
            .action(blockPlaceAction)
            .location(at)
            .player(player.getUniqueId(), player.getName())
            .build();

        recordingService.addToQueue(blockPlaceActivity);
    }
}
