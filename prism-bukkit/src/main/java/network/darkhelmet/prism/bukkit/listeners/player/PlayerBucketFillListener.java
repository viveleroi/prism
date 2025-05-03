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

import network.darkhelmet.prism.bukkit.actions.BukkitBlockAction;
import network.darkhelmet.prism.bukkit.actions.BukkitItemStackAction;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.api.activities.BukkitActivity;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class PlayerBucketFillListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public PlayerBucketFillListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService) {
        super(configurationService, expectationService, recordingService);
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

        var bucketEmptyAction = new BukkitItemStackAction(BukkitActionTypeRegistry.BUCKET_FILL, event.getItemStack());

        var bucketEmptyActivity = BukkitActivity.builder()
            .action(bucketEmptyAction)
            .location(event.getBlock().getLocation())
            .player(event.getPlayer())
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

        var blockPlaceAction = new BukkitBlockAction(
            BukkitActionTypeRegistry.BLOCK_BREAK, blockData, event.getBlock().translationKey(), null);

        var blockPlaceActivity = BukkitActivity.builder()
            .action(blockPlaceAction)
            .location(event.getBlock().getLocation())
            .player(event.getPlayer())
            .build();

        recordingService.addToQueue(blockPlaceActivity);
    }
}
