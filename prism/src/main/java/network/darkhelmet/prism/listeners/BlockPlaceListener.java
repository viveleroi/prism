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

import network.darkhelmet.prism.actions.ActionRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.actions.IActionRegistry;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.filters.FilterService;
import network.darkhelmet.prism.services.recording.RecordingQueue;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionRegistry The action registry
     * @param expectationService The expectation service
     * @param filterService The filter service
     */
    @Inject
    public BlockPlaceListener(
            ConfigurationService configurationService,
            IActionRegistry actionRegistry,
            ExpectationService expectationService,
            FilterService filterService) {
        super(configurationService, actionRegistry, expectationService, filterService);
    }

    /**
     * Listens for block place events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();

        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().blockPlace()) {
            return;
        }

        Block blockPlaced = event.getBlockPlaced();
        final BlockState replacedState = event.getBlockReplacedState();

        // Build the action
        final IAction action = actionRegistry.createBlockAction(
            ActionRegistry.BLOCK_PLACE, blockPlaced.getState(), replacedState);

        // Build the block place by player activity
        final IActivity activity = Activity.builder()
            .action(action).location(blockPlaced.getLocation()).cause(player).build();

        if (filterService.allows(activity)) {
            RecordingQueue.addToQueue(activity);
        }
    }
}
