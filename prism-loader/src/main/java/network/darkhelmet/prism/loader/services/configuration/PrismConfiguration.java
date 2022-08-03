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

package network.darkhelmet.prism.loader.services.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;

import network.darkhelmet.prism.loader.services.configuration.cache.CacheConfiguration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class PrismConfiguration {
    @Comment("Actions are in-game events/changes that Prism can record data for.\n"
        + "Some are purely informational, some can be reversed/restored.\n"
        + "Disabling any here will completely prevent prism from recording them.")
    private ActionsConfiguration actions = new ActionsConfiguration();

    @Comment("""
            Configure how Prism caches data. Probably best to leave
            these settings alone unless you have specific reasons to change them.
            """)
    private CacheConfiguration cacheConfiguration = new CacheConfiguration();

    @Comment("Enable plugin debug mode. Produces extra logging to help diagnose issues.")
    private boolean debug = false;

    @Comment("The default locale for plugin messages. Messages given to players\n"
        + "will use their client locale settings.")
    private Locale defaultLocale = Locale.US;

    @Comment("Filters allow fine-grained control over what prism records.\n"
        + "See the wiki for documentation.")
    private List<FilterConfiguartion> filters = new ArrayList<>();

    @Comment("Configure rules for modifications (rollbacks/restores).")
    private ModificationConfiguration modifications = new ModificationConfiguration();

    @Comment("Sets the default radius to use when searching for nearby activity.")
    private int nearRadius = 5;

    @Comment("Limits how many results are shown \"per page\" when doing lookups.")
    private int perPage = 5;
}