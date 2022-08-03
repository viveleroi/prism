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

package network.darkhelmet.prism.services.modifications;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.IModificationQueue;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.IPreviewable;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.core.injection.factories.IRestoreFactory;
import network.darkhelmet.prism.core.injection.factories.IRollbackFactory;
import network.darkhelmet.prism.services.messages.MessageService;
import network.darkhelmet.prism.services.modifications.state.BlockStateChange;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Singleton
public class ModificationQueueService implements IModificationQueueService {
    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * Cache the current queue, if any.
     */
    private IModificationQueue currentQueue = null;

    /**
     * The restore factory.
     */
    private final IRestoreFactory restoreFactory;

    /**
     * The rollback factory.
     */
    private final IRollbackFactory rollbackFactory;

    /**
     * A cache of recently used queues.
     */
    Cache<Object, ModificationQueueResult> queueResults = Caffeine.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .maximumSize(4)
        .evictionListener((key, value, cause) -> cancelQueueForOwner(key))
        .build();

    /**
     * Constructor.
     *
     * @param messageService The message service
     * @param restoreFactory The restore factory
     * @param rollbackFactory The rollback factory.
     */
    @Inject
    public ModificationQueueService(
        MessageService messageService,
        IRestoreFactory restoreFactory,
        IRollbackFactory rollbackFactory) {
        this.messageService = messageService;
        this.restoreFactory = restoreFactory;
        this.rollbackFactory = rollbackFactory;
    }

    @Override
    public boolean queueAvailable() {
        return currentQueue == null;
    }

    @Override
    public boolean cancelQueueForOwner(Object owner) {
        if (currentQueue != null && currentQueue.owner().equals(owner)) {
            ModificationQueueResult result = queueResults.getIfPresent(owner);
            if (result != null) {
                // If queue has a cancellable result, cancel it
                if (result.queue() instanceof IPreviewable) {
                    cancelPreview(owner, result);
                }

                queueResults.invalidate(owner);
            }

            this.currentQueue.destroy();
            this.currentQueue = null;

            return true;
        }

        return false;
    }

    /**
     * Re-send live blocks for ones we faked.
     *
     * @param owner The owner
     * @param queueResult The queue result
     */
    protected void cancelPreview(Object owner, ModificationQueueResult queueResult) {
        if (!queueResult.mode().equals(ModificationQueueMode.PLANNING)) {
            return;
        }

        if (owner instanceof Player player) {
            for (final Iterator<ModificationResult> iterator = queueResult.results().listIterator();
                 iterator.hasNext(); ) {
                final ModificationResult result = iterator.next();

                if (result.stateChange() instanceof BlockStateChange blockStateChange) {
                    Location loc = blockStateChange.oldState().getLocation();
                    BlockData liveBlockData = loc.getWorld().getBlockData(loc);
                    player.sendBlockChange(loc, liveBlockData);
                }

                iterator.remove();
            }
        }
    }

    @Override
    public IModificationQueue currentQueue() {
        return currentQueue;
    }

    @Override
    public Optional<IModificationQueue> currentQueueForOwner(Object owner) {
        if (currentQueue != null && currentQueue.owner().equals(owner)) {
            return Optional.of(currentQueue);
        }

        return Optional.empty();
    }

    @Override
    public IModificationQueue newQueue(
            Class<? extends IModificationQueue> clazz,
            ModificationRuleset modificationRuleset,
            Object owner,
            ActivityQuery query,
            List<IActivity> modifications) {
        if (clazz.equals(Rollback.class)) {
            return newRollbackQueue(modificationRuleset, owner, query, modifications);
        } else if (clazz.equals(Restore.class)) {
            return newRestoreQueue(modificationRuleset, owner, query, modifications);
        }

        throw new IllegalArgumentException("Invalid modification queue.");
    }

    @Override
    public IModificationQueue newRollbackQueue(
            ModificationRuleset modificationRuleset,
            Object owner,
            ActivityQuery query,
            List<IActivity> modifications) {
        if (!queueAvailable()) {
            throw new IllegalStateException("No queue available until current queue finished.");
        }

        // Cancel any cached queues
        cancelQueueForOwner(owner);

        this.currentQueue = rollbackFactory.create(modificationRuleset, owner, query, modifications, this::onEnd);

        return this.currentQueue;
    }

    @Override
    public IModificationQueue newRestoreQueue(
            ModificationRuleset modificationRuleset,
            Object owner,
            ActivityQuery query,
            List<IActivity> modifications) {
        if (!queueAvailable()) {
            throw new IllegalStateException("No queue available until current queue finished.");
        }

        // Cancel any cached queues
        cancelQueueForOwner(owner);

        this.currentQueue = restoreFactory.create(modificationRuleset, owner, query, modifications, this::onEnd);

        return this.currentQueue;
    }

    /**
     * On queue end, handle some cleanup.
     *
     * @param result Modification queue result
     */
    protected void onEnd(ModificationQueueResult result) {
        queueResults.put(currentQueue.owner(), result);

        if (result.mode().equals(ModificationQueueMode.COMPLETING)) {
            // Message the user with results
            if (currentQueue.owner() instanceof CommandSender sender) {
                messageService.modificationsAppliedSuccess(sender);
                messageService.modificationsApplied(sender, result.applied());
                messageService.modificationsSkipped(sender, result.skipped());

                if (result.removedBlocks() > 0) {
                    messageService.modificationsRemovedBlocks(sender, result.removedBlocks());
                }

                if (result.removedDrops() > 0) {
                    messageService.modificationsRemovedDrops(sender, result.removedDrops());
                }
            }

            // Clear and destroy the queue if completing
            this.currentQueue.destroy();
            this.currentQueue = null;
        } else if (result.mode().equals(ModificationQueueMode.PLANNING)) {
            // Message the user with results
            if (currentQueue.owner() instanceof CommandSender sender) {
                messageService.modificationsAppliedSuccess(sender, result.planned());
            }
        }
    }

    @Override
    public Optional<ModificationQueueResult> queueResultForOwner(Object owner) {
        return Optional.ofNullable(queueResults.getIfPresent(owner));
    }
}
