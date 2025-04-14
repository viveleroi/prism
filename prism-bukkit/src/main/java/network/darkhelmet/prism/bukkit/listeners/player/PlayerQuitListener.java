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

import network.darkhelmet.prism.api.services.modifications.ModificationQueueService;
import network.darkhelmet.prism.bukkit.actions.GenericBukkitAction;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.api.activities.BukkitActivity;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.bukkit.services.wands.WandService;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.entity.Player;
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
    private final ModificationQueueService modificationQueueService;

    /**
     * The cache service.
     */
    private final CacheService cacheService;

    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     * @param wandService The wand service
     * @param modificationQueueService The modification queue service
     */
    @Inject
    public PlayerQuitListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService,
            WandService wandService,
            ModificationQueueService modificationQueueService,
            CacheService cacheService) {
        super(configurationService, expectationService, recordingService);

        this.wandService = wandService;
        this.modificationQueueService = modificationQueueService;
        this.cacheService = cacheService;
    }

    /**
     * On player quit.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        // Cancel any modification queues for this player
        modificationQueueService.clearEverythingForOwner(player);

        // Deactivate any wands
        wandService.deactivateWand(player);

        if (configurationService.prismConfig().actions().playerQuit()) {
            var action = new GenericBukkitAction(BukkitActionTypeRegistry.PLAYER_QUIT);

            var activity = BukkitActivity.builder()
                .action(action)
                .location(player.getLocation())
                .player(player)
                .build();

            recordingService.addToQueue(activity);
        }

        // Remove cached player data
        Long playerPk = cacheService.playerUuidPkMap().getIfPresent(event.getPlayer().getUniqueId());
        if (playerPk != null) {
            // Remove player's PK -> cause PK from the cache first
            cacheService.playerCausePkMap().invalidate(playerPk);

            // Remove the player's UUID -> PK from the cache
            cacheService.playerUuidPkMap().invalidate(event.getPlayer().getUniqueId());
        }
    }
}
