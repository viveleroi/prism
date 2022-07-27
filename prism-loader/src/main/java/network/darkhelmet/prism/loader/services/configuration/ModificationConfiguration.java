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
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import network.darkhelmet.prism.api.services.configuration.IModificationConfiguration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class ModificationConfiguration implements IModificationConfiguration {
    @Comment("List materials that should be excluded from modifications.")
    private List<String> blockBlacklist = new ArrayList<>();

    /**
     * Constructor.
     */
    public ModificationConfiguration() {
        blockBlacklist.add("bedrock");
        blockBlacklist.add("tnt");
    }

    @Override
    public boolean blockBlacklistContainsAny(String... values) {
        return blockBlacklist.stream().anyMatch(str ->
            Arrays.stream(values).anyMatch(v -> v.equalsIgnoreCase(str)));
    }
}
