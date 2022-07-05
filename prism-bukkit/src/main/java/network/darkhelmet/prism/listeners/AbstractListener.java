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
import java.util.Locale;

import network.darkhelmet.prism.actions.ActionFactory;
import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.actions.IActionFactory;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.activities.SingleActivity;
import network.darkhelmet.prism.api.services.expectations.ExpectationType;
import network.darkhelmet.prism.core.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.filters.FilterService;
import network.darkhelmet.prism.services.recording.RecordingQueue;
import network.darkhelmet.prism.utils.BlockUtils;
import network.darkhelmet.prism.utils.EntityUtils;
import network.darkhelmet.prism.utils.LocationUtils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbstractListener {
    /**
     * The configuration service.
     */
    protected final ConfigurationService configurationService;

    /**
     * The action registry.
     */
    protected final IActionFactory<BlockState, Entity, ItemStack> actionFactory;

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
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param filterService The filter service
     */
    public AbstractListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            FilterService filterService) {
        this.configurationService = configurationService;
        this.actionFactory = actionFactory;
        this.expectationService = expectationService;
        this.filterService = filterService;
    }

    /**
     * Converts a cause to a descriptor.
     *
     * @param cause The cause
     * @return The cause descriptor
     */
    protected String causeDescriptor(Object cause) {
        String finalCause = "unknown";
        if (cause instanceof Entity causeEntity) {
            finalCause = causeEntity.getType().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        } else if (cause instanceof Block causeBlock) {
            finalCause = causeBlock.getType().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        } else if (cause instanceof String causeStr) {
            finalCause = causeStr;
        }

        return finalCause;
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
        final IAction action = actionFactory.createBlockAction(ActionTypeRegistry.BLOCK_BREAK, block.getState());

        // Build the block break by player activity
        final SingleActivity.Builder builder = SingleActivity.builder()
            .action(action).location(LocationUtils.locToWorldCoordinate(block.getLocation()));

        if (cause instanceof String) {
            builder.cause((String) cause);
        } else if (cause instanceof Player player) {
            builder.player(player.getUniqueId(), player.getName());
        }

        ISingleActivity activity = builder.build();
        if (filterService.allows(activity)) {
            RecordingQueue.addToQueue(activity);
        }
    }
}
