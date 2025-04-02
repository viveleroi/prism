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

package network.darkhelmet.prism.services.purge;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.function.Consumer;

import network.darkhelmet.prism.api.services.purges.IPurgeQueue;
import network.darkhelmet.prism.api.services.purges.PurgeCycleResult;
import network.darkhelmet.prism.api.services.purges.PurgeResult;
import network.darkhelmet.prism.core.injection.factories.IPurgeQueueFactory;

@Singleton
public class PurgeService {
    /**
     * The purge queue factory.
     */
    private final IPurgeQueueFactory purgeQueueFactory;

    /**
     * Cache the current queue.
     */
    private IPurgeQueue currentQueue;

    /**
     * Constructor.
     *
     * @param purgeQueueFactory The purge queue factory
     */
    @Inject
    public PurgeService(IPurgeQueueFactory purgeQueueFactory) {
        this.purgeQueueFactory = purgeQueueFactory;
    }

    /**
     * Create a new queue.
     *
     * @param onCycle The cycle callback
     * @return The purge queue
     */
    public IPurgeQueue newQueue(Consumer<PurgeCycleResult> onCycle, Consumer<PurgeResult> onEnd) {
        if (!queueFree()) {
            throw new IllegalStateException("Queue is not free.");
        }

        currentQueue = purgeQueueFactory.create(onCycle, purgeResult -> {
            release();

            onEnd.accept(purgeResult);
        });
        return currentQueue;
    }

    /**
     * Check if the queue is free.
     *
     * @return True if no queue
     */
    public boolean queueFree() {
        return currentQueue == null;
    }

    /**
     * Release the queue.
     */
    public void release() {
        if (currentQueue != null && currentQueue.running()) {
            throw new IllegalStateException("Cannot release a running queue.");
        }

        currentQueue = null;
    }
}
