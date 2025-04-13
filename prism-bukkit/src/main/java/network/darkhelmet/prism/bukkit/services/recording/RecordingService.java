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

package network.darkhelmet.prism.bukkit.services.recording;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.concurrent.LinkedBlockingQueue;

import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.services.recording.IRecordingService;
import network.darkhelmet.prism.bukkit.PrismBukkit;
import network.darkhelmet.prism.bukkit.services.filters.FilterService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Singleton
public class RecordingService implements IRecordingService {
    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The filter service.
     */
    private final FilterService filterService;

    /**
     * The recording task.
     */
    private final RecordingTask recordingTask;

    /**
     * Set the recording mode.
     */
    private RecordMode recordMode = RecordMode.NORMAL;

    /**
     * Queue of activities.
     */
    private final LinkedBlockingQueue<ISingleActivity> queue = new LinkedBlockingQueue<>();

    /**
     * Cache the scheduled task.
     */
    private BukkitTask task;

    /**
     * The drain mode.
     */
    private enum RecordMode {
        NORMAL, DRAIN_SYNC, STOPPED
    }

    /**
     * Construct the recording manager.
     *
     * @param configurationService The configuration service
     * @param filterService The filter service
     * @param recordingTask The recording task
     */
    @Inject
    public RecordingService(
            ConfigurationService configurationService,
            FilterService filterService,
            RecordingTask recordingTask) {
        this.configurationService = configurationService;
        this.filterService = filterService;
        this.recordingTask = recordingTask;

        queueNextRecording(recordingTask);
    }

    @Override
    public boolean addToQueue(final ISingleActivity activity) {
        if (activity == null) {
            return false;
        }

        if (!filterService.shouldRecord(activity)) {
            return false;
        }

        queue.add(activity);

        return true;
    }

    @Override
    public void clearTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void drainSync() {
        recordMode = RecordMode.DRAIN_SYNC;

        RecordingTask recordingTask = this.recordingTask.toNew();
        while (!queue.isEmpty()) {
            recordingTask.save();
        }
    }

    @Override
    public LinkedBlockingQueue<ISingleActivity> queue() {
        return queue;
    }

    @Override
    public void queueNextRecording(Runnable recordingTask) {
        long delay = configurationService.prismConfig().recording().delay();
        queueNextRecording(delay);
    }

    /**
     * Queue the next recording with a specific delay.
     *
     * @param delay The delay
     */
    public void queueNextRecording(long delay) {
        if (task != null) {
            throw new IllegalStateException("Recording tasks must be cleared before scheduling a new one.");
        }

        if (recordMode.equals(RecordMode.NORMAL)) {
            task = Bukkit.getServer().getScheduler()
                .runTaskLaterAsynchronously(PrismBukkit.instance().loaderPlugin(), recordingTask, delay);
        }
    }

    @Override
    public void stop() {
        this.clearTask();

        recordMode = RecordMode.STOPPED;
    }
}
