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

package org.prism_mc.prism.loader.services.configuration.limits;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class LimitScopeConfiguration {

    @Comment(
        """
        Numeric (integer) parameter limits, keyed by parameter name (e.g. "r",
        "above", "below"). Each entry may set a min and/or max."""
    )
    private Map<String, NumericLimitConfiguration> numeric = new LinkedHashMap<>();

    @Comment(
        """
        Maximum look-back for duration parameters, keyed by parameter name (e.g.
        "since", "before"). Values are durations like "7d", "12h". A holder may
        not query further back than this."""
    )
    private Map<String, String> maxTime = new LinkedHashMap<>();

    @Comment(
        """
        Allowed value whitelists, keyed by parameter name (e.g. "world", "a",
        "b"). The holder may only use the listed values for that parameter."""
    )
    private Map<String, List<String>> allowed = new LinkedHashMap<>();
}
