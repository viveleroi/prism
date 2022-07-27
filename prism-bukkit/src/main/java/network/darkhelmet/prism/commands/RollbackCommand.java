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

package network.darkhelmet.prism.commands;

import com.google.inject.Inject;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.NamedArguments;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.argument.named.Arguments;

import java.util.List;

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.providers.TaskChainProvider;
import network.darkhelmet.prism.services.messages.MessageService;
import network.darkhelmet.prism.services.query.QueryService;
import network.darkhelmet.prism.services.translation.TranslationKey;

import org.bukkit.entity.Player;

@Command(value = "prism", alias = {"pr"})
public class RollbackCommand extends BaseCommand {
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
     * The query service.
     */
    private final QueryService queryService;

    /**
     * The task chain provider.
     */
    private final TaskChainProvider taskChainProvider;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * Construct the rollback command.
     *
     * @param storageAdapter The storage adapter
     * @param messageService The message service
     * @param modificationQueueService The modification queue service
     * @param queryService The query service
     * @param taskChainProvider The task chain provider
     * @param loggingService The logging service
     */
    @Inject
    public RollbackCommand(
            IStorageAdapter storageAdapter,
            MessageService messageService,
            IModificationQueueService modificationQueueService,
            QueryService queryService,
            TaskChainProvider taskChainProvider,
            LoggingService loggingService) {
        this.storageAdapter = storageAdapter;
        this.messageService = messageService;
        this.modificationQueueService = modificationQueueService;
        this.queryService = queryService;
        this.taskChainProvider = taskChainProvider;
        this.loggingService = loggingService;
    }

    /**
     * Run the rollback command.
     *
     * @param player The player
     * @param arguments The arguments
     */
    @NamedArguments("params")
    @SubCommand(value = "rollback", alias = {"rb"})
    @Permission("prism.admin")
    public void onRollback(final Player player, final Arguments arguments) {
        // Ensure a queue is free
        if (!modificationQueueService.queueAvailable()) {
            messageService.error(player, new TranslationKey("queue-not-free"));

            return;
        }

        final ActivityQuery query = queryService
            .queryFromArguments(player.getLocation(), arguments).modification().reversed(false).build();
        taskChainProvider.newChain().asyncFirst(() -> {
            try {
                return storageAdapter.queryActivities(query);
            } catch (Exception e) {
                messageService.error(player, new TranslationKey("query-error"));
                loggingService.handleException(e);
            }

            return null;
        }).abortIfNull().<List<IAction>>sync(modifications -> {
            if (modifications.isEmpty()) {
                messageService.noResults(player);

                return null;
            }

            modificationQueueService.newRollbackQueue(player, query, modifications).apply();

            return null;
        }).execute();
    }
}