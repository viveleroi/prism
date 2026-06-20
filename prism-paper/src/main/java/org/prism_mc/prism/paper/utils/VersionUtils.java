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

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class VersionUtils {

    /**
     * The server major version.
     */
    private static byte serverMajorVersion;

    /**
     * The server minor version.
     */
    private static byte serverMinorVersion;

    /**
     * The server patch version.
     */
    private static byte serverPatchVersion;

    static {
        var segments = Bukkit.getServer().getBukkitVersion().split("-");
        String[] split = segments[0].split("\\.");
        serverMajorVersion = parseSegment(split, 0);
        serverMinorVersion = parseSegment(split, 1);
        serverPatchVersion = parseSegment(split, 2);
    }

    /**
     * Parse a numeric version segment, defaulting to zero when the segment is
     * missing or non-numeric (e.g. build metadata such as "build.12").
     *
     * @param split The dot-split version segments
     * @param index The index to parse
     * @return The parsed value, or zero
     */
    private static byte parseSegment(String[] split, int index) {
        if (index >= split.length) {
            return 0;
        }

        try {
            return Byte.parseByte(split[index]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Check if the server is at least the given major, minor, and patch version.
     *
     * @param major Major version number
     * @param minor Minor version number
     * @param patch Patch version number
     * @return True if the server is at least the version
     */
    public static boolean atLeast(int major, int minor, int patch) {
        if (serverMajorVersion != major) {
            return serverMajorVersion > major;
        }

        if (serverMinorVersion != minor) {
            return serverMinorVersion > minor;
        }

        return serverPatchVersion >= patch;
    }

    /**
     * Get the version as we detected it.
     *
     * @return The version
     */
    public static String detectedVersion() {
        return String.format("%d.%d.%d", serverMajorVersion, serverMinorVersion, serverPatchVersion);
    }
}
