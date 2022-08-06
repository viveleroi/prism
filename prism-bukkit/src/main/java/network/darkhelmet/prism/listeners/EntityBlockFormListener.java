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
import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.recording.RecordingService;
import network.darkhelmet.prism.utils.LocationUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;

public class EntityBlockFormListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public EntityBlockFormListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService) {
        super(configurationService, actionFactory, expectationService, recordingService);
    }

    /**
     * Listens for entity block form events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().blockForm()) {
            return;
        }

        // Build the action
        final IAction action = actionFactory.createBlockStateAction(
            ActionTypeRegistry.BLOCK_FORM, event.getBlock().getState());

        WorldCoordinate at = LocationUtils.locToWorldCoordinate(event.getBlock().getLocation());

        // Build the block place by player activity
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
