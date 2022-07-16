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

import com.google.inject.Inject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import net.jodah.expiringmap.ExpiringMap;

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
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.modifications.state.BlockStateChange;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class ModificationQueueService implements IModificationQueueService {
    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

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
    Map<Object, ModificationQueueResult> queueResults = ExpiringMap.builder()
        .maxSize(4)
        .expiration(5, TimeUnit.MINUTES)
        .expirationListener((owner, result) -> cancelQueueForOwner(owner))
        .build();

    /**
     * Constructor.
     *
     * @param configurationService The configuration service
     * @param restoreFactory The restore factory
     * @param rollbackFactory The rollback factory.
     */
    @Inject
    public ModificationQueueService(
        ConfigurationService configurationService,
        IRestoreFactory restoreFactory,
        IRollbackFactory rollbackFactory) {
        this.configurationService = configurationService;
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
            if (queueResults.containsKey(owner)) {
                ModificationQueueResult result = queueResults.get(owner);

                // If queue has a cancellable result, cancel it
                if (result.queue() instanceof IPreviewable) {
                    cancelPreview(owner, result);
                }

                queueResults.remove(owner);
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

        // Clear and destroy the queue if completing
        if (result.mode().equals(ModificationQueueMode.COMPLETING)) {
            this.currentQueue.destroy();
            this.currentQueue = null;
        }
    }

    @Override
    public Optional<ModificationQueueResult> queueResultForOwner(Object owner) {
        if (queueResults.containsKey(owner)) {
            return Optional.of(queueResults.get(owner));
        }

        return Optional.empty();
    }
}
