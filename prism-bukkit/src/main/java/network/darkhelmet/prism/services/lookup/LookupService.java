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

package network.darkhelmet.prism.services.lookup;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;

import java.util.Optional;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import network.darkhelmet.prism.api.PaginatedResults;
import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.configuration.cache.CacheConfiguration;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.providers.TaskChainProvider;
import network.darkhelmet.prism.services.messages.MessageService;
import network.darkhelmet.prism.services.translation.TranslationKey;
import network.darkhelmet.prism.services.translation.TranslationService;

import org.bukkit.command.CommandSender;

public class LookupService {
    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The storage adapter.
     */
    private final IStorageAdapter storageAdapter;

    /**
     * The translation service.
     */
    private final TranslationService translationService;

    /**
     * The bukkit audiences.
     */
    private final BukkitAudiences audiences;

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
     * @param configurationService The configuration service
     * @param messageService The message service
     * @param storageAdapter The storage adapter
     * @param translationService The translation service
     * @param audiences The audiences
     * @param taskChainProvider The task chain provider
     * @param loggingService The logging service
     */
    @Inject
    public LookupService(
            ConfigurationService configurationService,
            MessageService messageService,
            IStorageAdapter storageAdapter,
            TranslationService translationService,
            BukkitAudiences audiences,
            TaskChainProvider taskChainProvider,
            LoggingService loggingService) {
        this.messageService = messageService;
        this.storageAdapter = storageAdapter;
        this.translationService = translationService;
        this.audiences = audiences;
        this.taskChainProvider = taskChainProvider;
        this.loggingService = loggingService;

        CacheConfiguration cacheConfiguration = configurationService.prismConfig().cacheConfiguration();

        recentQueries = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.lookupExpiration().maxSize())
            .expireAfterAccess(cacheConfiguration.lookupExpiration().expiresAfterAccess().duration(),
                cacheConfiguration.lookupExpiration().expiresAfterAccess().timeUnit())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting activity query from cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing activity query from cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
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
                show(sender, storageAdapter.queryActivitiesPaginated(query));

                // Cache this senders' most recent query
                recentQueries.put(sender, query);
            } catch (Exception ex) {
                messageService.error(sender, new TranslationKey("query-error"));
                loggingService.handleException(ex);
            }
        }).execute();
    }

    /**
     * Display paginated results to a command sender.
     *
     * @param sender The command sender
     * @param results The paginated results
     */
    private void show(CommandSender sender, PaginatedResults<IActivity> results) {
        messageService.paginationHeader(sender, results);

        if (results.isEmpty()) {
            messageService.noResults(sender);
        } else {
            for (IActivity activity : results.results()) {
                messageService.listActivityRow(sender, activity);
            }

            if (results.hasPrevPage() || results.hasNextPage()) {
                Component prev = Component.empty();
                if (results.hasPrevPage()) {
                    String cmd = "/pr page " + (results.currentPage() - 1);

                    Component hover = Component.text(translationService.messageOf(sender, "page-prev-hover"));
                    String temp = translationService.messageOf(sender, "page-prev");
                    prev = MiniMessage.miniMessage().deserialize(temp)
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
                }

                Component next = Component.empty();
                if (results.hasNextPage()) {
                    String cmd = "/pr page " + (results.currentPage() + 1);

                    Component hover = Component.text(translationService.messageOf(sender, "page-next-hover"));
                    String temp = translationService.messageOf(sender, "page-next");
                    next = MiniMessage.miniMessage().deserialize(temp)
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
                }

                audiences.sender(sender).sendMessage(prev.append(next));
            }
        }
    }
}
