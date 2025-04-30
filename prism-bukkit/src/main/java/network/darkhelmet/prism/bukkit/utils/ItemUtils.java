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

package network.darkhelmet.prism.bukkit.utils;

import java.util.Locale;

import lombok.experimental.UtilityClass;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class ItemUtils {
    /**
     * Get the amount of a material an inventory has room for.
     *
     * @param inventory The inventory
     * @param material The material
     * @param max The max acceptable quantity to return
     * @return Amount
     */
    public static int inventoryAcceptsQuantity(Inventory inventory, Material material, Integer max) {
        var acceptableQuantity = 0;

        for (var item : inventory.getStorageContents()) {
            if (nullOrAir(item)) {
                acceptableQuantity += material.getMaxStackSize();
            } else if (item.getType().equals(material)) {
                acceptableQuantity += material.getMaxStackSize() - item.getAmount();
            }

            if (max != null && acceptableQuantity >= max) {
                acceptableQuantity = max;

                break;
            }
        }

        return acceptableQuantity;
    }

    /**
     * Get the material "nice" name for an item stack.
     *
     * @param itemStack The item stack
     * @return The material name
     */
    public static String materialName(ItemStack itemStack) {
        return itemStack.getType().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
    }

    /**
     * Checks for null or air items.
     *
     * @param item The item
     * @return True if null or air
     */
    public static boolean nullOrAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }
}
