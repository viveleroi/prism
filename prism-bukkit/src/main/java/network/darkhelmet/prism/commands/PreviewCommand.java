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
import java.util.Optional;

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.services.modifications.IModificationQueue;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.IPreviewable;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.providers.TaskChainProvider;
import network.darkhelmet.prism.services.messages.MessageService;
import network.darkhelmet.prism.services.modifications.Restore;
import network.darkhelmet.prism.services.modifications.Rollback;
import network.darkhelmet.prism.services.query.QueryService;

import org.bukkit.entity.Player;

@Command(value = "prism", alias = {"pr"})
public class PreviewCommand extends BaseCommand {
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
     * @param taskChainProvider The taskchain provider
     * @param loggingService The logging service
     */
    @Inject
    public PreviewCommand(
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
     * Run the preview apply command.
     *
     * @param player The player
     */
    @SubCommand(value = "preview-apply")
    public void onApply(final Player player) {
        Optional<IModificationQueue> optionalQueue = modificationQueueService.currentQueueForOwner(player);
        if (optionalQueue.isEmpty()) {
            messageService.errorQueueMissing(player);

            return;
        }

        optionalQueue.get().apply();

        messageService.previewApplied(player);
    }

    /**
     * Run the preview cancel command.
     *
     * @param player The player
     */
    @SubCommand(value = "preview-cancel")
    public void onCancel(final Player player) {
        Optional<IModificationQueue> optionalQueue = modificationQueueService.currentQueueForOwner(player);
        if (optionalQueue.isEmpty()) {
            messageService.errorQueueMissing(player);

            return;
        }

        modificationQueueService.cancelQueueForOwner(player);

        messageService.previewCancelled(player);
    }

    /**
     * Run the preview restore command.
     *
     * @param player The player
     * @param arguments The arguments
     */
    @NamedArguments("params")
    @SubCommand(value = "preview-restore", alias = {"prs"})
    @Permission("prism.admin")
    public void onPreviewRestore(final Player player, final Arguments arguments) {
        Optional<ActivityQuery.ActivityQueryBuilder> builder = queryService.queryFromArguments(player, arguments);
        if (builder.isPresent()) {
            final ActivityQuery query = builder.get().modification().reversed(true).build();

            preview(Restore.class, player, query);
        }
    }

    /**
     * Run the preview rollback command.
     *
     * @param player The player
     * @param arguments The arguments
     */
    @NamedArguments("params")
    @SubCommand(value = "preview-rollback", alias = {"prb"})
    @Permission("prism.admin")
    public void onPreviewRollback(final Player player, final Arguments arguments) {
        Optional<ActivityQuery.ActivityQueryBuilder> builder = queryService.queryFromArguments(player, arguments);
        if (builder.isPresent()) {
            final ActivityQuery query = builder.get().modification().reversed(false).build();

            preview(Rollback.class, player, query);
        }
    }

    /**
     * Create a new preview.
     *
     * @param clazz The modification queue class we're previewing.
     * @param player The player
     * @param query The query
     */
    protected void preview(Class<? extends IModificationQueue> clazz, final Player player, final ActivityQuery query) {
        // Ensure a queue is free
        if (!modificationQueueService.queueAvailable()) {
            messageService.errorQueueNotFree(player);

            return;
        }

        taskChainProvider.newChain().asyncFirst(() -> {
            try {
                return storageAdapter.queryActivities(query);
            } catch (Exception e) {
                messageService.errorQueryExec(player);
                loggingService.handleException(e);
            }

            return null;
        }).abortIfNull().<List<IAction>>sync(results -> {
            if (results.isEmpty()) {
                messageService.noResults(player);

                return null;
            }

            ModificationRuleset modificationRuleset = configurationService
                .prismConfig().modifications().toRulesetBuilder().build();

            IModificationQueue queue = modificationQueueService
                .newQueue(clazz, modificationRuleset, player, query, results);
            if (queue instanceof IPreviewable previewable) {
                previewable.preview();
            } else {
                messageService.errorNotPreviewable(player);
            }

            return null;
        }).execute();
    }
}