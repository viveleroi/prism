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

import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.services.recording.IRecordingService;
import network.darkhelmet.prism.api.storage.IActivityBatch;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.core.services.configuration.StorageConfiguration;
import network.darkhelmet.prism.core.services.logging.LoggingService;

public class RecordingTask implements Runnable {
    /**
     * The storage config.
     */
    private final StorageConfiguration storageConfig;

    /**
     * The storage adapter.
     */
    private final IStorageAdapter storageAdapter;

    /**
     * The recording manager.
     */
    private final IRecordingService recordingService;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * Construct a new recording task.
     *
     * @param storageConfig The storage config
     * @param storageAdapter The storage adapter
     * @param recordingService The recording service
     * @param loggingService The logging service
     */
    @Inject
    public RecordingTask(
            StorageConfiguration storageConfig,
            IStorageAdapter storageAdapter,
            IRecordingService recordingService,
            LoggingService loggingService) {
        this.storageConfig = storageConfig;
        this.storageAdapter = storageAdapter;
        this.recordingService = recordingService;
        this.loggingService = loggingService;
    }

    @Override
    public void run() {
        save();

        // Schedule the next recording
        recordingService.queueNextRecording(
            new RecordingTask(storageConfig, storageAdapter, recordingService, loggingService));
    }

    /**
     * Saves anything in the queue, or as many as we can.
     */
    public void save() {
        if (!recordingService.queue().isEmpty()) {
            try {
                int batchCount = 0;
                int batchMax = storageConfig.batchMax();

                IActivityBatch batch = storageAdapter.createActivityBatch();
                batch.startBatch();

                while (!recordingService.queue().isEmpty()) {
                    batchCount++;
                    final ISingleActivity activity = recordingService.queue().poll();
                    batch.add(activity);

                    // Batch max exceeded, break
                    if (batchCount > batchMax) {
                        String msg = "Recorder: Batch max exceeded, running insert. Queue remaining: %d";
                        loggingService.debug(String.format(msg, recordingService.queue().size()));

                        break;
                    }
                }

                batch.commitBatch();
            } catch (Exception e) {
                loggingService.handleException(e);
            }
        }
    }
}
