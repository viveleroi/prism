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

package org.prism_mc.prism.loader.services.configuration;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class DefaultsConfiguration {

    /**
     * Identifies a command that supports its own default overrides.
     */
    public enum CommandType {
        LOOKUP,
        NEAR,

        /**
         * Inspection wands.
         */
        WAND_INSPECT,

        /**
         * Modification (rollback, restore) wands.
         */
        WAND_MODIFICATION,

        /**
         * The modification system (rollback, restore, preview).
         */
        MODIFICATION,
    }

    @Comment("The default radius for the drain command.")
    private int drainRadius = 5;

    @Comment("The default radius for the extinguish command.")
    private int extinguishRadius = 5;

    @Comment(
        """
        The default locale for plugin messages. Messages given to players
        will use their client locale settings."""
    )
    private Locale defaultLocale = Locale.US;

    @Comment(
        """
        Base default parameters applied to every query command (lookups, rollbacks,
        restores, etc.). Per-command sections below are merged on top of these.
        Leave empty for none."""
    )
    private Map<String, String> parameters = new LinkedHashMap<>();

    @Comment(
        """
        Base default flags applied to every query command. Per-command sections below
        are merged on top of these. Leave empty for none."""
    )
    private Map<String, String> flags = new LinkedHashMap<>();

    @Comment("Default parameters and flags for the lookup command, merged over the base.")
    private CommandDefaultsConfiguration lookup = new CommandDefaultsConfiguration();

    @Comment("Default parameters and flags for the near command, merged over the base.")
    private CommandDefaultsConfiguration near = new CommandDefaultsConfiguration();

    @Comment("Default parameters and flags for wands, split by wand category, merged over the base.")
    private WandDefaultsConfiguration wand = new WandDefaultsConfiguration();

    @Comment(
        """
        Default parameters and flags for the modification commands (rollback, restore,
        preview), merged over the base."""
    )
    private CommandDefaultsConfiguration modifications = new CommandDefaultsConfiguration();

    @Comment("Sets the default radius to use when searching for nearby activity.")
    private int nearRadius = 5;

    @Comment("Limits how many results are shown \"per page\" when doing lookups.")
    private int perPage = 10;

    /**
     * Constructor.
     */
    public DefaultsConfiguration() {
        parameters.put("r", "32");
        parameters.put("since", "3d");
    }

    /**
     * Resolve the effective default parameters for a command by merging the base
     * parameters with the command-specific overrides (command wins).
     *
     * @param commandType The command type
     * @return The merged parameters
     */
    public Map<String, String> parameters(CommandType commandType) {
        Map<String, String> resolved = new LinkedHashMap<>(parameters);
        resolved.putAll(commandConfig(commandType).parameters());

        return resolved;
    }

    /**
     * Resolve the effective default flags for a command by merging the base flags
     * with the command-specific overrides (command wins).
     *
     * @param commandType The command type
     * @return The merged flags
     */
    public Map<String, String> flags(CommandType commandType) {
        Map<String, String> resolved = new LinkedHashMap<>(flags);
        resolved.putAll(commandConfig(commandType).flags());

        return resolved;
    }

    /**
     * Get the per-command defaults configuration for a command type.
     *
     * @param commandType The command type
     * @return The command defaults configuration
     */
    private CommandDefaultsConfiguration commandConfig(CommandType commandType) {
        return switch (commandType) {
            case LOOKUP -> lookup;
            case NEAR -> near;
            case WAND_INSPECT -> wand.inspect();
            case WAND_MODIFICATION -> wand.modify();
            case MODIFICATION -> modifications;
        };
    }

    /**
     * Wand default parameters and flags, split by wand category.
     */
    @ConfigSerializable
    @Getter
    public static class WandDefaultsConfiguration {

        @Comment("Default parameters and flags for inspection wands.")
        private CommandDefaultsConfiguration inspect = new CommandDefaultsConfiguration();

        @Comment("Default parameters and flags for modification (rollback/restore) wands.")
        private CommandDefaultsConfiguration modify = new CommandDefaultsConfiguration();
    }

    /**
     * Per-command default parameters and flags, merged over the base defaults.
     */
    @ConfigSerializable
    @Getter
    public static class CommandDefaultsConfiguration {

        @Comment("Default parameters for this command. Leave empty for none.")
        private Map<String, String> parameters = new LinkedHashMap<>();

        @Comment("Default flags for this command. Leave empty for none.")
        private Map<String, String> flags = new LinkedHashMap<>();
    }
}
