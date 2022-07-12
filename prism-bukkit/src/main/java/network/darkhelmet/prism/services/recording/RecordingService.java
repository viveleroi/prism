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

package network.darkhelmet.prism.services.recording;

import com.google.inject.Inject;

import java.util.concurrent.LinkedBlockingQueue;

import network.darkhelmet.prism.PrismBukkit;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.services.recording.IRecordingService;
import network.darkhelmet.prism.services.filters.FilterService;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class RecordingService implements IRecordingService {
    /**
     * The filter service.
     */
    protected final FilterService filterService;

    /**
     * Queue of activities.
     */
    private final LinkedBlockingQueue<ISingleActivity> queue = new LinkedBlockingQueue<>();

    /**
     * Cache the scheduled task.
     */
    private BukkitTask task;

    /**
     * Construct the recording manager.
     *
     * @param recordingTask The recording task
     */
    @Inject
    public RecordingService(
            FilterService filterService,
            RecordingTask recordingTask) {
        this.filterService = filterService;

        queueNextRecording(recordingTask);
    }

    @Override
    public boolean addToQueue(final ISingleActivity activity) {
        if (activity == null) {
            return false;
        }

        if (!filterService.allows(activity)) {
            return false;
        }

        queue.add(activity);

        return true;
    }

    @Override
    public LinkedBlockingQueue<ISingleActivity> queue() {
        return queue;
    }

    @Override
    public void queueNextRecording(Runnable recordingTask) {
        task = Bukkit.getServer().getScheduler()
            .runTaskLaterAsynchronously(PrismBukkit.getInstance(), recordingTask, 10);
    }
}
