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

package org.prism_mc.prism.paper.integrations.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;
import org.prism_mc.prism.paper.services.recording.PaperRecordingService;

public class WorldEditLoggingHandler {

    /**
     * The recording service.
     */
    private final PaperRecordingService recordingService;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * Whether FAWE is installed.
     */
    private final boolean fawe;

    /**
     * Constructor.
     *
     * @param recordingService The recording service
     * @param configurationService The configuration service
     */
    public WorldEditLoggingHandler(PaperRecordingService recordingService, ConfigurationService configurationService) {
        this.recordingService = recordingService;
        this.configurationService = configurationService;
        this.fawe = isFawePresent();
    }

    /**
     * Check if FAWE classes are available.
     *
     * @return true if FAWE is present
     */
    private static boolean isFawePresent() {
        try {
            Class.forName("com.fastasyncworldedit.core.Fawe");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Handle EditSessionEvent.
     *
     * @param event The EditSessionEvent
     */
    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        // FAWE: by BEFORE_HISTORY the extent is (almost always) a delegating extent, so our
        // processor or fallback extent is registered in place rather than replacing the
        // extent, which FAWE would block as an unknown third-party extent.
        EditSession.Stage stage = fawe ? EditSession.Stage.BEFORE_HISTORY : EditSession.Stage.BEFORE_CHANGE;
        if (event.getStage() != stage) {
            return;
        }

        // Check if either WorldEdit action is enabled
        if (
            !configurationService.prismConfig().actions().worldeditBreak() &&
            !configurationService.prismConfig().actions().worldeditPlace()
        ) {
            return;
        }

        // Get the Bukkit world
        World weWorld = event.getWorld();
        if (weWorld == null) {
            return;
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld(weWorld.getName());
        if (bukkitWorld == null) {
            return;
        }

        // Get the Bukkit player if the actor is a player
        org.bukkit.entity.Player bukkitPlayer = null;
        if (event.getActor() instanceof Player actor) {
            bukkitPlayer = Bukkit.getPlayer(actor.getUniqueId());
        }

        var recorder = new WorldEditChangeRecorder(bukkitPlayer, bukkitWorld, recordingService, configurationService);

        if (fawe) {
            FaweLoggingHelper.addProcessor(event, recorder);
        } else {
            event.setExtent(new PrismLoggingExtent(event.getExtent(), recorder));
        }
    }
}
