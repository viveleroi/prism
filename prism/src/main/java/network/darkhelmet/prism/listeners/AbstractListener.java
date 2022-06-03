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

import network.darkhelmet.prism.actions.ActionRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.actions.IActionRegistry;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.filters.FilterService;
import network.darkhelmet.prism.services.recording.RecordingQueue;

import org.bukkit.block.Block;

public class AbstractListener {
    /**
     * The configuration service.
     */
    protected final ConfigurationService configurationService;

    /**
     * The action registry.
     */
    protected final IActionRegistry actionRegistry;

    /**
     * The filter service.
     */
    protected final FilterService filterService;

    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionRegistry The action registry
     * @param filterService The filter service
     */
    public AbstractListener(
            ConfigurationService configurationService,
            IActionRegistry actionRegistry,
            FilterService filterService) {
        this.configurationService = configurationService;
        this.actionRegistry = actionRegistry;
        this.filterService = filterService;
    }

    /**
     * Convenience method for recording a block break action.
     *
     * @param block The block
     * @param cause The cause
     */
    protected void recordBlockBreakAction(Block block, Object cause) {
        // Build the action
        final IAction action = actionRegistry.createBlockAction(ActionRegistry.BLOCK_BREAK, block.getState());

        // Build the block break by player activity
        final IActivity activity = Activity.builder()
            .action(action).location(block.getLocation()).cause(cause).build();

        if (filterService.allows(activity)) {
            RecordingQueue.addToQueue(activity);
        }
    }
}
