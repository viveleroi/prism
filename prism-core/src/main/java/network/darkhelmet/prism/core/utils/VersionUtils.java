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

package network.darkhelmet.prism.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtils {
    private VersionUtils() {}

    /**
     * Parses the mc version as a short.
     *
     * @param version The version as a string
     * @return The mc version as a number
     */
    public static Short minecraftVersion(String version) {
        Pattern pattern = Pattern.compile("([0-9]+\\.[0-9]+)");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            return Short.parseShort(matcher.group(1).replace(".", ""));
        }

        return null;
    }
}
