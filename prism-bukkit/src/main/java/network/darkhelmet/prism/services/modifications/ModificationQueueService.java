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

import java.util.List;
import java.util.Optional;

import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.IModificationQueue;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueResult;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueState;

public class ModificationQueueService implements IModificationQueueService {
    /**
     * Cache the current queue, if any.
     */
    private IModificationQueue currentQueue = null;

    @Override
    public boolean queueAvailable() {
        return currentQueue == null;
    }

    @Override
    public boolean cancelQueueForOwner(Object owner) {
        if (currentQueue != null && currentQueue.owner().equals(owner)) {
            currentQueue.cancel();
            currentQueue = null;
        }

        return false;
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
    public IModificationQueue newRollbackQueue(Object owner, List<IActivity> modifications) {
        if (!queueAvailable()) {
            throw new IllegalStateException("No queue available until current queue finished.");
        }

        this.currentQueue = new Rollback(owner, modifications, this::onComplete);
        return this.currentQueue;
    }

    @Override
    public IModificationQueue newRestoreQueue(Object owner, List<IActivity> modifications) {
        if (!queueAvailable()) {
            throw new IllegalStateException("No queue available until current queue finished.");
        }

        this.currentQueue = new Restore(owner, modifications, this::onComplete);
        return this.currentQueue;
    }

    /**
     * On queue completion, handle some cleanup.
     *
     * @param result Modification queue result
     */
    protected void onComplete(ModificationQueueResult result) {
        if (result.phase().equals(ModificationQueueState.COMPLETE)) {
            this.currentQueue = null;
        }
    }
}
