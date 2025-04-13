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

package network.darkhelmet.prism.utils;

import java.util.Locale;

import lombok.experimental.UtilityClass;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

@UtilityClass
public class ItemUtils {
    /**
     * Checks for null or air items.
     *
     * @param item The item
     * @return True if null or air
     */
    public static boolean nullOrAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    /**
     * Gets a descriptor for an item stack, because often times the material isn't enough.
     *
     * @param itemStack The item stack
     * @return A descriptor
     */
    public static String getItemStackDescriptor(ItemStack itemStack) {
        StringBuilder descriptor = new StringBuilder(itemStack.getType().name().toLowerCase(Locale.ENGLISH)
            .replace('_', ' '));

        var meta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : null;

        if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
            // Dyed Leather
            if (leatherArmorMeta.getColor() != Bukkit.getItemFactory().getDefaultLeatherColor()) {
                descriptor.append(" dyed ").append(leatherArmorMeta.getColor());
            }
        } else if (meta instanceof SkullMeta skullMeta) {
            // Skulls
            if (skullMeta.hasOwner()) {
                descriptor.append(skullMeta.getOwningPlayer().getName()).append("'s ");
            }
        } else if (meta instanceof BookMeta bookMeta) {
            // Books
            if (bookMeta.hasTitle()) {
                descriptor.append(" '").append(bookMeta.getTitle()).append('\'');
            }

            if (bookMeta.hasAuthor()) {
                descriptor.append(" by ").append(bookMeta.getAuthor());
            }
        }

        // Custom name
        if (meta != null) {
            if (meta.hasDisplayName() && !meta.getDisplayName().isEmpty()) {
                descriptor.append(" named \"").append(meta.getDisplayName()).append("\"");
            }
        }

        // Potions
        if (itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
            var potionType = potionMeta.getBasePotionType();
            if (potionType != null) {
                var name = potionType.name().replaceAll("_", " ").toLowerCase(Locale.ENGLISH);

                if (name.contains("strong ")) {
                    name = name.replace("strong ", "");
                    name += " II";
                } else if (name.contains("long ")) {
                    name = name.replace("long ", "");
                    name += " extended";
                }

                descriptor.append(" of ").append(name);
            }
        }

        return descriptor.toString();
    }
}
