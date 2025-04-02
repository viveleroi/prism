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

package network.darkhelmet.prism.listeners.player;

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
import network.darkhelmet.prism.utils.BlockUtils;
import network.darkhelmet.prism.utils.LocationUtils;

import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

public class PlayerHarvestBlockListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public PlayerHarvestBlockListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService) {
        super(configurationService, actionFactory, expectationService, recordingService);
    }

    /**
     * Listens for block harvest events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHarvestBlock(final PlayerHarvestBlockEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().blockHarvest()) {
            return;
        }

        BlockState oldState = event.getHarvestedBlock().getState();

        // Fake the new state as "harvested". Sadly this needs per-material logic.
        BlockState newState = event.getHarvestedBlock().getState();
        if (newState.getBlockData() instanceof Ageable ageable) {
            Integer age = BlockUtils.harvestedAge(oldState.getType());
            if (age != null) {
                ageable.setAge(age);
                newState.setBlockData(ageable);
            }
        }

        // Build the action
        final IAction action = actionFactory.createBlockStateAction(
            ActionTypeRegistry.BLOCK_HARVEST, newState, oldState);

        // Build the activity
        ISingleActivity activity = Activity.builder()
            .action(action)
            .player(event.getPlayer().getUniqueId(), event.getPlayer().getName())
            .location(LocationUtils.locToWorldCoordinate(oldState.getLocation()))
            .build();

        recordingService.addToQueue(activity);
    }
}
