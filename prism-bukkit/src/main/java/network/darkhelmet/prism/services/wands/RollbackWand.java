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

package network.darkhelmet.prism.services.wands;

import com.google.inject.Inject;

import java.util.List;

import network.darkhelmet.prism.PrismBukkit;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.services.modifications.IModificationQueue;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.wands.IWand;
import network.darkhelmet.prism.api.services.wands.WandMode;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.services.messages.MessageService;
import network.darkhelmet.prism.services.translation.TranslationKey;

import org.bukkit.command.CommandSender;

public class RollbackWand implements IWand {
    /**
     * The storage adapter.
     */
    private final IStorageAdapter storageAdapter;

    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The modification queue service.
     */
    private final IModificationQueueService modificationQueueService;

    /**
     * The owner.
     */
    private Object owner;

    /**
     * Construct a new inspection wand.
     *
     * @param storageAdapter The storage adapter
     * @param messageService The message service
     * @param modificationQueueService The modification queue service
     */
    @Inject
    public RollbackWand(
            IStorageAdapter storageAdapter,
            MessageService messageService,
            IModificationQueueService modificationQueueService) {
        this.storageAdapter = storageAdapter;
        this.messageService = messageService;
        this.modificationQueueService = modificationQueueService;
    }

    @Override
    public WandMode mode() {
        return WandMode.ROLLBACK;
    }

    @Override
    public void setOwner(Object owner) {
        this.owner = owner;
    }

    @Override
    public void use(WorldCoordinate at) {
        // Ensure a queue is free
        if (!modificationQueueService.queueAvailable()) {
            messageService.error((CommandSender) owner, new TranslationKey("queue-not-free"));

            return;
        }

        final ActivityQuery query = new ActivityQuery().location(at).limit(1);

        PrismBukkit.newChain().asyncFirst(() -> {
            try {
                return storageAdapter.queryActivities(query);
            } catch (Exception e) {
                messageService.error((CommandSender) owner, new TranslationKey("query-error"));
                PrismBukkit.getInstance().handleException(e);
            }

            return null;
        }).abortIfNull().<List<IAction>>sync(results -> {
            IModificationQueue queue = modificationQueueService.newRollbackQueue(owner, results);
            queue.apply();

            return null;
        }).execute();
    }
}
