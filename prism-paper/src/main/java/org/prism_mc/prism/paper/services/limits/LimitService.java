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

package org.prism_mc.prism.paper.services.limits;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;
import org.prism_mc.prism.loader.services.configuration.limits.LimitConfiguration;
import org.prism_mc.prism.loader.services.configuration.limits.LimitScopeConfiguration;
import org.prism_mc.prism.loader.services.logging.LoggingService;
import org.prism_mc.prism.paper.utils.DateUtils;

/**
 * Resolves the permission-gated, opt-in limits on query parameter values.
 *
 * <p>Each configured {@link LimitConfiguration} is tied to a custom permission
 * node. For a given sender and command, the limits from every node the sender
 * holds are merged most-permissively into an {@link EffectiveLimits}.
 */
@Singleton
public class LimitService {

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * The limits loaded from config, with durations pre-parsed.
     */
    private List<LoadedLimit> loadedLimits = List.of();

    /**
     * The permission nodes referenced by the loaded limits.
     */
    private Set<String> permissionNodes = Set.of();

    /**
     * Construct the limit service.
     *
     * @param configurationService The configuration service
     * @param loggingService The logging service
     */
    @Inject
    public LimitService(ConfigurationService configurationService, LoggingService loggingService) {
        this.configurationService = configurationService;
        this.loggingService = loggingService;

        loadLimits();
    }

    /**
     * Reload limits from the configuration.
     */
    public void loadLimits() {
        List<LoadedLimit> loaded = new ArrayList<>();
        Set<String> nodes = new LinkedHashSet<>();

        for (LimitConfiguration config : configurationService.prismConfig().limits()) {
            if (config.permission() == null || config.permission().isBlank()) {
                loggingService.warn("Limit entry skipped: no permission node defined.");

                continue;
            }

            LoadedScope all = loadScope(config.permission(), "all", config.all());

            Map<String, LoadedScope> commands = new LinkedHashMap<>();
            config.commands().forEach((path, scope) -> commands.put(path, loadScope(config.permission(), path, scope)));

            loaded.add(new LoadedLimit(config.permission(), all, commands));
            nodes.add(config.permission());
        }

        this.loadedLimits = loaded;
        this.permissionNodes = Collections.unmodifiableSet(nodes);
    }

    /**
     * The permission nodes referenced by the configured limits.
     *
     * @return The permission node names
     */
    public Set<String> permissionNodes() {
        return permissionNodes;
    }

    /**
     * Compute the effective limits for a sender running a command.
     *
     * @param sender The command sender
     * @param commandPath The permission command path (e.g. "command.lookup"), or null
     * @return The merged limits, or {@link EffectiveLimits#EMPTY}
     */
    public EffectiveLimits effectiveLimits(CommandSender sender, String commandPath) {
        if (commandPath == null || loadedLimits.isEmpty()) {
            return EffectiveLimits.EMPTY;
        }

        Map<String, Integer> max = new HashMap<>();
        Map<String, Integer> min = new HashMap<>();
        Map<String, Long> maxTime = new HashMap<>();
        Map<String, Set<String>> allowed = new HashMap<>();

        for (LoadedLimit limit : loadedLimits) {
            if (!sender.hasPermission(limit.permission())) {
                continue;
            }

            // Within a single node the command-specific scope overrides the "all"
            // scope so an admin can make one command stricter than the baseline;
            // the resulting per-node scope is then merged most-permissively across
            // every node the sender holds.
            mergeScope(overlay(limit.all(), limit.commands().get(commandPath)), max, min, maxTime, allowed);
        }

        if (max.isEmpty() && min.isEmpty() && maxTime.isEmpty() && allowed.isEmpty()) {
            return EffectiveLimits.EMPTY;
        }

        return new EffectiveLimits(max, min, maxTime, allowed);
    }

    /**
     * Merge one scope's constraints into the accumulators using most-permissive rules.
     */
    private static void mergeScope(
        LoadedScope scope,
        Map<String, Integer> max,
        Map<String, Integer> min,
        Map<String, Long> maxTime,
        Map<String, Set<String>> allowed
    ) {
        if (scope == null) {
            return;
        }

        scope.max().forEach((parameter, value) -> max.merge(parameter, value, Math::max));
        scope.min().forEach((parameter, value) -> min.merge(parameter, value, Math::min));
        scope.maxTimeSeconds().forEach((parameter, value) -> maxTime.merge(parameter, value, Math::max));
        scope
            .allowed()
            .forEach((parameter, values) -> allowed.computeIfAbsent(parameter, key -> new HashSet<>()).addAll(values));
    }

    /**
     * Overlay a command-specific scope on top of a baseline scope, letting the
     * command scope replace (and therefore tighten) any constraint the baseline
     * defines for the same parameter. A null override yields the baseline unchanged.
     *
     * @param base The baseline ("all") scope
     * @param override The command-specific scope, or null
     * @return The combined scope
     */
    private static LoadedScope overlay(LoadedScope base, LoadedScope override) {
        if (override == null) {
            return base;
        }

        Map<String, Integer> max = new LinkedHashMap<>(base.max());
        max.putAll(override.max());

        Map<String, Integer> min = new LinkedHashMap<>(base.min());
        min.putAll(override.min());

        Map<String, Long> maxTime = new LinkedHashMap<>(base.maxTimeSeconds());
        maxTime.putAll(override.maxTimeSeconds());

        Map<String, Set<String>> allowed = new LinkedHashMap<>(base.allowed());
        allowed.putAll(override.allowed());

        return new LoadedScope(max, min, maxTime, allowed);
    }

    /**
     * Convert a configured scope into a loaded scope, parsing durations and
     * normalizing whitelist values. Invalid durations are logged and skipped.
     */
    private LoadedScope loadScope(String permission, String scopeName, LimitScopeConfiguration config) {
        if (config == null) {
            return LoadedScope.EMPTY;
        }

        Map<String, Integer> max = new LinkedHashMap<>();
        Map<String, Integer> min = new LinkedHashMap<>();
        config
            .numeric()
            .forEach((parameter, numeric) -> {
                if (numeric == null) {
                    return;
                }
                if (numeric.max() != null) {
                    max.put(sanitize(parameter), numeric.max());
                }
                if (numeric.min() != null) {
                    min.put(sanitize(parameter), numeric.min());
                }
            });

        Map<String, Long> maxTime = new LinkedHashMap<>();
        config
            .maxTime()
            .forEach((parameter, duration) -> {
                Long seconds = DateUtils.parseDurationSeconds(duration);
                if (seconds == null) {
                    loggingService.warn(
                        "Limit {0} ({1}): invalid max-time \"{2}\" for parameter {3}, skipping.",
                        permission,
                        scopeName,
                        duration,
                        parameter
                    );
                } else {
                    maxTime.put(sanitize(parameter), seconds);
                }
            });

        Map<String, Set<String>> allowed = new LinkedHashMap<>();
        config
            .allowed()
            .forEach((parameter, values) -> {
                Set<String> normalized = new HashSet<>();
                for (String value : values) {
                    normalized.add(value.toLowerCase(Locale.ROOT));
                }
                allowed.put(sanitize(parameter), normalized);
            });

        return new LoadedScope(max, min, maxTime, allowed);
    }

    /**
     * Normalize a configured parameter key to the same base token the parsers
     * look up by: the trailing {@code !} that marks an exclude-variant is
     * stripped and the name is lowercased, so {@code a} and {@code a!} share a
     * single constraint just as they share a single permission node.
     *
     * @param parameter The configured parameter key
     * @return The normalized key
     */
    private static String sanitize(String parameter) {
        return parameter.replace("!", "").toLowerCase(Locale.ROOT);
    }

    /**
     * A configured limit with its durations pre-parsed.
     */
    private record LoadedLimit(String permission, LoadedScope all, Map<String, LoadedScope> commands) {}

    /**
     * One scope's resolved constraints.
     */
    private record LoadedScope(
        Map<String, Integer> max,
        Map<String, Integer> min,
        Map<String, Long> maxTimeSeconds,
        Map<String, Set<String>> allowed
    ) {
        static final LoadedScope EMPTY = new LoadedScope(Map.of(), Map.of(), Map.of(), Map.of());
    }
}
