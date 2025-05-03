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

package org.prism_mc.prism.bukkit.listeners.block;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.prism_mc.prism.bukkit.listeners.AbstractListener;
import org.prism_mc.prism.bukkit.services.alerts.BukkitAlertService;
import org.prism_mc.prism.bukkit.services.expectations.ExpectationService;
import org.prism_mc.prism.bukkit.services.recording.BukkitRecordingService;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;

public class BlockBreakListener extends AbstractListener implements Listener {

    /**
     * The alert service.
     */
    private final BukkitAlertService alertService;

    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param alertService The alert service
     * @param recordingService The recording service
     */
    @Inject
    public BlockBreakListener(
        ConfigurationService configurationService,
        ExpectationService expectationService,
        BukkitAlertService alertService,
        BukkitRecordingService recordingService
    ) {
        super(configurationService, expectationService, recordingService);
        this.alertService = alertService;
    }

    /**
     * Listens for block break events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        processBlockBreak(event.getBlock(), event.getPlayer());

        alertService.alertBlockBreak(event.getBlock(), event.getPlayer());
    }
}
