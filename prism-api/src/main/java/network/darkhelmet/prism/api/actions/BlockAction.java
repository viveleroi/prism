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

package network.darkhelmet.prism.api.actions;

import org.jetbrains.annotations.Nullable;

public interface BlockAction extends CustomData {
    /**
     * The block namespace. Usually "minecraft"
     *
     * @return The namespace
     */
    String blockNamespace();

    /**
     * The block name.
     *
     * @return Block name
     */
    String blockName();

    /**
     * Serialize block data.
     *
     * @return The block data string
     */
    String serializeBlockData();

    /**
     * Replaced block namespace.
     *
     * @return Block namespace
     */
    String replacedBlockNamespace();

    /**
     * The replaced block name.
     *
     * @return Block name
     */
    String replacedBlockName();

    /**
     * Serialized the replaced block data.
     *
     * @return The serialized block data
     */
    @Nullable String serializeReplacedBlockData();
}
