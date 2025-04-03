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

package network.darkhelmet.prism.commands;

import com.google.inject.Inject;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.NamedArguments;
import dev.triumphteam.cmd.core.argument.keyed.Arguments;

import java.util.Optional;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.providers.TaskChainProvider;
import network.darkhelmet.prism.services.messages.MessageService;
import network.darkhelmet.prism.services.query.QueryService;

import org.bukkit.command.CommandSender;

@Command(value = "prism", alias = {"pr"})
public class RollbackCommand {
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
     * @param configurationService The configuration service
     * @param storageAdapter The storage adapter
     * @param messageService The message service
     * @param modificationQueueService The modification queue service
     * @param queryService The query service
     * @param taskChainProvider The task chain provider
     * @param loggingService The logging service
     */
    @Inject
    public RollbackCommand(
            ConfigurationService configurationService,
            IStorageAdapter storageAdapter,
            MessageService messageService,
            IModificationQueueService modificationQueueService,
            QueryService queryService,
            TaskChainProvider taskChainProvider,
            LoggingService loggingService) {
        this.configurationService = configurationService;
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
     * @param sender The command sender
     * @param arguments The arguments
     */
    @NamedArguments("query-parameters")
    @Command(value = "rollback", alias = {"rb"})
    @Permission("prism.admin")
    public void onRollback(final CommandSender sender, final Arguments arguments) {
        // Ensure a queue is free
        if (!modificationQueueService.queueAvailable()) {
            messageService.errorQueueNotFree(sender);

            return;
        }

        Optional<ActivityQuery.ActivityQueryBuilder> builder = queryService.queryFromArguments(sender, arguments);
        if (builder.isPresent()) {
            final ActivityQuery query = builder.get().rollback().build();
            taskChainProvider.newChain().asyncFirst(() -> {
                try {
                    return storageAdapter.queryActivities(query);
                } catch (Exception e) {
                    messageService.errorQueryExec(sender);
                    loggingService.handleException(e);
                }

                return null;
            }).abortIfNull().syncLast(modifications -> {
                if (modifications.isEmpty()) {
                    messageService.noResults(sender);

                    return;
                }

                ModificationRuleset modificationRuleset = configurationService
                    .prismConfig().modifications().toRulesetBuilder().build();

                modificationQueueService.newRollbackQueue(modificationRuleset, sender, query, modifications).apply();
            }).execute();
        }
    }
}