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

package org.prism_mc.prism.paper.permissions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

/**
 * Central registry of Prism's permission nodes.
 *
 * <p>There are two tiers. The fine-grained per-command nodes all live under
 * {@code prism.command.*} (with {@code .parameter.*} / {@code .flag.*} children
 * for query commands). The legacy coarse permissions ({@code prism.lookup},
 * {@code prism.modify}, {@code prism.admin}, …) are kept as parents that grant
 * the relevant {@code prism.command.*} subtree, so servers that don't need
 * fine-grained control keep working exactly as before.
 */
@UtilityClass
public class PrismPermissions {

    // Command paths (the part after "prism.") for the query-style commands whose
    // parameter and flag perms are generated from the registered parsers.
    public static final String PATH_LOOKUP = "command.lookup";
    public static final String PATH_NEAR = "command.near";
    public static final String PATH_PROXIMITY = "command.proximity";
    public static final String PATH_TELEPORT_ID = "command.teleport.id";
    public static final String PATH_TELEPORT_LOC = "command.teleport.loc";
    public static final String PATH_ROLLBACK = "command.rollback";
    public static final String PATH_RESTORE = "command.restore";
    public static final String PATH_PREVIEW_ROLLBACK = "command.preview.rollback";
    public static final String PATH_PREVIEW_RESTORE = "command.preview.restore";
    public static final String PATH_VAULT = "command.vault";
    public static final String PATH_WAND_INSPECT = "command.wand.inspect";
    public static final String PATH_WAND_ROLLBACK = "command.wand.rollback";
    public static final String PATH_WAND_RESTORE = "command.wand.restore";
    public static final String PATH_PURGE_START = "command.purge.start";
    public static final String PATH_PURGE_STOP = "command.purge.stop";

    // Fine-grained per-command permission nodes.
    public static final String PERM_COMMAND_HELP = "prism.command.help";

    public static final String PERM_COMMAND_LOOKUP = "prism." + PATH_LOOKUP;
    public static final String PERM_COMMAND_NEAR = "prism." + PATH_NEAR;
    public static final String PERM_COMMAND_PROXIMITY = "prism." + PATH_PROXIMITY;
    public static final String PERM_COMMAND_TELEPORT = "prism.command.teleport";
    public static final String PERM_COMMAND_TELEPORT_ID = "prism." + PATH_TELEPORT_ID;
    public static final String PERM_COMMAND_TELEPORT_LOC = "prism." + PATH_TELEPORT_LOC;

    public static final String PERM_COMMAND_ROLLBACK = "prism." + PATH_ROLLBACK;
    public static final String PERM_COMMAND_RESTORE = "prism." + PATH_RESTORE;
    public static final String PERM_COMMAND_PREVIEW = "prism.command.preview";
    public static final String PERM_COMMAND_PREVIEW_ROLLBACK = "prism." + PATH_PREVIEW_ROLLBACK;
    public static final String PERM_COMMAND_PREVIEW_RESTORE = "prism." + PATH_PREVIEW_RESTORE;
    public static final String PERM_COMMAND_REPORT = "prism.command.report";
    public static final String PERM_COMMAND_REPORT_QUEUE = "prism.command.report.modification-queue";
    public static final String PERM_COMMAND_REPORT_RECORDING_QUEUE = "prism.command.report.recording-queue";
    public static final String PERM_COMMAND_REPORT_PARTIAL = "prism.command.report.partial";
    public static final String PERM_COMMAND_REPORT_SKIPS = "prism.command.report.skips";

    public static final String PERM_COMMAND_VAULT = "prism." + PATH_VAULT;

    public static final String PERM_COMMAND_WAND = "prism.command.wand";
    public static final String PERM_COMMAND_WAND_INSPECT = "prism." + PATH_WAND_INSPECT;
    public static final String PERM_COMMAND_WAND_ROLLBACK = "prism." + PATH_WAND_ROLLBACK;
    public static final String PERM_COMMAND_WAND_RESTORE = "prism." + PATH_WAND_RESTORE;

    public static final String PERM_COMMAND_DRAIN = "prism.command.drain";
    public static final String PERM_COMMAND_DRAIN_LAVA = "prism.command.drain.lava";
    public static final String PERM_COMMAND_DRAIN_WATER = "prism.command.drain.water";

    public static final String PERM_COMMAND_EXTINGUISH = "prism.command.extinguish";

    public static final String PERM_COMMAND_PURGE = "prism.command.purge";
    public static final String PERM_COMMAND_PURGE_START = "prism." + PATH_PURGE_START;
    public static final String PERM_COMMAND_PURGE_STOP = "prism." + PATH_PURGE_STOP;

    public static final String PERM_COMMAND_CACHE = "prism.command.cache";
    public static final String PERM_COMMAND_CACHE_LIST = "prism.command.cache.list";
    public static final String PERM_COMMAND_CONFIGS = "prism.command.configs";
    public static final String PERM_COMMAND_CONFIGS_RELOAD = "prism.command.configs.reload";
    public static final String PERM_COMMAND_CONFIGS_LOCALES_RELOAD = "prism.command.configs.locales-reload";
    public static final String PERM_COMMAND_CONFIGS_WRITE_HIKARI = "prism.command.configs.write-hikari";
    public static final String PERM_COMMAND_STATUS = "prism.command.status";

    public static final String PERM_COMMAND_AIRTAG = "prism.command.airtag";
    public static final String PERM_COMMAND_AIRTAGS = "prism.command.airtags";
    public static final String PERM_COMMAND_AIRTAGS_VAULT = "prism.command.airtags.vault";
    public static final String PERM_COMMAND_AIRTAGS_OTHERS = "prism.command.airtags.others";

    // The per-player airtag cap nodes (prism.command.airtags.limit.<n>) are dynamic
    // and resolved at runtime; see AirtagService#LIMIT_PERMISSION_PREFIX.

    public static final String PERM_COMMAND_WEB = "prism.command.web";
    public static final String PERM_COMMAND_WEB_START = "prism.command.web.start";
    public static final String PERM_COMMAND_WEB_STOP = "prism.command.web.stop";

    // Legacy / coarse permissions. Each is a parent over the prism.command.*
    // nodes it covered before granular permissions existed.
    public static final String PERM_HELP = "prism.help";
    public static final String PERM_LOOKUP = "prism.lookup";
    public static final String PERM_MODIFY = "prism.modify";
    public static final String PERM_WAND = "prism.wand";
    public static final String PERM_INSPECT = "prism.inspect";
    public static final String PERM_DRAIN = "prism.drain";
    public static final String PERM_EXTINGUISH = "prism.extinguish";
    public static final String PERM_PURGE = "prism.purge";
    public static final String PERM_AIRTAGS = "prism.airtags";
    public static final String PERM_ADMIN = "prism.admin";

    public static final String PERM_ALERTS_BYPASS = "prism.alerts.bypass";

    // Legacy alias for prism.alerts.bypass before it was renamed; kept as a
    // parent so servers that granted the old node keep bypassing alerts.
    public static final String PERM_ALERT_BYPASS_LEGACY = "prism.alert.bypass";

    /**
     * Generate a permission node for a parameter.
     *
     * @param commandPath The command path
     * @param parameterName The parameter name
     * @return Permission node
     */
    public static String parameterPerm(String commandPath, String parameterName) {
        return "prism." + commandPath + ".parameter." + sanitize(parameterName);
    }

    /**
     * Generate a permission node for a flag.
     *
     * @param commandPath The command path
     * @param flagName The flag name
     * @return Permission node
     */
    public static String flagPerm(String commandPath, String flagName) {
        return "prism." + commandPath + ".flag." + sanitize(flagName);
    }

    /**
     * Normalize a parameter or flag name into a permission-node-safe token.
     * Strips the trailing {@code !} used to mark exclude-variant parameters
     * so that a parameter and its negation share a single perm — granting
     * {@code prism.command.lookup.parameter.c} covers both {@code c:} and
     * {@code c!:}.
     *
     * @param name The parameter or flag name
     * @return The sanitized token
     */
    private static String sanitize(String name) {
        return name.replace("!", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Build and register the full permission tree with Bukkit.
     *
     * @param pluginManager The plugin manager
     * @param queryParameterNames Names of every parameter the query parsers recognize.
     */
    public static void register(PluginManager pluginManager, Collection<String> queryParameterNames) {
        Set<String> lookupCmds = new LinkedHashSet<>();
        Set<String> modifyCmds = new LinkedHashSet<>();
        Set<String> wandCmds = new LinkedHashSet<>();
        Set<String> drainCmds = new LinkedHashSet<>();
        Set<String> purgeCmds = new LinkedHashSet<>();
        Set<String> adminCmds = new LinkedHashSet<>();

        register(pluginManager, leaf(PERM_COMMAND_HELP, PermissionDefault.OP));

        // Lookup-family read commands
        registerCommandWithQueryArgs(pluginManager, PATH_LOOKUP, lookupCmds, queryParameterNames);
        registerCommandWithQueryArgs(pluginManager, PATH_NEAR, lookupCmds, queryParameterNames);
        registerCommandWithQueryArgs(pluginManager, PATH_PROXIMITY, lookupCmds, queryParameterNames);

        // Teleport commands
        register(pluginManager, leaf(PERM_COMMAND_TELEPORT_ID, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_TELEPORT_LOC, PermissionDefault.OP));
        Map<String, Boolean> teleportChildren = new LinkedHashMap<>();
        teleportChildren.put(PERM_COMMAND_TELEPORT_ID, true);
        teleportChildren.put(PERM_COMMAND_TELEPORT_LOC, true);
        register(pluginManager, parent(PERM_COMMAND_TELEPORT, PermissionDefault.OP, teleportChildren));
        lookupCmds.add(PERM_COMMAND_TELEPORT);

        // Modify-family commands
        registerCommandWithQueryArgs(pluginManager, PATH_ROLLBACK, modifyCmds, queryParameterNames);
        registerCommandWithQueryArgs(pluginManager, PATH_RESTORE, modifyCmds, queryParameterNames);
        registerCommandWithQueryArgs(pluginManager, PATH_PREVIEW_ROLLBACK, null, queryParameterNames);
        registerCommandWithQueryArgs(pluginManager, PATH_PREVIEW_RESTORE, null, queryParameterNames);

        // Preview parent groups the preview-rollback/restore subcommands
        Map<String, Boolean> previewChildren = new LinkedHashMap<>();
        previewChildren.put(PERM_COMMAND_PREVIEW_ROLLBACK, true);
        previewChildren.put(PERM_COMMAND_PREVIEW_RESTORE, true);
        register(pluginManager, parent(PERM_COMMAND_PREVIEW, PermissionDefault.OP, previewChildren));
        modifyCmds.add(PERM_COMMAND_PREVIEW);

        // Report subcommands
        register(pluginManager, leaf(PERM_COMMAND_REPORT_QUEUE, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_REPORT_RECORDING_QUEUE, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_REPORT_PARTIAL, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_REPORT_SKIPS, PermissionDefault.OP));
        Map<String, Boolean> reportChildren = new LinkedHashMap<>();
        reportChildren.put(PERM_COMMAND_REPORT_QUEUE, true);
        reportChildren.put(PERM_COMMAND_REPORT_RECORDING_QUEUE, true);
        reportChildren.put(PERM_COMMAND_REPORT_PARTIAL, true);
        reportChildren.put(PERM_COMMAND_REPORT_SKIPS, true);
        register(pluginManager, parent(PERM_COMMAND_REPORT, PermissionDefault.OP, reportChildren));
        modifyCmds.add(PERM_COMMAND_REPORT);

        // Vault is a member of the modify family
        registerCommandWithQueryArgs(pluginManager, PATH_VAULT, modifyCmds, queryParameterNames);

        // Wands. prism.command.wand groups the three wand modes
        registerCommandWithQueryArgs(pluginManager, PATH_WAND_INSPECT, wandCmds, queryParameterNames);
        registerCommandWithQueryArgs(pluginManager, PATH_WAND_ROLLBACK, wandCmds, queryParameterNames);
        registerCommandWithQueryArgs(pluginManager, PATH_WAND_RESTORE, wandCmds, queryParameterNames);
        register(pluginManager, parent(PERM_COMMAND_WAND, PermissionDefault.OP, asTrueMap(wandCmds)));

        // Drain. prism.command.drain groups lava/water
        register(pluginManager, leaf(PERM_COMMAND_DRAIN_LAVA, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_DRAIN_WATER, PermissionDefault.OP));
        drainCmds.add(PERM_COMMAND_DRAIN_LAVA);
        drainCmds.add(PERM_COMMAND_DRAIN_WATER);
        register(pluginManager, parent(PERM_COMMAND_DRAIN, PermissionDefault.OP, asTrueMap(drainCmds)));

        // Extinguish
        register(pluginManager, leaf(PERM_COMMAND_EXTINGUISH, PermissionDefault.OP));

        // Purge — a member of the admin family. prism.command.purge groups
        // start/stop, and the start node grants stop (see below).
        Map<String, Boolean> purgeStartChildren = new LinkedHashMap<>();
        for (String param : queryParameterNames) {
            String node = parameterPerm(PATH_PURGE_START, param);
            if (purgeStartChildren.put(node, true) == null) {
                register(pluginManager, leaf(node, PermissionDefault.OP));
            }
        }

        for (var def : PrismFlags.PURGE) {
            String node = flagPerm(PATH_PURGE_START, def.longName());
            if (purgeStartChildren.put(node, true) == null) {
                register(pluginManager, leaf(node, PermissionDefault.OP));
            }
        }

        register(pluginManager, leaf(PERM_COMMAND_PURGE_STOP, PermissionDefault.OP));

        // Stopping a purge is critical to operating one, so anyone who can start
        // a purge can also stop it. Unlike preview-cancel this stays a real node
        // (rather than ungated) because the purge queue is server-wide — an
        // unprivileged player must not be able to abort a running purge.
        purgeStartChildren.put(PERM_COMMAND_PURGE_STOP, true);

        register(pluginManager, parent(PERM_COMMAND_PURGE_START, PermissionDefault.OP, purgeStartChildren));

        Map<String, Boolean> purgeChildren = new LinkedHashMap<>();
        purgeChildren.put(PERM_COMMAND_PURGE_START, true);
        purgeChildren.put(PERM_COMMAND_PURGE_STOP, true);
        register(pluginManager, parent(PERM_COMMAND_PURGE, PermissionDefault.OP, purgeChildren));
        purgeCmds.add(PERM_COMMAND_PURGE);
        adminCmds.add(PERM_COMMAND_PURGE);

        // Admin: cache
        register(pluginManager, leaf(PERM_COMMAND_CACHE_LIST, PermissionDefault.OP));
        Map<String, Boolean> cacheChildren = new LinkedHashMap<>();
        cacheChildren.put(PERM_COMMAND_CACHE_LIST, true);
        register(pluginManager, parent(PERM_COMMAND_CACHE, PermissionDefault.OP, cacheChildren));
        adminCmds.add(PERM_COMMAND_CACHE);

        // Admin: configs
        register(pluginManager, leaf(PERM_COMMAND_CONFIGS_RELOAD, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_CONFIGS_LOCALES_RELOAD, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_CONFIGS_WRITE_HIKARI, PermissionDefault.OP));
        Map<String, Boolean> configsChildren = new LinkedHashMap<>();
        configsChildren.put(PERM_COMMAND_CONFIGS_RELOAD, true);
        configsChildren.put(PERM_COMMAND_CONFIGS_LOCALES_RELOAD, true);
        configsChildren.put(PERM_COMMAND_CONFIGS_WRITE_HIKARI, true);
        register(pluginManager, parent(PERM_COMMAND_CONFIGS, PermissionDefault.OP, configsChildren));
        adminCmds.add(PERM_COMMAND_CONFIGS);

        // Admin: status
        register(pluginManager, leaf(PERM_COMMAND_STATUS, PermissionDefault.OP));
        adminCmds.add(PERM_COMMAND_STATUS);

        // Admin: web server control
        register(pluginManager, leaf(PERM_COMMAND_WEB_START, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_WEB_STOP, PermissionDefault.OP));
        Map<String, Boolean> webChildren = new LinkedHashMap<>();
        webChildren.put(PERM_COMMAND_WEB_START, true);
        webChildren.put(PERM_COMMAND_WEB_STOP, true);
        register(pluginManager, parent(PERM_COMMAND_WEB, PermissionDefault.OP, webChildren));
        adminCmds.add(PERM_COMMAND_WEB);

        // Airtags
        register(pluginManager, leaf(PERM_COMMAND_AIRTAG, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_AIRTAGS, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_AIRTAGS_VAULT, PermissionDefault.OP));
        register(pluginManager, leaf(PERM_COMMAND_AIRTAGS_OTHERS, PermissionDefault.OP));

        // Alerts bypass, with a legacy alias parent so the pre-rename
        // prism.alert.bypass grant still works.
        register(pluginManager, leaf(PERM_ALERTS_BYPASS, PermissionDefault.OP));
        register(
            pluginManager,
            parent(PERM_ALERT_BYPASS_LEGACY, PermissionDefault.OP, asTrueMap(Set.of(PERM_ALERTS_BYPASS)))
        );

        // ── Legacy / coarse parents ──
        // These keep working exactly as before for servers that don't need
        // fine-grained control; each grants the relevant prism.command.* nodes.
        register(pluginManager, parent(PERM_HELP, PermissionDefault.OP, asTrueMap(Set.of(PERM_COMMAND_HELP))));

        // prism.lookup also grants the inspect wand, matching pre-4.x behavior.
        lookupCmds.add(PERM_COMMAND_WAND_INSPECT);
        register(pluginManager, parent(PERM_LOOKUP, PermissionDefault.OP, asTrueMap(lookupCmds)));

        // prism.modify also grants the rollback and restore wands.
        modifyCmds.add(PERM_COMMAND_WAND_ROLLBACK);
        modifyCmds.add(PERM_COMMAND_WAND_RESTORE);
        register(pluginManager, parent(PERM_MODIFY, PermissionDefault.OP, asTrueMap(modifyCmds)));

        register(pluginManager, parent(PERM_WAND, PermissionDefault.OP, asTrueMap(wandCmds)));
        register(
            pluginManager,
            parent(PERM_INSPECT, PermissionDefault.OP, asTrueMap(Set.of(PERM_COMMAND_WAND_INSPECT)))
        );
        register(pluginManager, parent(PERM_DRAIN, PermissionDefault.OP, asTrueMap(drainCmds)));
        register(
            pluginManager,
            parent(PERM_EXTINGUISH, PermissionDefault.OP, asTrueMap(Set.of(PERM_COMMAND_EXTINGUISH)))
        );
        register(pluginManager, parent(PERM_PURGE, PermissionDefault.OP, asTrueMap(purgeCmds)));

        // prism.airtags is the single umbrella permission for airtags: it grants
        // the tag-creating command, the management GUI, and the personal vault.
        // Cross-player ".others" access is intentionally excluded so it must be
        // granted on its own, and the per-player ".limit.<n>" caps are assigned
        // individually rather than granted here.
        register(
            pluginManager,
            parent(
                PERM_AIRTAGS,
                PermissionDefault.OP,
                asTrueMap(Set.of(PERM_COMMAND_AIRTAG, PERM_COMMAND_AIRTAGS, PERM_COMMAND_AIRTAGS_VAULT))
            )
        );

        register(pluginManager, parent(PERM_ADMIN, PermissionDefault.OP, asTrueMap(adminCmds)));
    }

    /**
     * Register custom, config-defined limit permission nodes. These default to
     * false so a limit only applies to players who are explicitly granted its
     * node — ops and unassigned players are never silently capped. Safe to call
     * repeatedly (e.g. on config reload); existing registrations are replaced.
     *
     * @param pluginManager The plugin manager
     * @param nodes The limit permission node names
     */
    public static void registerLimitNodes(PluginManager pluginManager, Collection<String> nodes) {
        for (String node : nodes) {
            Permission existing = pluginManager.getPermission(node);

            // Limit nodes are the only permissions registered with a FALSE default.
            // If a node of this name already exists with any other default it is a
            // built-in (or another plugin's) permission — replacing it would wipe
            // its children and break that command's subtree, so skip it. A prior
            // limit-node registration (FALSE) is safe to replace on reload.
            if (existing != null && existing.getDefault() != PermissionDefault.FALSE) {
                continue;
            }

            register(pluginManager, leaf(node, PermissionDefault.FALSE));
        }
    }

    /**
     * Register every parameter and flag leaf for a query-style command, plus
     * the command's own node which grants them as children.
     *
     * @param pluginManager The plugin manager
     * @param commandPath The command path (e.g. {@code command.lookup})
     * @param cmdNodeSink Optional set to receive the command-node string for later
     *                    aggregation into a coarse grouping parent. May be null.
     * @param queryParameterNames Names of every parameter the query parsers recognize
     */
    private static void registerCommandWithQueryArgs(
        PluginManager pluginManager,
        String commandPath,
        Set<String> cmdNodeSink,
        Collection<String> queryParameterNames
    ) {
        String cmdNode = "prism." + commandPath;
        Map<String, Boolean> children = new LinkedHashMap<>();

        for (String param : queryParameterNames) {
            String node = parameterPerm(commandPath, param);
            if (children.put(node, true) == null) {
                register(pluginManager, leaf(node, PermissionDefault.OP));
            }
        }

        for (var def : PrismFlags.QUERY) {
            String node = flagPerm(commandPath, def.longName());
            if (children.put(node, true) == null) {
                register(pluginManager, leaf(node, PermissionDefault.OP));
            }
        }

        register(pluginManager, parent(cmdNode, PermissionDefault.OP, children));
        if (cmdNodeSink != null) {
            cmdNodeSink.add(cmdNode);
        }
    }

    /**
     * Build a childless permission node.
     *
     * @param name The permission node name
     * @param def The default grant state
     * @return Permission
     */
    private static Permission leaf(String name, PermissionDefault def) {
        return new Permission(name, def);
    }

    /**
     * Build a permission node that grants the given children when held.
     *
     * @param name The permission node name
     * @param def The default grant state
     * @param children Child nodes mapped to the value granted when this node is held
     * @return Permission
     */
    private static Permission parent(String name, PermissionDefault def, Map<String, Boolean> children) {
        return new Permission(name, def, children);
    }

    /**
     * Build a child map that grants each of the given nodes when the owning parent is held.
     *
     * @param nodes The child permission nodes
     * @return Each node mapped to {@code true}
     */
    private static Map<String, Boolean> asTrueMap(Set<String> nodes) {
        Map<String, Boolean> map = new LinkedHashMap<>(nodes.size());
        for (String node : nodes) {
            map.put(node, true);
        }

        return map;
    }

    /**
     * Register a permission, replacing any pre-existing registration.
     * The replacement path matters on plugin reload — Bukkit throws if the same name
     * is added twice without removal.
     *
     * @param pluginManager The plugin manager
     * @param permission The permission to register
     */
    private static void register(PluginManager pluginManager, Permission permission) {
        Permission existing = pluginManager.getPermission(permission.getName());
        if (existing != null) {
            pluginManager.removePermission(existing);
        }

        pluginManager.addPermission(permission);
    }
}
