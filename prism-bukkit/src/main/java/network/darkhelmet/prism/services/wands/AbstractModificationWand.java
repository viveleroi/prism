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

package network.darkhelmet.prism.services.wands;

import com.google.inject.Inject;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.services.modifications.IModificationQueue;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.providers.TaskChainProvider;
import network.darkhelmet.prism.services.messages.MessageService;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractModificationWand {
    /**
     * The configuration service.
     */
    protected final ConfigurationService configurationService;

    /**
     * The storage adapter.
     */
    protected final IStorageAdapter storageAdapter;

    /**
     * The message service.
     */
    protected final MessageService messageService;

    /**
     * The modification queue service.
     */
    protected final IModificationQueueService modificationQueueService;

    /**
     * The task chain provider.
     */
    protected final TaskChainProvider taskChainProvider;

    /**
     * The logging service.
     */
    protected final LoggingService loggingService;

    /**
     * The owner.
     */
    protected Object owner;

    /**
     * Construct a new inspection wand.
     *
     * @param configurationService The configuration service
     * @param storageAdapter The storage adapter
     * @param messageService The message service
     * @param modificationQueueService The modification queue service
     * @param taskChainProvider The task chain provider
     * @param loggingService The logging service
     */
    @Inject
    public AbstractModificationWand(
            ConfigurationService configurationService,
            IStorageAdapter storageAdapter,
            MessageService messageService,
            IModificationQueueService modificationQueueService,
            TaskChainProvider taskChainProvider,
            LoggingService loggingService) {
        this.configurationService = configurationService;
        this.storageAdapter = storageAdapter;
        this.messageService = messageService;
        this.modificationQueueService = modificationQueueService;
        this.taskChainProvider = taskChainProvider;
        this.loggingService = loggingService;
    }

    /**
     * Activate the wand using a given query and modification class.
     *
     * @param query The query
     * @param clazz The modification class
     */
    protected void use(ActivityQuery query, Class<? extends IModificationQueue> clazz) {
        // Ensure a queue is free
        if (!modificationQueueService.queueAvailable()) {
            messageService.errorQueueNotFree((CommandSender) owner);

            return;
        }

        taskChainProvider.newChain().asyncFirst(() -> {
            try {
                return storageAdapter.queryActivities(query);
            } catch (Exception e) {
                messageService.errorQueryExec((CommandSender) owner);
                loggingService.handleException(e);
            }

            return null;
        }).abortIfNull().syncLast(modifications -> {
            if (modifications.isEmpty()) {
                messageService.noResults((Player) owner);

                return;
            }

            ModificationRuleset modificationRuleset = configurationService
                .prismConfig().modifications().toRulesetBuilder().build();

            modificationQueueService.newQueue(clazz, modificationRuleset, owner, query, modifications).apply();
        }).execute();
    }
}
