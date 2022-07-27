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

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.api.services.wands.IWand;
import network.darkhelmet.prism.api.services.wands.WandMode;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.providers.TaskChainProvider;
import network.darkhelmet.prism.services.messages.MessageService;
import network.darkhelmet.prism.services.translation.TranslationKey;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RestoreWand implements IWand {
    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

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
     * The task chain provider.
     */
    private final TaskChainProvider taskChainProvider;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * The owner.
     */
    private Object owner;

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
    public RestoreWand(
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

    @Override
    public WandMode mode() {
        return WandMode.RESTORE;
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

        final ActivityQuery query = ActivityQuery.builder().location(at).limit(1).reversed(true).build();

        taskChainProvider.newChain().asyncFirst(() -> {
            try {
                return storageAdapter.queryActivities(query);
            } catch (Exception e) {
                messageService.error((CommandSender) owner, new TranslationKey("query-error"));
                loggingService.handleException(e);
            }

            return null;
        }).abortIfNull().<List<IAction>>sync(modifications -> {
            if (modifications.isEmpty()) {
                messageService.noResults((Player) owner);

                return null;
            }

            ModificationRuleset modificationRuleset = configurationService
                .prismConfig().modifications().toRulesetBuilder().build();

            modificationQueueService.newRestoreQueue(modificationRuleset, owner, query, modifications).apply();

            return null;
        }).execute();
    }
}
