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

package network.darkhelmet.prism.bukkit.commands;

import com.google.inject.Inject;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.CommandFlags;
import dev.triumphteam.cmd.core.annotations.NamedArguments;
import dev.triumphteam.cmd.core.argument.keyed.Arguments;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.bukkit.services.lookup.LookupService;
import network.darkhelmet.prism.bukkit.services.query.QueryService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.command.CommandSender;

@Command(value = "prism", alias = {"pr"})
public class LookupCommand {
    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The query service.
     */
    private final QueryService queryService;

    /**
     * The lookup service.
     */
    private final LookupService lookupService;

    /**
     * Construct the lookup command.
     *
     * @param configurationService The configuration service
     * @param queryService The query service
     * @param lookupService The lookup service
     */
    @Inject
    public LookupCommand(
            ConfigurationService configurationService,
            QueryService queryService,
            LookupService lookupService) {
        this.configurationService = configurationService;
        this.queryService = queryService;
        this.lookupService = lookupService;
    }

    /**
     * Run a lookup.
     *
     * @param sender The command sender
     * @param arguments The arguments
     */
    @CommandFlags(key = "query-flags")
    @NamedArguments("query-parameters")
    @Command(value = "lookup", alias = {"l"})
    @Permission("prism.lookup")
    public void onLookup(final CommandSender sender, final Arguments arguments) {
        var builder = queryService.queryFromArguments(sender, arguments);
        if (builder.isPresent()) {
            final ActivityQuery query = builder.get()
                .limit(configurationService.prismConfig().defaults().perPage()).build();
            lookupService.lookup(sender, query);
        }
    }
}