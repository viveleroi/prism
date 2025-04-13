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
import dev.triumphteam.cmd.core.annotations.Command;

import java.util.Locale;
import java.util.Optional;

import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResultStatus;
import network.darkhelmet.prism.services.messages.MessageService;

import org.bukkit.command.CommandSender;

@Command(value = "prism", alias = {"pr"})
public class ReportCommand {
    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The modification queue service.
     */
    private final IModificationQueueService modificationQueueService;

    /**
     * Construct the about command.
     *
     * @param messageService The message service
     * @param modificationQueueService The modification queue service
     */
    @Inject
    public ReportCommand(
            MessageService messageService,
            IModificationQueueService modificationQueueService) {
        this.messageService = messageService;
        this.modificationQueueService = modificationQueueService;
    }

    @Command("report")
    @Permission("prism.modify")
    public class ReportSubCommand {
        /**
         * Run the modification skips report command.
         *
         * @param sender The command sender
         */
        @Command("skips")
        public void onReport(final CommandSender sender) {
            Optional<ModificationQueueResult> resultOptional = modificationQueueService.queueResultForOwner(sender);
            if (resultOptional.isEmpty()) {
                messageService.errorQueueResultMissing(sender);

                return;
            }

            ModificationQueueResult queueResult = resultOptional.get();

            messageService.modificationsReportSkippedHeader(sender);

            for (ModificationResult result : queueResult.results()) {
                if (result.status().equals(ModificationResultStatus.SKIPPED)) {
                    messageService.modificationsReportSkippedActivity(
                        sender, result.activity(), result.skipReason().toString()
                            .replaceAll("_", " ").toLowerCase(Locale.ROOT));
                }
            }
        }
    }
}
