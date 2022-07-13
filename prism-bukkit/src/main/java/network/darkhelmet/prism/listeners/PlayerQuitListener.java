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

package network.darkhelmet.prism.listeners;

import com.google.inject.Inject;

import network.darkhelmet.prism.actions.ActionFactory;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.core.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.recording.RecordingService;
import network.darkhelmet.prism.services.wands.WandService;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener extends AbstractListener implements Listener {
    /**
     * The wand service.
     */
    private final WandService wandService;

    /**
     * The modification queue service.
     */
    private final IModificationQueueService modificationQueueService;

    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     * @param wandService The wand service
     * @param modificationQueueService The modification queue service
     */
    @Inject
    public PlayerQuitListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService,
            WandService wandService,
            IModificationQueueService modificationQueueService) {
        super(configurationService, actionFactory, expectationService, recordingService);

        this.wandService = wandService;
        this.modificationQueueService = modificationQueueService;
    }

    /**
     * On player quit.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        // Cancel any modification queues for this player
        modificationQueueService.cancelQueueForOwner(event.getPlayer());

        // Deactivate any wands
        wandService.deactivateWand(event.getPlayer());
    }
}
