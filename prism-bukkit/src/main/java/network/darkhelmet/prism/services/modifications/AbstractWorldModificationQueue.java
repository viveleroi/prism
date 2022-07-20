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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import lombok.Getter;

import network.darkhelmet.prism.PrismBukkit;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.IModificationQueue;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResultStatus;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractWorldModificationQueue implements IModificationQueue {
    /**
     * The logging service.
     */
    protected LoggingService loggingService;

    /**
     * Manage a queue of pending modifications.
     */
    private final List<IActivity> modificationsQueue = Collections.synchronizedList(new LinkedList<>());

    /**
     * The onEnd handler.
     */
    private final Consumer<ModificationQueueResult> onEndCallback;

    /**
     * The period duration between executions of tasks.
     * @todo Move this to config
     */
    private final long taskPeriod = 5;

    /**
     * The maximum number of queue activities read per task run.
     * @todo Move this to config
     */
    private final int maxPerTask = 1000;

    /**
     * The owner.
     */
    @Getter
    private Object owner;

    /**
     * The state.
     */
    @Getter
    protected ModificationQueueMode mode = ModificationQueueMode.UNDECIDED;

    /**
     * Cache the bukkit task id.
     */
    private int taskId;

    /**
     * Count how many were read from the queue.
     */
    private int countModificationsRead;

    /**
     * Count how many were applied.
     */
    private int countApplied = 0;

    /**
     * Count how many were planned.
     */
    private int countPlanned = 0;

    /**
     * Count how many were skipped.
     */
    private int countSkipped = 0;

    /**
     * A list of all modification results.
     */
    private final List<ModificationResult> results = new ArrayList<>();

    /**
     * Construct a new world modification.
     *
     * @param loggingService The logging service
     * @param owner The owner
     * @param modifications A list of all modifications
     * @param onEndCallback The ended callback
     */
    public AbstractWorldModificationQueue(
            LoggingService loggingService,
            Object owner,
            final List<IActivity> modifications,
            Consumer<ModificationQueueResult> onEndCallback) {
        modificationsQueue.addAll(modifications);
        this.loggingService = loggingService;
        this.owner = owner;
        this.onEndCallback = onEndCallback;
    }

    /**
     * Apply a modification.
     *
     * @param activity The activity
     * @return The modification result
     */
    protected ModificationResult applyModification(IActivity activity) {
        return ModificationResult.builder().status(ModificationResultStatus.SKIPPED).build();
    }

    /**
     * Apply any post-modification tasks.
     */
    protected void postProcess() {}

    @Override
    public void preview() {
        this.mode = ModificationQueueMode.PLANNING;
        execute();
    }

    @Override
    public void apply() {
        this.mode = ModificationQueueMode.COMPLETING;
        execute();
    }

    protected void execute() {
        String queueSizeMsg = "Modification queue beginning application. Queue size: %d";
        loggingService.debug(String.format(queueSizeMsg, modificationsQueue.size()));

        if (!modificationsQueue.isEmpty()) {
            // Schedule a new sync task
            JavaPlugin plugin = PrismBukkit.getInstance().loaderPlugin();
            taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                loggingService.debug("New modification run beginning...");

                int iterationCount = 0;
                final int currentQueueOffset = countModificationsRead;

                if (currentQueueOffset < modificationsQueue.size()) {
                    for (final Iterator<IActivity> iterator = modificationsQueue.listIterator(currentQueueOffset);
                         iterator.hasNext();) {
                        final IActivity activity = iterator.next();

                        // Simulate queue pointer advancement for previews
                        if (mode.equals(ModificationQueueMode.PLANNING)) {
                            countModificationsRead++;
                        }

                        // Limit the absolute max number of steps per execution of this task
                        if (++iterationCount >= maxPerTask) {
                            break;
                        }

                        // Delegate the modifications to the actions
                        ModificationResult result = applyModification(activity);
                        results.add(result);

                        if (result.status().equals(ModificationResultStatus.PLANNED)) {
                            countPlanned++;
                        } else if (result.status().equals(ModificationResultStatus.APPLIED)) {
                            countApplied++;
                        } else {
                            countSkipped++;
                        }

                        // Remove from the queue if we're not previewing
                        if (mode.equals(ModificationQueueMode.COMPLETING)) {
                            iterator.remove();
                        }
                    }
                }

                // The task for this action is done being used
                if (modificationsQueue.isEmpty() || countModificationsRead >= modificationsQueue.size()) {
                    loggingService.debug("Modification queue now empty, finishing up.");

                    // Cancel the repeating task
                    Bukkit.getServer().getScheduler().cancelTask(taskId);

                    ModificationQueueResult result = ModificationQueueResult.builder()
                        .mode(mode)
                        .results(results)
                        .applied(countApplied)
                        .planned(countPlanned)
                        .skipped(countSkipped)
                        .build();

                    onEnd(result);

                    // Post process
                    postProcess();
                }
            }, 0, taskPeriod);
        }
    }

    @Override
    public void destroy() {
        Bukkit.getServer().getScheduler().cancelTask(taskId);
    }

    /**
     * Called when the modification queue has ended.
     *
     * @param result The result
     */
    protected void onEnd(ModificationQueueResult result) {
        // Execute the callback, letting the caller know we've ended
        onEndCallback.accept(result);
    }
}
