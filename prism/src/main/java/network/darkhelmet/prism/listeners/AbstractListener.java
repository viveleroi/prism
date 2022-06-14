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

import java.util.ArrayList;
import java.util.List;

import network.darkhelmet.prism.actions.ActionRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.actions.IActionRegistry;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.expectations.ExpectationType;
import network.darkhelmet.prism.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.filters.FilterService;
import network.darkhelmet.prism.services.recording.RecordingQueue;
import network.darkhelmet.prism.utils.BlockUtils;
import network.darkhelmet.prism.utils.EntityUtils;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

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
     * The expectation service.
     */
    protected final ExpectationService expectationService;

    /**
     * The filter service.
     */
    protected final FilterService filterService;

    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionRegistry The action registry
     * @param expectationService The expectation service
     * @param filterService The filter service
     */
    public AbstractListener(
            ConfigurationService configurationService,
            IActionRegistry actionRegistry,
            ExpectationService expectationService,
            FilterService filterService) {
        this.configurationService = configurationService;
        this.actionRegistry = actionRegistry;
        this.expectationService = expectationService;
        this.filterService = filterService;
    }

    /**
     * Process a block break. This looks for hanging items, detachables, etc.
     *
     * @param brokenBlock The block.
     * @param cause The cause.
     */
    protected void processBlockBreak(Block brokenBlock, Object cause) {
        final Block block = BlockUtils.rootBlock(brokenBlock);

        // Find any hanging entities.
        if (configurationService.prismConfig().actions().hangingBreak()) {
            for (Entity hanging : EntityUtils.hangingEntities(block.getLocation(), 2)) {
                expectationService.cacheFor(ExpectationType.DETACH).expect(hanging, cause);
            }
        }

        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().blockBreak()) {
            return;
        }

        // Record all blocks that will detach
        for (Block detachable : BlockUtils.detachables(new ArrayList<>(), block)) {
            recordBlockBreakAction(detachable, cause);
        }

        // Record all blocks that will fall
        for (Block faller : BlockUtils.gravity(new ArrayList<>(), block)) {
            recordBlockBreakAction(faller, cause);
        }

        // Record this block
        recordBlockBreakAction(block, cause);
    }

    /**
     * Process explosions.
     *
     * <p>This skips detachable logic because the affected
     * block lists will already include them.</p>
     *
     * <p>This skips checking for hanging items because
     * they're AIR by now.</p>
     *
     * @param affectedBlocks A list of affected blocks
     * @param cause The cause
     */
    protected void processExplosion(List<Block> affectedBlocks, Object cause) {
        for (Block affectedBlock : affectedBlocks) {
            final Block block = BlockUtils.rootBlock(affectedBlock);

            // Ignore if this event is disabled
            if (!configurationService.prismConfig().actions().blockBreak()) {
                continue;
            }

            // Record all blocks that will fall
            for (Block faller : BlockUtils.gravity(new ArrayList<>(), block)) {
                // Skip blocks already in the affected block list
                if (affectedBlocks.contains(faller)) {
                    continue;
                }

                recordBlockBreakAction(faller, cause);
            }

            // Record this block
            recordBlockBreakAction(block, cause);
        }
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
