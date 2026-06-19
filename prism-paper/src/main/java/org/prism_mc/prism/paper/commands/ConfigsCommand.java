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

package org.prism_mc.prism.paper.commands;

import com.google.inject.Inject;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.prism_mc.prism.api.storage.StorageAdapter;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;
import org.prism_mc.prism.loader.services.logging.LoggingService;
import org.prism_mc.prism.paper.permissions.PrismPermissions;
import org.prism_mc.prism.paper.services.alerts.PaperAlertService;
import org.prism_mc.prism.paper.services.filters.PaperFilterService;
import org.prism_mc.prism.paper.services.limits.LimitService;
import org.prism_mc.prism.paper.services.messages.MessageService;
import org.prism_mc.prism.paper.services.translation.PaperTranslationService;

@Command(value = "prism", alias = { "pr" })
public class ConfigsCommand {

    /**
     * The alert service.
     */
    private final PaperAlertService alertService;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The storage service.
     */
    private final StorageAdapter storageAdapter;

    /**
     * The translation service.
     */
    private final PaperTranslationService translationService;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The filter service.
     */
    private final PaperFilterService filterService;

    /**
     * The limit service.
     */
    private final LimitService limitService;

    /**
     * Construct the command.
     *
     * @param loggingService The logging service
     * @param messageService The message service
     * @param storageAdapter The storage adapter
     */
    @Inject
    public ConfigsCommand(
        PaperAlertService alertService,
        LoggingService loggingService,
        MessageService messageService,
        StorageAdapter storageAdapter,
        PaperTranslationService translationService,
        ConfigurationService configurationService,
        PaperFilterService filterService,
        LimitService limitService
    ) {
        this.alertService = alertService;
        this.loggingService = loggingService;
        this.messageService = messageService;
        this.storageAdapter = storageAdapter;
        this.translationService = translationService;
        this.configurationService = configurationService;
        this.filterService = filterService;
        this.limitService = limitService;
    }

    @Command("configs")
    public class ConfigsSubCommand {

        @Command("prism")
        public class ConfigSubCommand {

            /**
             * Reload the config.
             *
             * @param sender The command sender
             */
            @Command("reload")
            @Permission(PrismPermissions.PERM_COMMAND_CONFIGS_RELOAD)
            public void onReloadConfig(final CommandSender sender) {
                configurationService.loadConfigurations();

                filterService.loadFilters();
                alertService.loadAlerts();

                limitService.loadLimits();
                PrismPermissions.registerLimitNodes(
                    Bukkit.getServer().getPluginManager(),
                    limitService.permissionNodes()
                );

                messageService.reloadedConfig(sender);
            }
        }

        @Command("locales")
        public class LocalesSubCommand {

            /**
             * Reload the locale files.
             *
             * @param sender The command sender
             */
            @Command("reload")
            @Permission(PrismPermissions.PERM_COMMAND_CONFIGS_LOCALES_RELOAD)
            public void onReloadLocales(final CommandSender sender) {
                try {
                    translationService.reloadTranslations();

                    messageService.reloadedLocales(sender);
                } catch (IOException e) {
                    messageService.errorReloadLocale(sender);
                    loggingService.handleException(e);
                }
            }
        }

        @Command("storage")
        public class StorageConfigSubCommand {

            /**
             * Run the command to write a hikari properties file.
             *
             * @param sender The command sender
             */
            @Command("write-hikari")
            @Permission(PrismPermissions.PERM_COMMAND_CONFIGS_WRITE_HIKARI)
            public void onHikariWrite(final CommandSender sender) {
                try {
                    storageAdapter.writeHikariPropertiesFile();

                    messageService.hikariFileWritten(sender);
                } catch (IOException e) {
                    messageService.errorWriteHikari(sender);
                    loggingService.handleException(e);
                }
            }
        }
    }
}
