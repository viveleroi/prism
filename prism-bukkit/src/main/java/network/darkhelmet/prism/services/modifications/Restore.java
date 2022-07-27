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
import com.google.inject.assistedinject.Assisted;

import java.util.List;
import java.util.function.Consumer;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.services.modifications.IRestore;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResultStatus;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

public class Restore extends AbstractWorldModificationQueue implements IRestore {
    /**
     * The storage adapter.
     */
    private final IStorageAdapter storageAdapter;

    /**
     * Construct a new restore.
     *
     * @param loggingService The logging service
     * @param storageAdapter The storage adapter
     * @param modificationRuleset The ruleset
     * @param owner The owner
     * @param query The query used
     * @param modifications A list of modifications
     * @param onEnd The end callback
     */
    @Inject
    public Restore(
        LoggingService loggingService,
        IStorageAdapter storageAdapter,
        @Assisted ModificationRuleset modificationRuleset,
        @Assisted Object owner,
        @Assisted ActivityQuery query,
        @Assisted final List<IActivity> modifications,
        @Assisted Consumer<ModificationQueueResult> onEnd
    ) {
        super(loggingService, modificationRuleset, owner, query, modifications, onEnd);

        this.storageAdapter = storageAdapter;
    }

    @Override
    protected ModificationResult applyModification(IActivity activity) {
        return activity.action().applyRestore(modificationRuleset, owner(), activity, mode);
    }

    @Override
    protected void onEnd(ModificationQueueResult result) {
        if (result.mode().equals(ModificationQueueMode.COMPLETING)) {
            // Get PKs of all applied activities
            List<Long> primarykeys = result.results().stream().filter(
                r -> r.status().equals(ModificationResultStatus.APPLIED)).map(
                    r -> (long) ((ISingleActivity) r.activity()).primaryKey()).toList();

            try {
                storageAdapter.markReversed(primarykeys, false);
            } catch (Exception e) {
                loggingService.handleException(e);
            }
        }

        super.onEnd(result);
    }

    @Override
    public void preview() {
        this.mode = ModificationQueueMode.PLANNING;
        execute();
    }
}