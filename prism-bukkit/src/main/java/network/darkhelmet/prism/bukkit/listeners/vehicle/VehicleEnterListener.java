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

package network.darkhelmet.prism.bukkit.listeners.vehicle;

import com.google.inject.Inject;

import network.darkhelmet.prism.bukkit.actions.BukkitEntityAction;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.api.activities.BukkitActivity;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class VehicleEnterListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public VehicleEnterListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService) {
        super(configurationService, expectationService, recordingService);
    }

    /**
     * Listens for vehicle enter events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        Vehicle vehicle = event.getVehicle();

        var actionType = BukkitActionTypeRegistry.VEHICLE_ENTER;
        if (vehicle instanceof Tameable) {
            actionType = BukkitActionTypeRegistry.ENTITY_RIDE;
        }

        // Ignore if this event is disabled
        if ((actionType.equals(BukkitActionTypeRegistry.VEHICLE_ENTER)
                && !configurationService.prismConfig().actions().vehicleEnter()) ||
                (actionType.equals(BukkitActionTypeRegistry.ENTITY_RIDE)
                    && !configurationService.prismConfig().actions().entityRide())) {
            return;
        }

        Entity entity = event.getEntered();

        var action = new BukkitEntityAction(actionType, vehicle);

        var builder = BukkitActivity.builder().action(action).location(vehicle.getLocation());
        if (entity instanceof Player player) {
            builder.player(player);
        } else {
            builder.cause(nameFromCause(entity));
        }

        recordingService.addToQueue(builder.build());
    }
}