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
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.recording.RecordingService;
import network.darkhelmet.prism.services.wands.WandService;
import network.darkhelmet.prism.utils.LocationUtils;

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
    private final IModificationQueueService modificationQueueService;

    /**
     * The cache service.
     */
    private final CacheService cacheService;

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
            IModificationQueueService modificationQueueService,
            CacheService cacheService) {
        super(configurationService, actionFactory, expectationService, recordingService);

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
        modificationQueueService.cancelQueueForOwner(player);

        // Deactivate any wands
        wandService.deactivateWand(player);

        if (configurationService.prismConfig().actions().playerQuit()) {
            // Build the action
            final IAction action = actionFactory.createAction(ActionTypeRegistry.PLAYER_QUIT);

            // Build the activity
            final ISingleActivity activity = Activity.builder()
                .action(action)
                .location(LocationUtils.locToWorldCoordinate(player.getLocation()))
                .player(player.getUniqueId(), player.getName())
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
