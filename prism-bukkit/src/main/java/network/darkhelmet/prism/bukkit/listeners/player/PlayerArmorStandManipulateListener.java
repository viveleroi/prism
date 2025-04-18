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

import network.darkhelmet.prism.api.actions.Action;
import network.darkhelmet.prism.bukkit.actions.BukkitItemStackAction;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.api.activities.BukkitActivity;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.bukkit.utils.ItemUtils;
import network.darkhelmet.prism.bukkit.utils.TagLib;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class PlayerArmorStandManipulateListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public PlayerArmorStandManipulateListener(
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
    public void onPlayerArmorStandManipulate(final PlayerArmorStandManipulateEvent event) {
        Action action = null;
        if (event.getArmorStandItem().getType() == Material.AIR) {
            // Ignore if this event is disabled or the player is not holding a valid item
            if (!configurationService.prismConfig().actions().itemInsert()
                    || !TagLib.ALL_ARMOR.isTagged(event.getPlayerItem().getType())) {
                return;
            }

            action = new BukkitItemStackAction(BukkitActionTypeRegistry.ITEM_INSERT, event.getPlayerItem());
        } else {
            // Ignore if this event is disabled or the player is holding an item
            if (!configurationService.prismConfig().actions().itemRemove()
                    || !ItemUtils.nullOrAir(event.getPlayerItem())) {
                return;
            }

            action = new BukkitItemStackAction(BukkitActionTypeRegistry.ITEM_REMOVE, event.getArmorStandItem());
        }

        var activity = BukkitActivity.builder()
            .action(action)
            .player(event.getPlayer())
            .location(event.getRightClicked().getLocation())
            .build();

        recordingService.addToQueue(activity);
    }
}
