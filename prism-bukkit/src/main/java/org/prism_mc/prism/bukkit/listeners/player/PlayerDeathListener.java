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

package org.prism_mc.prism.bukkit.listeners.player;

import com.google.inject.Inject;

import org.prism_mc.prism.bukkit.actions.GenericBukkitAction;
import org.prism_mc.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import org.prism_mc.prism.bukkit.api.activities.BukkitActivity;
import org.prism_mc.prism.bukkit.listeners.AbstractListener;
import org.prism_mc.prism.bukkit.services.expectations.ExpectationService;
import org.prism_mc.prism.bukkit.services.recording.BukkitRecordingService;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public PlayerDeathListener(
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
    public void onPlayerDeath(final PlayerDeathEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().playerDeath()) {
            return;
        }

        var action = new GenericBukkitAction(BukkitActionTypeRegistry.PLAYER_DEATH);

        var activity = BukkitActivity.builder()
            .action(action)
            .location(event.getEntity().getLocation())
            .player(event.getEntity())
            .build();

        recordingService.addToQueue(activity);

        // No need to check keepInventory settings because event.getDrops will be empty if it's true
        for (var item : event.getDrops()) {
            recordItemDropActivity(event.getEntity().getLocation(), event.getEntity(), item);
        }
    }
}
