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

package network.darkhelmet.prism.bukkit.listeners.entity;

import com.google.inject.Inject;

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.bukkit.actions.ActionFactory;
import network.darkhelmet.prism.bukkit.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.RecordingService;
import network.darkhelmet.prism.bukkit.utils.LocationUtils;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupItemListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public EntityPickupItemListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService) {
        super(configurationService, actionFactory, expectationService, recordingService);
    }

    /**
     * Listens to entity pickup item events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPickupItem(final EntityPickupItemEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().itemPickup()) {
            return;
        }

        // Build the action
        final IAction action = actionFactory.createItemStackAction(
            ActionTypeRegistry.ITEM_PICKUP, event.getItem().getItemStack());

        WorldCoordinate at = LocationUtils.locToWorldCoordinate(event.getEntity().getLocation());

        Activity.ActivityBuilder builder = Activity.builder()
            .action(action).location(at);

        if (event.getEntity() instanceof Player player) {
            builder.player(player.getUniqueId(), player.getName());
        } else {
            builder.cause(nameFromCause(event.getEntity()));
        }

        recordingService.addToQueue(builder.build());
    }
}
