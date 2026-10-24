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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;
import org.prism_mc.prism.paper.actions.PaperBlockAction;
import org.prism_mc.prism.paper.actions.types.PaperActionTypeRegistry;
import org.prism_mc.prism.paper.api.activities.PaperActivity;
import org.prism_mc.prism.paper.services.recording.PaperRecordingService;

public class WorldEditChangeRecorder {

    /**
     * The Bukkit player performing the edit, or null for non-player edits.
     */
    private final Player bukkitPlayer;

    /**
     * The Bukkit world being edited.
     */
    private final World bukkitWorld;

    /**
     * The recording service.
     */
    private final PaperRecordingService recordingService;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * Construct a WorldEditChangeRecorder.
     *
     * @param bukkitPlayer The Bukkit player performing the edit (may be null)
     * @param bukkitWorld The Bukkit world
     * @param recordingService The recording service
     * @param configurationService The configuration service
     */
    public WorldEditChangeRecorder(
        Player bukkitPlayer,
        World bukkitWorld,
        PaperRecordingService recordingService,
        ConfigurationService configurationService
    ) {
        this.bukkitPlayer = bukkitPlayer;
        this.bukkitWorld = bukkitWorld;
        this.recordingService = recordingService;
        this.configurationService = configurationService;
    }

    /**
     * Whether any WorldEdit action is enabled for recording.
     *
     * @return True if worldedit-break or worldedit-place is enabled
     */
    public boolean enabled() {
        return (
            configurationService.prismConfig().actions().worldeditBreak() ||
            configurationService.prismConfig().actions().worldeditPlace()
        );
    }

    /**
     * Record a single block change.
     *
     * @param x The block x coordinate
     * @param y The block y coordinate
     * @param z The block z coordinate
     * @param oldState The block state before the change
     * @param newState The block state after the change
     */
    public void record(int x, int y, int z, BlockState oldState, BlockState newState) {
        boolean logBreak = configurationService.prismConfig().actions().worldeditBreak();
        boolean logPlace = configurationService.prismConfig().actions().worldeditPlace();

        boolean oldIsAir = oldState.getBlockType().getMaterial().isAir();
        boolean newIsAir = newState.getBlockType().getMaterial().isAir();

        Object cause = bukkitPlayer != null ? bukkitPlayer : "WorldEdit";
        Location location = new Location(bukkitWorld, x, y, z);

        // Log block removal (break) only if replacing with air (actual removal)
        if (!oldIsAir && newIsAir && logBreak) {
            BlockData oldBlockData = BukkitAdapter.adapt(oldState);
            String oldTranslationKey = oldBlockData.getMaterial().getBlockTranslationKey();

            var breakAction = new PaperBlockAction(
                PaperActionTypeRegistry.WORLDEDIT_BREAK,
                oldBlockData,
                oldTranslationKey,
                null,
                null
            );

            var breakActivity = PaperActivity.builder().action(breakAction).location(location).cause(cause).build();

            recordingService.addToQueue(breakActivity);
        }

        // Log block placement if new block is not air
        if (!newIsAir && logPlace) {
            BlockData newBlockData = BukkitAdapter.adapt(newState);
            String newTranslationKey = newBlockData.getMaterial().getBlockTranslationKey();

            // For the replaced state, use the old state if it wasn't air
            BlockData oldBlockData = null;
            String oldTranslationKey = null;
            if (!oldIsAir) {
                oldBlockData = BukkitAdapter.adapt(oldState);
                oldTranslationKey = oldBlockData.getMaterial().getBlockTranslationKey();
            }

            var placeAction = new PaperBlockAction(
                PaperActionTypeRegistry.WORLDEDIT_PLACE,
                newBlockData,
                newTranslationKey,
                oldBlockData,
                oldTranslationKey
            );

            var placeActivity = PaperActivity.builder().action(placeAction).location(location).cause(cause).build();

            recordingService.addToQueue(placeActivity);
        }
    }
}
