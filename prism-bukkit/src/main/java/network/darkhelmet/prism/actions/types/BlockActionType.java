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

package network.darkhelmet.prism.actions.types;

import de.tr7zw.changeme.nbtapi.NBTContainer;

import java.util.Locale;

import network.darkhelmet.prism.actions.BlockAction;
import network.darkhelmet.prism.api.actions.ActionData;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.ActionType;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class BlockActionType extends ActionType {
    /**
     * Construct a new block action type.
     *
     * @param key The key
     * @param resultType The result type
     * @param reversible If action is reversible
     */
    public BlockActionType(String key, ActionResultType resultType, boolean reversible) {
        super(key, resultType, reversible);
    }

    @Override
    public IAction createAction(ActionData actionData) {
        BlockData blockData = null;
        if (actionData.material() != null) {
            String replacedBlockDataStr = actionData.material().toLowerCase(Locale.ENGLISH);

            if (actionData.materialData() != null) {
                replacedBlockDataStr += actionData.materialData();
            }

            blockData = Bukkit.createBlockData(replacedBlockDataStr);
        }

        NBTContainer nbtContainer = null;
        if (actionData.customData() != null && actionData.customDataVersion() > 0) {
            nbtContainer = new NBTContainer(actionData.customData());
        }

        BlockData replacedBlockData = null;
        if (actionData.replacedMaterial() != null) {
            String replacedBlockDataStr = actionData.replacedMaterial().toLowerCase(Locale.ENGLISH);

            if (actionData.replacedMaterialData() != null) {
                replacedBlockDataStr += actionData.replacedMaterialData();
            }

            replacedBlockData = Bukkit.createBlockData(replacedBlockDataStr);
        }

        Material material = Material.valueOf(actionData.material());
        Material replaced = null;
        if (actionData.replacedMaterial() != null) {
            replaced = Material.valueOf(actionData.replacedMaterial());
        }

        return new BlockAction(
            this, material, blockData, nbtContainer, replaced, replacedBlockData, actionData.descriptor());
    }
}
