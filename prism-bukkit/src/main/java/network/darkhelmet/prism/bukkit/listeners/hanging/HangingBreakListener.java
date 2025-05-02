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

package network.darkhelmet.prism.bukkit.listeners.hanging;

import com.google.inject.Inject;

import java.util.Optional;

import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

public class HangingBreakListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public HangingBreakListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService) {
        super(configurationService, expectationService, recordingService);
    }

    /**
     * Listens to hanging break events.
     *
     * <p>Hanging items broken directly by a player fall under HangingBreakByEntityEvent.
     * This is merely here to capture indirect causes (physics) for when they detach
     * from a block.</p>
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreakEvent(final HangingBreakEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().hangingBreak()) {
            return;
        }

        final Hanging hanging = event.getEntity();
        if (event.getCause().equals(HangingBreakEvent.RemoveCause.PHYSICS)) {
            // Physics causes. Hopefully find the actual cause through an expectation
            Optional<Object> expectation = expectationService.detachExpectation(hanging);
            if (expectation.isEmpty()) {
                recordHangingBreak(hanging, "physics");
            } else {
                // Queue a recording
                recordHangingBreak(hanging, expectation.get());

                // Remove from cache
                expectationService.metDetachExpectation(hanging);
            }
        }
    }
}
