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

package org.prism_mc.prism.bukkit.listeners.entity;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.prism_mc.prism.bukkit.actions.BukkitItemStackAction;
import org.prism_mc.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import org.prism_mc.prism.bukkit.api.activities.BukkitActivity;
import org.prism_mc.prism.bukkit.listeners.AbstractListener;
import org.prism_mc.prism.bukkit.services.expectations.ExpectationService;
import org.prism_mc.prism.bukkit.services.recording.BukkitRecordingService;
import org.prism_mc.prism.bukkit.utils.ItemUtils;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;

public class EntityPickupItemListener extends AbstractListener implements Listener {

    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public EntityPickupItemListener(
        ConfigurationService configurationService,
        ExpectationService expectationService,
        BukkitRecordingService recordingService
    ) {
        super(configurationService, expectationService, recordingService);
    }

    /**
     * Listens to entity pickup item events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPickupItem(final EntityPickupItemEvent event) {
        var itemStack = event.getItem().getItemStack();

        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().itemPickup() || !ItemUtils.isValidItem(itemStack)) {
            return;
        }

        var action = new BukkitItemStackAction(BukkitActionTypeRegistry.ITEM_PICKUP, itemStack);
        var builder = BukkitActivity.builder()
            .action(action)
            .location(event.getEntity().getLocation())
            .cause(event.getEntity());
        recordingService.addToQueue(builder.build());
    }
}
