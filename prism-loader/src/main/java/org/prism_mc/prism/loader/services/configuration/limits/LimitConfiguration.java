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
import java.util.Map;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class LimitConfiguration {

    @Comment(
        """
        The permission node that activates these limits. Only players who hold
        this node are subject to the limits below. The node is registered with a
        default of false, so it never applies until you explicitly grant it."""
    )
    private String permission = "";

    @Comment("Limits that apply to every query command.")
    private LimitScopeConfiguration all = new LimitScopeConfiguration();

    @Comment(
        """
        Limits scoped to specific commands, keyed by command path (e.g.
        "command.lookup", "command.rollback", "command.purge.start"). Merged with
        the "all" block when that command runs."""
    )
    private Map<String, LimitScopeConfiguration> commands = new LinkedHashMap<>();
}
