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
import com.google.inject.assistedinject.Assisted;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Getter;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.services.purges.IPurgeQueue;
import network.darkhelmet.prism.api.services.purges.PurgeCycleResult;
import network.darkhelmet.prism.api.services.purges.PurgeResult;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.api.util.Pair;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.providers.TaskChainProvider;

public class PurgeQueue implements IPurgeQueue {
    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * The task chain provider.
     */
    private final TaskChainProvider taskChainProvider;

    /**
     * The storage adapter.
     */
    private final IStorageAdapter storageAdapter;

    /**
     * The cycle callback.
     */
    private final Consumer<PurgeCycleResult> onCycle;

    /**
     * The end callback.
     */
    private final Consumer<PurgeResult> onEnd;

    /**
     * The queue of purge queries.
     */
    private final List<ActivityQuery> purgeQueue = Collections.synchronizedList(new LinkedList<>());

    /**
     * The running flag.
     */
    @Getter
    private boolean running = false;

    /**
     * The total deletions count.
     */
    private int deleted = 0;

    /**
     * Constructor.
     *
     * @param configurationService The configuration service
     * @param loggingService The logging service
     * @param taskChainProvider The task chain provider
     * @param storageAdapter The storage adapter
     * @param onCycle The cycle callback
     * @param onEnd The end callback
     */
    @Inject
    public PurgeQueue(
            ConfigurationService configurationService,
            LoggingService loggingService,
            TaskChainProvider taskChainProvider,
            IStorageAdapter storageAdapter,
            @Assisted Consumer<PurgeCycleResult> onCycle,
            @Assisted Consumer<PurgeResult> onEnd) {
        this.configurationService = configurationService;
        this.loggingService = loggingService;
        this.taskChainProvider = taskChainProvider;
        this.storageAdapter = storageAdapter;
        this.onCycle = onCycle;
        this.onEnd = onEnd;
    }

    @Override
    public void add(ActivityQuery query) {
        purgeQueue.add(query);
    }

    @Override
    public void start() {
        running = true;

        taskChainProvider.newChain().asyncFirst(() -> {
            Pair<Integer, Integer> keys = storageAdapter.getActivitiesPkBounds();

            loggingService.debug(String.format("Absolute purge lower/bound primary keys: %d, %d",
                keys.key(), keys.value()));

            return keys;
        }).syncLast(keys -> executeNext(keys.key(), keys.value())).execute();
    }

    /**
     * Execute the next purge query or cycle.
     *
     * @param cycleMinPrimaryKey The minimum primary key for this cycle
     * @param maxPrimaryKey The absolute upper bound primary key
     */
    protected void executeNext(int cycleMinPrimaryKey, int maxPrimaryKey) {
        if (purgeQueue.isEmpty()) {
            if (running) {
                running = false;

                loggingService.logger().info("Purge queue now empty, finishing.");

                onEnd.accept(PurgeResult.builder().deleted(deleted).build());
            }

            return;
        }

        Long cycleDuration = null;
        TimeUnit cycleTimeUnit = null;

        if (configurationService.prismConfig().purges().cycleDelay() != null) {
            cycleDuration = configurationService.prismConfig().purges().cycleDelay().duration();
            cycleTimeUnit = configurationService.prismConfig().purges().cycleDelay().timeUnit();
        }

        if (cycleDuration == null) {
            cycleDuration = 2L;
        }

        if (cycleTimeUnit == null) {
            cycleTimeUnit = TimeUnit.SECONDS;
        }

        // Get the query
        ActivityQuery query = purgeQueue.get(0);
        taskChainProvider.newChain().asyncFirst(() -> {
            loggingService.logger().info(String.format("Executing next purge for query %s...", query));

            // Calculate the cycle upper bound
            int cycleMaxPrimaryKey = cycleMinPrimaryKey + configurationService.prismConfig().purges().limit();
            loggingService.debug(String.format("Limiting cycle to primary keys %d - %d",
                cycleMinPrimaryKey, cycleMaxPrimaryKey));

            // Delete this batch and store the count
            int count = storageAdapter.deleteActivities(query, cycleMinPrimaryKey, cycleMaxPrimaryKey);
            deleted += count;

            // Emit information to the cycle callback
            onCycle.accept(PurgeCycleResult.builder()
                .deleted(count)
                .minPrimaryKey(cycleMinPrimaryKey)
                .maxPrimaryKey(cycleMaxPrimaryKey)
                .build());
            loggingService.logger().info(String.format("Purged %d activity records", count));

            // Advance our starting pk for the next cycle
            int nextCycleMinPrimaryKey = cycleMinPrimaryKey + configurationService.prismConfig().purges().limit();

            // If we deleted less than our limit, or our next pk exceeds the max,
            // remove this query from the queue
            if (nextCycleMinPrimaryKey >= maxPrimaryKey) {
                purgeQueue.remove(0);
            }

            loggingService.logger().info("Scheduling next cycle (and any configured delay)");

            return nextCycleMinPrimaryKey;
        }).delay(cycleDuration.intValue(), cycleTimeUnit).syncLast(nextCycleMinPrimaryKey -> {
            this.executeNext(nextCycleMinPrimaryKey, maxPrimaryKey);
        }).execute();
    }
}
