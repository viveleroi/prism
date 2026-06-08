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

package org.prism_mc.prism.paper.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.prism_mc.prism.api.util.Coordinate;

@UtilityClass
public class QueryParsingUtil {

    /**
     * Pattern matching a relative time segment: a number followed by a unit (s, m, h, d, w).
     */
    private static final Pattern RELATIVE_TIME_PATTERN = Pattern.compile("(\\d+)([smhdw])");

    /**
     * Parse a relative time string into a unix epoch timestamp (seconds).
     *
     * <p>Supports multiple units, e.g. "1d2h30m".</p>
     *
     * @param value The relative time string
     * @return The epoch timestamp in seconds, or null if invalid
     */
    public static Long parseRelativeTimestamp(String value) {
        final Matcher matcher = RELATIVE_TIME_PATTERN.matcher(value);

        boolean found = false;
        final Calendar cal = Calendar.getInstance();
        while (matcher.find()) {
            found = true;
            final int time = Integer.parseInt(matcher.group(1));
            final String unit = matcher.group(2);

            switch (unit) {
                case "w":
                    cal.add(Calendar.WEEK_OF_YEAR, -1 * time);
                    break;
                case "d":
                    cal.add(Calendar.DAY_OF_MONTH, -1 * time);
                    break;
                case "h":
                    cal.add(Calendar.HOUR, -1 * time);
                    break;
                case "m":
                    cal.add(Calendar.MINUTE, -1 * time);
                    break;
                case "s":
                    cal.add(Calendar.SECOND, -1 * time);
                    break;
                default:
                    break;
            }
        }

        if (!found) {
            return null;
        }

        return cal.getTime().getTime() / 1000;
    }

    /**
     * Parse a coordinate string in the format "x,y,z".
     *
     * @param input The coordinate string
     * @return The coordinate, or null if invalid
     */
    public static Coordinate parseCoordinate(String input) {
        String[] parts = input.split(",");
        if (parts.length != 3) {
            return null;
        }

        try {
            return new Coordinate(
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim())
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Resolve comma-separated block tag names to a list of block names.
     *
     * @param input Comma-separated tag names, e.g. "minecraft:logs,minecraft:planks"
     * @return The resolved block names
     */
    public static List<String> resolveBlockTags(String input) {
        List<String> blockNames = new ArrayList<>();
        for (String tagName : input.split(",")) {
            NamespacedKey key = NamespacedKey.fromString(tagName.trim());
            if (key == null) {
                continue;
            }

            Tag<Material> tag = Bukkit.getTag("blocks", key, Material.class);
            if (tag != null) {
                tag.getValues().forEach(material -> blockNames.add(material.toString().toLowerCase(Locale.ENGLISH)));
            }
        }

        return blockNames;
    }

    /**
     * Resolve comma-separated entity type tag names to a list of entity type names.
     *
     * @param input Comma-separated tag names, e.g. "minecraft:raiders"
     * @return The resolved entity type names
     */
    public static List<String> resolveEntityTypeTags(String input) {
        List<String> entityNames = new ArrayList<>();
        for (String tagName : input.split(",")) {
            NamespacedKey key = NamespacedKey.fromString(tagName.trim());
            if (key == null) {
                continue;
            }

            Tag<EntityType> tag = Bukkit.getTag("entity_types", key, EntityType.class);
            if (tag != null) {
                tag
                    .getValues()
                    .forEach(entityType -> entityNames.add(entityType.toString().toLowerCase(Locale.ENGLISH)));
            }
        }

        return entityNames;
    }

    /**
     * Resolve comma-separated item tag names to a list of material names.
     *
     * @param input Comma-separated tag names, e.g. "minecraft:swords"
     * @return The resolved material names
     */
    public static List<String> resolveItemTags(String input) {
        List<String> materialNames = new ArrayList<>();
        for (String tagName : input.split(",")) {
            NamespacedKey key = NamespacedKey.fromString(tagName.trim());
            if (key == null) {
                continue;
            }

            Tag<Material> tag = Bukkit.getTag("items", key, Material.class);
            if (tag != null) {
                tag.getValues().forEach(material -> materialNames.add(material.toString().toLowerCase(Locale.ENGLISH)));
            }
        }

        return materialNames;
    }
}
