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

package org.prism_mc.prism.api.services.modifications;

import java.util.List;
import java.util.Optional;

import org.prism_mc.prism.api.activities.Activity;
import org.prism_mc.prism.api.activities.ActivityQuery;

public interface ModificationQueueService {
    /**
     * Check if the queue is free. Nicer than trying and getting exceptions.
     *
     * @return True if a new queue can be made.
     */
    boolean queueAvailable();

    /**
     * Cancel an active queue for a given owner.
     *
     * @param owner The owner
     * @return True if the queue was cleared
     */
    boolean cancelQueueForOwner(Object owner);

    /**
     * Clear active queues and/or recent queue results for a given owner.
     *
     * @param owner The owner
     */
    void clearEverythingForOwner(Object owner);

    /**
     * The current modification queue.
     *
     * @return The queue
     */
    ModificationQueue currentQueue();

    /**
     * Get the current queue only if it's owned by a given owner.
     *
     * @param owner The owner
     * @return The queue, if any
     */
    Optional<ModificationQueue> currentQueueForOwner(Object owner);

    /**
     * Create a new modification queue.
     *
     * @param clazz The modification queue class
     * @param owner The owner
     * @param query The query used
     * @param modifications A list of activities to make modifications
     * @return The rollback queue
     * @throws IllegalStateException If queue can't be created
     */
    ModificationQueue newQueue(
        Class<? extends ModificationQueue> clazz,
        ModificationRuleset modificationRuleset,
        Object owner,
        ActivityQuery query,
        List<Activity> modifications);

    /**
     * Create a new rollback queue.
     *
     * @param owner The owner
     * @param query The query used
     * @param modifications A list of activities to make modifications
     * @return The rollback queue
     * @throws IllegalStateException If queue can't be created
     */
    ModificationQueue newRollbackQueue(
        ModificationRuleset modificationRuleset,
        Object owner,
        ActivityQuery query,
        List<Activity> modifications);

    /**
     * Create a new restore queue.
     *
     * @param owner The owner
     * @param query The query used
     * @param modifications A list of activities to make modifications
     * @return The restore queue
     * @throws IllegalStateException If queue can't be created
     */
    ModificationQueue newRestoreQueue(
        ModificationRuleset modificationRuleset,
        Object owner,
        ActivityQuery query,
        List<Activity> modifications);

    /**
     * Get a queue result for a given owner.
     *
     * @param owner The owner
     * @return The queue result
     */
    Optional<ModificationQueueResult> queueResultForOwner(Object owner);
}
