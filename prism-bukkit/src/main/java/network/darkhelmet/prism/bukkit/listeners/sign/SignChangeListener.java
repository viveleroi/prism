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

package network.darkhelmet.prism.bukkit.listeners.sign;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import network.darkhelmet.prism.bukkit.actions.BukkitBlockAction;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.api.activities.BukkitActivity;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public SignChangeListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService) {
        super(configurationService, expectationService, recordingService);
    }

    /**
     * Listens for sign change events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().signEdit()) {
            return;
        }

        final Player player = event.getPlayer();

        var action = new BukkitBlockAction(BukkitActionTypeRegistry.SIGN_EDIT, event.getBlock().getState(), null);

        // Because the block state doesn't have the new lines from a sign change event,
        // we need to fake it by merging in the text so it gets recorded.
        if (event.getSide().equals(Side.FRONT)) {
            action.mergeCompound(String.format("{front_text:{messages:[%s]}}", linesToNbtMessages(event.getLines())));
        } else {
            action.mergeCompound(String.format("{back_text:{messages:[%s]}}", linesToNbtMessages(event.getLines())));
        }

        var activity = BukkitActivity.builder()
            .action(action)
            .location(event.getBlock().getLocation())
            .player(player).build();

        recordingService.addToQueue(activity);
    }

    /**
     * Convert the lines array to a nbt messages format.
     *
     * @param lines The lines
     * @return Nbt messages string
     */
    protected String linesToNbtMessages(String[] lines) {
        List<String> messages = new ArrayList<>();

        for (String line : lines) {
            messages.add(String.format("'\"%s\"'", line));
        }

        return String.join(",", messages);
    }
}
