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
import org.bukkit.command.CommandSender;
import org.prism_mc.prism.paper.services.messages.MessageService;
import org.prism_mc.prism.paper.services.web.WebService;

@Command(value = "prism", alias = { "pr" })
public class WebCommand {

    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The web service.
     */
    private final WebService webService;

    /**
     * Constructor.
     *
     * @param messageService The message service
     * @param webService The web service
     */
    @Inject
    public WebCommand(MessageService messageService, WebService webService) {
        this.messageService = messageService;
        this.webService = webService;
    }

    @Command("web")
    @Permission("prism.admin")
    public class WebSubCommand {

        /**
         * Start the web server.
         *
         * @param sender The command sender
         */
        @Command("start")
        public void onWebStart(final CommandSender sender) {
            switch (webService.start()) {
                case STARTED -> messageService.webStarted(sender);
                case ALREADY_RUNNING -> messageService.webAlreadyRunning(sender);
                case DISABLED -> messageService.errorWebDisabled(sender);
                case NO_API_KEY -> messageService.errorWebNoApiKey(sender);
                case FAILED -> messageService.errorWebStartFailed(sender);
            }
        }

        /**
         * Stop the web server.
         *
         * @param sender The command sender
         */
        @Command("stop")
        public void onWebStop(final CommandSender sender) {
            if (webService.stop()) {
                messageService.webStopped(sender);
            } else {
                messageService.errorWebNotRunning(sender);
            }
        }
    }
}
