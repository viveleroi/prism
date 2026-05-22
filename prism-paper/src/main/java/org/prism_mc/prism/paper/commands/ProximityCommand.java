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
import dev.triumphteam.cmd.core.annotations.CommandFlags;
import dev.triumphteam.cmd.core.annotations.NamedArguments;
import dev.triumphteam.cmd.core.argument.keyed.Arguments;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.prism_mc.prism.api.activities.Activity;
import org.prism_mc.prism.api.activities.ActivityQuery;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;
import org.prism_mc.prism.paper.api.activities.PaperActivityQuery;
import org.prism_mc.prism.paper.services.lookup.LookupService;
import org.prism_mc.prism.paper.services.messages.MessageService;
import org.prism_mc.prism.paper.services.query.QueryService;
import org.prism_mc.prism.paper.services.scheduling.PrismScheduler;
import org.prism_mc.prism.paper.utils.DateUtils;

@Command(value = "prism", alias = { "pr" })
public class ProximityCommand {

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The lookup service.
     */
    private final LookupService lookupService;

    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The scheduler.
     */
    private final PrismScheduler prismScheduler;

    /**
     * The query service.
     */
    private final QueryService queryService;

    /**
     * Construct the proximity command.
     *
     * @param configurationService The configuration service
     * @param lookupService The lookup service
     * @param messageService The message service
     * @param prismScheduler The scheduler
     * @param queryService The query service
     */
    @Inject
    public ProximityCommand(
        ConfigurationService configurationService,
        LookupService lookupService,
        MessageService messageService,
        PrismScheduler prismScheduler,
        QueryService queryService
    ) {
        this.configurationService = configurationService;
        this.lookupService = lookupService;
        this.messageService = messageService;
        this.prismScheduler = prismScheduler;
        this.queryService = queryService;
    }

    /**
     * Run a proximity lookup.
     *
     * @param sender The command sender
     * @param activityId The anchor activity id
     * @param arguments The arguments
     */
    @CommandFlags(key = "query-flags")
    @NamedArguments("query-parameters")
    @Command(value = "proximity", alias = { "prox" })
    @Permission("prism.lookup")
    public void onProximity(final CommandSender sender, final Integer activityId, final Arguments arguments) {
        var dArg = arguments.getArgument("d", String.class);
        var rArg = arguments.getArgument("r", Integer.class);
        var beforeArg = arguments.getArgument("before", String.class);
        var sinceArg = arguments.getArgument("since", String.class);
        var worldArg = arguments.getArgument("world", String.class);

        if (dArg.isEmpty() && rArg.isEmpty() && beforeArg.isEmpty() && sinceArg.isEmpty()) {
            messageService.errorProximityNoWindow(sender);
            return;
        }

        // Radius is centered on the anchor's coordinate, which is in the anchor's
        // world by definition. Pairing r: with world: would be contradictory.
        if (rArg.isPresent() && worldArg.isPresent()) {
            messageService.errorParamConflict(sender, "r", "world");
            return;
        }

        // Validate any duration values up front
        if (dArg.isPresent() && DateUtils.parseDurationSeconds(dArg.get()) == null) {
            messageService.errorParamInvalidTime(sender);
            return;
        }

        if (beforeArg.isPresent() && DateUtils.parseDurationSeconds(beforeArg.get()) == null) {
            messageService.errorParamInvalidTime(sender);
            return;
        }

        if (sinceArg.isPresent() && DateUtils.parseDurationSeconds(sinceArg.get()) == null) {
            messageService.errorParamInvalidTime(sender);
            return;
        }

        var anchorQuery = PaperActivityQuery.builder()
            .activityId(activityId)
            .lookup(true)
            .grouped(false)
            .limit(1)
            .build();

        lookupService.lookup(sender, anchorQuery, anchors -> {
            if (anchors.isEmpty()) {
                runOnSenderThread(sender, () ->
                    messageService.errorProximityNotFound(sender, String.valueOf(activityId))
                );
                return;
            }

            Activity anchor = anchors.get(0);
            World world = Bukkit.getServer().getWorld(anchor.worldUuid());
            if (world == null) {
                runOnSenderThread(sender, () -> messageService.errorProximityWorldMissing(sender));
                return;
            }

            Location anchorLocation = new Location(
                world,
                anchor.coordinate().intX(),
                anchor.coordinate().intY(),
                anchor.coordinate().intZ()
            );

            runOnSenderThread(sender, () -> finalizeAndRun(sender, arguments, anchor, anchorLocation));
        });
    }

    /**
     * Build the query from arguments with the anchor's location as the
     * reference, apply anchor-relative time bounds, and dispatch the lookup.
     */
    private void finalizeAndRun(CommandSender sender, Arguments arguments, Activity anchor, Location anchorLocation) {
        // Bypass config-driven defaults: proximity already forces an explicit
        // window via d:/r:/before:/since:, and the anchor activity supplies
        // the implicit world/location reference
        var builderOpt = queryService.queryFromArguments(sender, arguments, anchorLocation, true);
        if (builderOpt.isEmpty()) {
            return;
        }

        var builder = builderOpt.get();

        // Default the world filter to the anchor's world
        if (arguments.getArgument("world", String.class).isEmpty()) {
            builder.worldUuid(anchorLocation.getWorld().getUID());
        }

        applyAnchorTimeBounds(arguments, anchor.timestamp(), builder);

        messageService.proximityBasis(sender, anchor);

        final ActivityQuery query = builder.limit(configurationService.prismConfig().defaults().perPage()).build();

        if (query.countOnly()) {
            lookupService.count(sender, query);
        } else {
            lookupService.lookup(sender, query);
        }
    }

    /**
     * Convert anchor-relative d:/before:/since: arguments into absolute
     * before/after timestamps on the builder. The standard Before/Since
     * parsers will have already populated before/after with now-relative
     * values; this overrides them.
     */
    private void applyAnchorTimeBounds(
        Arguments arguments,
        long anchorTimestamp,
        PaperActivityQuery.PaperActivityQueryBuilder<?, ?> builder
    ) {
        var dArg = arguments.getArgument("d", String.class);
        if (dArg.isPresent()) {
            // onContext already validated this parses.
            long radius = DateUtils.parseDurationSeconds(dArg.get());
            builder.before(anchorTimestamp + radius);
            builder.after(anchorTimestamp - radius);
            return;
        }

        var beforeArg = arguments.getArgument("before", String.class);
        var sinceArg = arguments.getArgument("since", String.class);
        Long beforeDur = beforeArg.isPresent() ? DateUtils.parseDurationSeconds(beforeArg.get()) : null;
        Long sinceDur = sinceArg.isPresent() ? DateUtils.parseDurationSeconds(sinceArg.get()) : null;

        if (beforeDur == null && sinceDur == null) {
            // Only r: was provided; leave the now-relative bounds set by the
            // Before/Since parsers (which would be null if user didn't provide
            // those args either).
            return;
        }

        builder.before(sinceDur != null ? anchorTimestamp + sinceDur : anchorTimestamp);
        builder.after(beforeDur != null ? anchorTimestamp - beforeDur : anchorTimestamp);
    }

    /**
     * Schedule a sender-bound task on the correct region thread.
     */
    private void runOnSenderThread(CommandSender sender, Runnable task) {
        if (sender instanceof Player player) {
            prismScheduler.runForEntity(player, task);
        } else {
            prismScheduler.runGlobal(task);
        }
    }
}
