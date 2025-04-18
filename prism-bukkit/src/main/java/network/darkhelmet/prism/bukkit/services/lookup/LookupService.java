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

package network.darkhelmet.prism.bukkit.services.lookup;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import network.darkhelmet.prism.api.PaginatedResults;
import network.darkhelmet.prism.api.activities.AbstractActivity;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.storage.StorageAdapter;
import network.darkhelmet.prism.bukkit.providers.TaskChainProvider;
import network.darkhelmet.prism.bukkit.services.messages.MessageService;
import network.darkhelmet.prism.bukkit.services.translation.BukkitTranslationService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.configuration.cache.CacheConfiguration;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.bukkit.command.CommandSender;

@Singleton
public class LookupService {
    /**
     * The bukkit audiences.
     */
    private final BukkitAudiences audiences;

    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The storage adapter.
     */
    private final StorageAdapter storageAdapter;

    /**
     * The translation service.
     */
    private final BukkitTranslationService translationService;

    /**
     * The task chain provider.
     */
    private final TaskChainProvider taskChainProvider;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * Cache recent queries.
     */
    private final Cache<CommandSender, ActivityQuery> recentQueries;

    /**
     * Construct the lookup service.
     *
     * @param audiences The bukkit audiences
     * @param configurationService The configuration service
     * @param messageService The message service
     * @param storageAdapter The storage adapter
     * @param translationService The translation service
     * @param taskChainProvider The task chain provider
     * @param loggingService The logging service
     */
    @Inject
    public LookupService(
            BukkitAudiences audiences,
            ConfigurationService configurationService,
            MessageService messageService,
            StorageAdapter storageAdapter,
            BukkitTranslationService translationService,
            TaskChainProvider taskChainProvider,
            LoggingService loggingService) {
        this.audiences = audiences;
        this.messageService = messageService;
        this.storageAdapter = storageAdapter;
        this.translationService = translationService;
        this.taskChainProvider = taskChainProvider;
        this.loggingService = loggingService;

        CacheConfiguration cacheConfiguration = configurationService.prismConfig().cache();

        recentQueries = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.lookupExpiration().maxSize())
            .expireAfterAccess(cacheConfiguration.lookupExpiration().expiresAfterAccess().duration(),
                cacheConfiguration.lookupExpiration().expiresAfterAccess().timeUnit())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting activity query from cache: Key: {0}, Value: {1}, Removal Cause: {2}";
                loggingService.debug(msg, key, value, cause);
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing activity query from cache: Key: {0} Value: {1}, Removal Cause: {2}";
                loggingService.debug(msg, key, value, cause);
            }).build();
    }

    /**
     * Get the last query for a command sender.
     *
     * @param sender The sender
     * @return The last query, if any
     */
    public Optional<ActivityQuery> lastQuery(CommandSender sender) {
        return Optional.ofNullable(recentQueries.getIfPresent(sender));
    }

    /**
     * Performs an async storage query and displays the results to the command sender in a paginated chat view.
     *
     * @param sender The command sender
     * @param query The activity query
     */
    public void lookup(CommandSender sender, ActivityQuery query) {
        taskChainProvider.newChain().async(() -> {
            try {
                show(sender, storageAdapter.queryActivitiesPaginated(query), query);

                // Cache this senders' most recent query
                recentQueries.put(sender, query);
            } catch (Exception ex) {
                messageService.errorQueryExec(sender);
                loggingService.handleException(ex);
            }
        }).execute();
    }

    /**
     * Performs an async storage query and passes the result to the consumer.
     *
     * @param sender The command sender
     * @param query The activity query
     */
    public void lookup(CommandSender sender, ActivityQuery query, Consumer<List<Activity>> consumer) {
        taskChainProvider.newChain().async(() -> {
            try {
                consumer.accept(storageAdapter.queryActivities(query));

                // Cache this senders' most recent query
                recentQueries.put(sender, query);
            } catch (Exception ex) {
                messageService.errorQueryExec(sender);
                loggingService.handleException(ex);
            }
        }).execute();
    }

    /**
     * Display paginated results to a command sender.
     *
     * @param sender The command sender
     * @param results The paginated results
     * @param query The original query
     */
    private void show(CommandSender sender, PaginatedResults<AbstractActivity> results, ActivityQuery query) {
        messageService.paginationHeader(sender, results);

        if (!query.defaultsUsed().isEmpty()) {
            messageService.defaultsUsed(sender, String.join(" ", query.defaultsUsed()));
        }

        if (results.isEmpty()) {
            messageService.noResults(sender);
        } else {
            for (var activity : results.results()) {
                if (query.grouped()) {
                    if (activity.action().descriptor() != null) {
                        messageService.listActivityRowGrouped(sender, activity);
                    } else {
                        messageService.listActivityRowGroupedNoDescriptor(sender, activity);
                    }
                } else {
                    if (activity.action().descriptor() != null) {
                        messageService.listActivityRowSingle(sender, activity);
                    } else {
                        messageService.listActivityRowSingleNoDescriptor(sender, activity);
                    }
                }
            }

            if (results.hasPrevPage() || results.hasNextPage()) {
                Component prev = Component.empty();
                if (results.hasPrevPage()) {
                    String cmd = "/pr page " + (results.currentPage() - 1);

                    Component hover = Component.text(
                        translationService.messageOf(sender, "text.page-prev-hover"));
                    prev = MiniMessage.miniMessage().deserialize(
                            translationService.messageOf(sender, "rich.page-prev"))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
                }

                Component splitter = MiniMessage.miniMessage().deserialize(
                    translationService.messageOf(sender, "rich.page-separator"));

                Component next = Component.empty();
                if (results.hasNextPage()) {
                    String cmd = "/pr page " + (results.currentPage() + 1);

                    Component hover = Component.text(
                        translationService.messageOf(sender, "text.page-next-hover"));
                    next = MiniMessage.miniMessage().deserialize(
                            translationService.messageOf(sender, "rich.page-next"))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
                }

                audiences.sender(sender).sendMessage(prev.append(splitter).append(next));
            }
        }
    }
}
