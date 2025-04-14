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

package network.darkhelmet.prism.bukkit.listeners.player;

import com.google.inject.Inject;

import network.darkhelmet.prism.bukkit.actions.BukkitBlockAction;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.api.activities.BukkitActivity;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.bukkit.utils.BlockUtils;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

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
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public PlayerHarvestBlockListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService) {
        super(configurationService, expectationService, recordingService);
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

        var action = new BukkitBlockAction(BukkitActionTypeRegistry.BLOCK_HARVEST, newState, oldState);

        var activity = BukkitActivity.builder()
            .action(action)
            .player(event.getPlayer())
            .location(oldState.getLocation())
            .build();

        recordingService.addToQueue(activity);
    }
}
