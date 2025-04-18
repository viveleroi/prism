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

package network.darkhelmet.prism.loader.services.configuration.filters;

import java.util.List;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@Getter
public class FilterConditionsConfiguration {
    /**
     * Actions.
     */
    private List<String> actions;

    /**
     * Block tags.
     */
    private List<String> blockTags;

    /**
     * Causes.
     */
    private List<String> causes;

    /**
     * Entity Types.
     */
    private List<String> entityTypes;

    /**
     * Entity Type Tags.
     */
    private List<String> entityTypesTags;

    /**
     * Item tags.
     */
    private List<String> itemTags;

    /**
     * Materials.
     */
    private List<String> materials;

    /**
     * Permissions.
     */
    private List<String> permissions;

    /**
     * Worlds.
     */
    private List<String> worlds;
}
