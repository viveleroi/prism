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

package network.darkhelmet.prism.listeners.portal;

import com.google.inject.Inject;

import network.darkhelmet.prism.actions.ActionFactory;
import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.listeners.AbstractListener;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.recording.RecordingService;
import network.darkhelmet.prism.utils.LocationUtils;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public PortalCreateListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService) {
        super(configurationService, actionFactory, expectationService, recordingService);
    }

    /**
     * Listens for portal create events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().blockPlace()) {
            return;
        }

        for (BlockState block : event.getBlocks()) {
            WorldCoordinate at = LocationUtils.locToWorldCoordinate(block.getLocation());

            // Build the action
            final IAction action = actionFactory.createBlockStateAction(ActionTypeRegistry.BLOCK_PLACE, block);

            // Build the block place by player activity
            Activity.ActivityBuilder builder = Activity.builder().action(action).location(at);

            if (event.getEntity() instanceof Player player) {
                builder.player(player.getUniqueId(), player.getName()).build();
            } else if (event.getEntity() != null) {
                builder.cause(nameFromCause(event.getEntity()));
            } else {
                builder.cause("nature");
            }

            if (event.getReason().equals(PortalCreateEvent.CreateReason.FIRE)) {
                // Include only the nether portal blocks that were created
                // because the obsidian frame was already present
                if (block.getType().equals(Material.NETHER_PORTAL)) {
                    recordingService.addToQueue(builder.build());
                }
            } else {
                recordingService.addToQueue(builder.build());
            }
        }
    }
}
