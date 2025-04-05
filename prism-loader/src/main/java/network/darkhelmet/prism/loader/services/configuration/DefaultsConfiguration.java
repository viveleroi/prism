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

package network.darkhelmet.prism.loader.services.configuration;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class DefaultsConfiguration {
    @Comment("The default radius for the extinguish command.")
    private int extinguishRadius = 5;

    @Comment("""
            The default locale for plugin messages. Messages given to players
            will use their client locale settings.
            """)
    private Locale defaultLocale = Locale.US;

    @Comment("Default parameters for lookups, rollbacks, restores. Leave empty for none.")
    private Map<String, String> parameters = new LinkedHashMap<>();

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
}
