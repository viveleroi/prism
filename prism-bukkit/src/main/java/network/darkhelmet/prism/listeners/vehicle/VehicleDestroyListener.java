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

package network.darkhelmet.prism.listeners.vehicle;

import com.google.inject.Inject;

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

import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class VehicleDestroyListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public VehicleDestroyListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService) {
        super(configurationService, actionFactory, expectationService, recordingService);
    }

    /**
     * Listens for vehicle destroy events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().vehicleBreak()) {
            return;
        }

        var location = event.getVehicle().getLocation();
        final IAction action = actionFactory.createEntityAction(ActionTypeRegistry.VEHICLE_BREAK, event.getVehicle());

        // Build the activity
        var builder = Activity.builder();
        builder.action(action).location(LocationUtils.locToWorldCoordinate(location));

        if (event.getAttacker() != null) {
            if (event.getAttacker() instanceof Player player) {
                builder.player(player.getUniqueId(), player.getName());
            } else {
                builder.cause(event.getAttacker().toString());
            }

            ISingleActivity activity = builder.build();
            recordingService.addToQueue(activity);
        } else if (!event.getVehicle().getPassengers().isEmpty()) {
            Entity passenger = event.getVehicle().getPassengers().getFirst();

            if (passenger instanceof Player player) {
                builder.player(player.getUniqueId(), player.getName());
            } else {
                builder.cause(passenger.toString());
            }

            ISingleActivity activity = builder.build();
            recordingService.addToQueue(activity);
        }

        if (event.getVehicle() instanceof ChestBoat chestBoat) {
            recordItemDropFromInventory(
                chestBoat.getInventory(), location, nameFromCause(event.getVehicle().getType()));
        }
    }
}