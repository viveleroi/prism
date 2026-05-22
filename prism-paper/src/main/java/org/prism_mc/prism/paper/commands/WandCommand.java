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
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.CommandFlags;
import dev.triumphteam.cmd.core.annotations.NamedArguments;
import dev.triumphteam.cmd.core.argument.keyed.Arguments;
import org.bukkit.entity.Player;
import org.prism_mc.prism.api.activities.ActivityQuery;
import org.prism_mc.prism.api.services.wands.Wand;
import org.prism_mc.prism.api.services.wands.WandMode;
import org.prism_mc.prism.paper.services.messages.MessageService;
import org.prism_mc.prism.paper.services.query.QueryService;
import org.prism_mc.prism.paper.services.wands.WandService;

@Command(value = "prism", alias = { "pr" })
public class WandCommand {

    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The query service.
     */
    private final QueryService queryService;

    /**
     * The wand service.
     */
    private final WandService wandService;

    /**
     * Construct the wand command.
     *
     * @param messageService The message service
     * @param queryService The query service
     * @param wandService The wand service
     */
    @Inject
    public WandCommand(MessageService messageService, QueryService queryService, WandService wandService) {
        this.messageService = messageService;
        this.queryService = queryService;
        this.wandService = wandService;
    }

    @Command("wand")
    public class WandSubCommand {

        /**
         * Bare /pr wand — toggles any active wand off, or activates an inspect wand with the
         * supplied filters. Location parameters are not allowed.
         *
         * @param player The player
         * @param arguments The query parameters
         */
        @CommandFlags(key = "query-flags")
        @NamedArguments("query-parameters")
        @Command(Command.DEFAULT_CMD_NAME)
        public void onWand(final Player player, final Arguments arguments) {
            boolean hasParameters = queryService.hasAnyParameter(arguments);

            if (!hasParameters && wandService.hasActiveWand(player)) {
                wandService.deactivateWand(player);

                return;
            }

            activate(player, WandMode.INSPECT, arguments, hasParameters);
        }

        /**
         * /pr wand inspect [params] — activate the inspect wand with optional filters.
         *
         * @param player The player
         * @param arguments The query parameters
         */
        @CommandFlags(key = "query-flags")
        @NamedArguments("query-parameters")
        @Command("inspect")
        public void onInspect(final Player player, final Arguments arguments) {
            onModeCommand(player, WandMode.INSPECT, arguments);
        }

        /**
         * /pr wand rollback [params] — activate the rollback wand with optional filters.
         *
         * @param player The player
         * @param arguments The query parameters
         */
        @CommandFlags(key = "query-flags")
        @NamedArguments("query-parameters")
        @Command("rollback")
        public void onRollback(final Player player, final Arguments arguments) {
            onModeCommand(player, WandMode.ROLLBACK, arguments);
        }

        /**
         * /pr wand restore [params] — activate the restore wand with optional filters.
         *
         * @param player The player
         * @param arguments The query parameters
         */
        @CommandFlags(key = "query-flags")
        @NamedArguments("query-parameters")
        @Command("restore")
        public void onRestore(final Player player, final Arguments arguments) {
            onModeCommand(player, WandMode.RESTORE, arguments);
        }

        private void onModeCommand(Player player, WandMode wandMode, Arguments arguments) {
            boolean hasParameters = queryService.hasAnyParameter(arguments);

            java.util.Optional<Wand> activeWand = wandService.getWand(player);
            if (activeWand.isPresent() && activeWand.get().mode().equals(wandMode) && !hasParameters) {
                wandService.deactivateWand(player);

                return;
            }

            activate(player, wandMode, arguments, hasParameters);
        }
    }

    /**
     * Validate permissions, parse filter parameters, and activate the wand.
     */
    private void activate(Player player, WandMode wandMode, Arguments arguments, boolean hasParameters) {
        boolean canInspect = player.hasPermission("prism.inspect") || player.hasPermission("prism.lookup");
        boolean canModify = player.hasPermission("prism.modify");

        if (
            (wandMode == WandMode.INSPECT && !canInspect) ||
            (wandMode == WandMode.ROLLBACK && !canModify) ||
            (wandMode == WandMode.RESTORE && !canModify)
        ) {
            messageService.errorInsufficientPermission(player);

            return;
        }

        ActivityQuery activityQuery = null;
        if (hasParameters) {
            var builderOpt = queryService.queryFromArguments(
                player,
                arguments,
                player.getLocation(),
                QueryService.LOCATION_PARSERS
            );
            if (builderOpt.isEmpty()) {
                return;
            }
            activityQuery = builderOpt.get().build();
        }

        wandService.activateWand(player, wandMode, activityQuery);
    }
}
