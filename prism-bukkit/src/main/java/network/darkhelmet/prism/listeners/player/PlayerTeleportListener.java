/*
 * Prism (Refracted)
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

import java.util.Locale;

import network.darkhelmet.prism.actions.ActionFactory;
import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.listeners.AbstractListener;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.recording.RecordingService;
import network.darkhelmet.prism.utils.LocationUtils;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public PlayerTeleportListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService) {
        super(configurationService, actionFactory, expectationService, recordingService);
    }

    /**
     * On player quit.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().playerTeleport()) {
            return;
        }

        Location to = event.getTo();

        String descriptor = "to unknown";
        if (to != null && to.getWorld() != null) {
            descriptor = String.format("%s %d,%d,%d",
                to.getWorld().getName(), to.getBlockX(), to.getBlockY(), to.getBlockZ());
        }

        descriptor += String.format(" (via %s)",
            (event.getCause().name().toLowerCase(Locale.ENGLISH).replace("_", " ")));

        // Build the action
        final IAction action = actionFactory.createAction(ActionTypeRegistry.PLAYER_TELEPORT, descriptor);

        // Build the activity
        final ISingleActivity activity = Activity.builder()
            .action(action)
            .location(LocationUtils.locToWorldCoordinate(event.getFrom()))
            .player(event.getPlayer().getUniqueId(), event.getPlayer().getName())
            .build();

        recordingService.addToQueue(activity);
    }
}
