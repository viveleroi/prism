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

package network.darkhelmet.prism.bukkit.actions.types;

import de.tr7zw.nbtapi.NBT;

import network.darkhelmet.prism.api.actions.Action;
import network.darkhelmet.prism.api.actions.ActionData;
import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.ActionType;
import network.darkhelmet.prism.bukkit.actions.BukkitItemStackAction;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemActionType extends ActionType {
    /**
     * Construct a new item action type.
     *
     * @param key The key
     * @param resultType The result type
     * @param reversible If action is reversible
     */
    public ItemActionType(String key, ActionResultType resultType, boolean reversible) {
        super(key, resultType, reversible);
    }

    @Override
    public Action createAction(ActionData actionData) {
        ItemStack itemStack;
        if (actionData.itemData() != null) {
            itemStack = NBT.itemStackFromNBT(NBT.parseNBT(actionData.itemData()));
        } else {
            Material material = Material.valueOf(actionData.material());
            itemStack = new ItemStack(material);
        }

        return new BukkitItemStackAction(this, itemStack, actionData.itemQuantity(), actionData.descriptor());
    }
}