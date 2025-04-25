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

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

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

    /**
     * Gets a descriptor for an item stack, because often times the material isn't enough.
     *
     * @param itemStack The item stack
     * @return A descriptor
     */
    public static String getItemStackDescriptor(ItemStack itemStack) {
        StringBuilder descriptor = new StringBuilder(materialName(itemStack));

        var meta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : null;

        if (meta != null) {
            if (meta.hasItemName()) {
                // Attempt to strip out legacy color/format codes. Right now we use it uses section characters.
                descriptor = new StringBuilder(PlainTextComponentSerializer.plainText()
                    .serialize(LegacyComponentSerializer.legacySection().deserialize(meta.getItemName())));
            } else if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
                // Dyed Leather
                if (leatherArmorMeta.getColor() != Bukkit.getItemFactory().getDefaultLeatherColor()) {
                    descriptor.append(" dyed ").append(leatherArmorMeta.getColor());
                }
            } else if (meta instanceof SkullMeta skullMeta) {
                // Skulls
                if (skullMeta.hasOwner()) {
                    descriptor = new StringBuilder().append(skullMeta.getOwningPlayer().getName()).append("'s head");
                }
            } else if (meta instanceof BookMeta bookMeta) {
                // Books
                if (bookMeta.hasTitle()) {
                    descriptor.append(" '").append(bookMeta.getTitle()).append('\'');
                }

                if (bookMeta.hasAuthor()) {
                    descriptor.append(" by ").append(bookMeta.getAuthor());
                }
            } else if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta
                    && enchantmentStorageMeta.hasStoredEnchants()) {
                if (itemStack.getType().equals(Material.ENCHANTED_BOOK)) {
                    var first = enchantmentStorageMeta.getStoredEnchants().entrySet().iterator().next();

                    descriptor = new StringBuilder();
                    descriptor.append(first.getKey().getKey().getKey().replaceAll("_", " "));

                    if (first.getValue() > 1) {
                        descriptor.append(" ").append(first.getValue() == 2 ? "II" : "III");
                    }

                    descriptor.append(" book");
                }
            } else if (meta instanceof PotionMeta potionMeta) {
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

            // Custom name
            if (meta.hasDisplayName() && !meta.getDisplayName().isEmpty()) {
                descriptor.append(" named \"").append(meta.getDisplayName()).append("\"");
            }
        }

        return descriptor.toString();
    }
}
