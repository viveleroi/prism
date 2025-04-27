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

package network.darkhelmet.prism.bukkit.listeners.block;

import com.google.inject.Inject;

import network.darkhelmet.prism.bukkit.actions.BukkitBlockAction;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.api.activities.BukkitActivity;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class BlockIgniteListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public BlockIgniteListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService) {
        super(configurationService, expectationService, recordingService);
    }

    /**
     * Listens for block place events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().blockIgnite()) {
            return;
        }

        if (!event.getBlock().getType().isBurnable()) {
            return;
        }

        // This event can't accurately capture the "clicked" block for flint and steel
        // use, and that's infinitely more helpful than "ignited air". Instead,
        // we'll handle flint and steel use in the player interact listener.
        if (event.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)) {
            return;
        }

        Block affectedBlock = event.getBlock();

        // Fireballs always ignite the block below them. Since this is informational,
        // it's ok to use that as the target.
        if (event.getCause().equals(BlockIgniteEvent.IgniteCause.FIREBALL)) {
            affectedBlock = affectedBlock.getRelative(BlockFace.DOWN);
        }

        var action = new BukkitBlockAction(BukkitActionTypeRegistry.BLOCK_IGNITE, affectedBlock.getState());

        var builder = BukkitActivity.builder().action(action).location(event.getBlock().getLocation());
        if (event.getPlayer() != null) {
            builder.player(event.getPlayer());
        } else if (event.getIgnitingBlock() != null) {
            builder.cause(nameFromCause(event.getIgnitingBlock()));
        } else if (event.getIgnitingEntity() != null) {
            builder.cause(nameFromCause(event.getIgnitingEntity()));
        } else {
            builder.cause(nameFromCause(event.getCause()));
        }

        recordingService.addToQueue(builder.build());
    }
}
