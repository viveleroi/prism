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

package org.prism_mc.prism.paper.listeners.command;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.prism_mc.prism.paper.permissions.PrismFlags;
import org.prism_mc.prism.paper.permissions.PrismPermissions;

/**
 * Hides parameter and flag suggestions the sender cannot use.
 *
 * <p>TriumphTeam's NamedInternalArgument has no public hook for permission-
 * aware suggestion filtering, so we intercept the suggestion list after the
 * library has produced it. We only touch completions for prism's own command
 * tree, and only filter parameter-name (ending in {@code :}) and flag
 * (starting with {@code -} or {@code --}) tokens — value completions for
 * an already-typed parameter look like plain words and pass through
 * untouched.
 */
@Singleton
public class PrismTabCompleteListener implements Listener {

    /**
     * Subcommand path → permission command path. Order matters: longer paths
     * (e.g. {@code wand inspect}) must match before their prefixes
     * ({@code wand}) so the more specific entry wins.
     */
    private static final List<Map.Entry<String, String>> SUBCOMMAND_PATHS = List.of(
        Map.entry("teleport id", PrismPermissions.PATH_TELEPORT_ID),
        Map.entry("teleport loc", PrismPermissions.PATH_TELEPORT_LOC),
        Map.entry("wand inspect", PrismPermissions.PATH_WAND_INSPECT),
        Map.entry("wand rollback", PrismPermissions.PATH_WAND_ROLLBACK),
        Map.entry("wand restore", PrismPermissions.PATH_WAND_RESTORE),
        Map.entry("purge start", PrismPermissions.PATH_PURGE_START),
        Map.entry("purge stop", PrismPermissions.PATH_PURGE_STOP),
        Map.entry("lookup", PrismPermissions.PATH_LOOKUP),
        Map.entry("l", PrismPermissions.PATH_LOOKUP),
        Map.entry("near", PrismPermissions.PATH_NEAR),
        Map.entry("proximity", PrismPermissions.PATH_PROXIMITY),
        Map.entry("prox", PrismPermissions.PATH_PROXIMITY),
        Map.entry("rollback", PrismPermissions.PATH_ROLLBACK),
        Map.entry("rb", PrismPermissions.PATH_ROLLBACK),
        Map.entry("restore", PrismPermissions.PATH_RESTORE),
        Map.entry("rs", PrismPermissions.PATH_RESTORE),
        Map.entry("preview-rollback", PrismPermissions.PATH_PREVIEW_ROLLBACK),
        Map.entry("prb", PrismPermissions.PATH_PREVIEW_ROLLBACK),
        Map.entry("preview-restore", PrismPermissions.PATH_PREVIEW_RESTORE),
        Map.entry("prs", PrismPermissions.PATH_PREVIEW_RESTORE),
        Map.entry("vault", PrismPermissions.PATH_VAULT),
        Map.entry("v", PrismPermissions.PATH_VAULT),
        // Bare /pr wand activates inspect mode, so completions there should
        // be gated against the inspect wand's parameter perms.
        Map.entry("wand", PrismPermissions.PATH_WAND_INSPECT)
    );

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent event) {
        String commandPath = commandPathFor(event.getBuffer());
        if (commandPath == null) {
            return;
        }

        CommandSender sender = event.getSender();
        List<String> filtered = new ArrayList<>(event.getCompletions().size());
        for (String completion : event.getCompletions()) {
            if (isAuthorized(sender, commandPath, completion)) {
                filtered.add(completion);
            }
        }

        if (filtered.size() != event.getCompletions().size()) {
            event.setCompletions(filtered);
        }
    }

    /**
     * Parse the typed buffer and return the permission command path for the
     * prism subcommand the user is completing, or null if this is not a
     * prism command or the subcommand is unknown.
     */
    private static String commandPathFor(String buffer) {
        if (buffer == null || buffer.isEmpty()) {
            return null;
        }

        // Strip the leading "/" and lowercase for matching.
        String trimmed = (buffer.startsWith("/") ? buffer.substring(1) : buffer).toLowerCase(Locale.ROOT);

        // Cheap prism-root guard before the regex split — this listener fires for
        // every command's completions server-wide, so bail on foreign commands
        // before doing any real work.
        if (!trimmed.startsWith("prism ") && !trimmed.startsWith("pr ")) {
            return null;
        }

        String[] tokens = trimmed.split("\\s+");
        if (tokens.length < 2) {
            return null;
        }

        // Try the longest possible subcommand match first.
        String twoToken = tokens.length >= 3 ? tokens[1] + " " + tokens[2] : null;
        String oneToken = tokens[1];

        for (var entry : SUBCOMMAND_PATHS) {
            if (twoToken != null && entry.getKey().equals(twoToken)) {
                return entry.getValue();
            }
        }
        for (var entry : SUBCOMMAND_PATHS) {
            if (entry.getKey().equals(oneToken)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Decide whether a single completion entry should survive the filter.
     * Plain values (no leading {@code -}, no trailing {@code :}) are always
     * kept; only parameter-name and flag tokens are checked against perms.
     */
    private static boolean isAuthorized(CommandSender sender, String commandPath, String completion) {
        if (completion == null || completion.isEmpty()) {
            return true;
        }

        // Flag completion: "-c" or "--count"
        if (completion.startsWith("-")) {
            String flagToken = completion.startsWith("--") ? completion.substring(2) : completion.substring(1);
            // Some flags accept values and may surface as "--sort=" — strip anything after = or :
            int sep = indexOfFirst(flagToken, '=', ':');
            if (sep >= 0) {
                flagToken = flagToken.substring(0, sep);
            }
            if (flagToken.isEmpty()) {
                return true;
            }
            String longName = PrismFlags.resolveLongName(flagToken);
            return sender.hasPermission(PrismPermissions.flagPerm(commandPath, longName));
        }

        // Parameter-name completion: ends with ":" and has no other ":" before it
        // (a value completion like "world:overworld" would have already had the
        // ":" typed by the user, so it'd come through as just "overworld")
        if (completion.endsWith(":") && completion.indexOf(':') == completion.length() - 1) {
            String paramName = completion.substring(0, completion.length() - 1);
            return sender.hasPermission(PrismPermissions.parameterPerm(commandPath, paramName));
        }

        return true;
    }

    private static int indexOfFirst(String s, char a, char b) {
        int ia = s.indexOf(a);
        int ib = s.indexOf(b);
        if (ia < 0) {
            return ib;
        }
        if (ib < 0) {
            return ia;
        }
        return Math.min(ia, ib);
    }
}
