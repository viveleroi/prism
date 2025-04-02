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

package network.darkhelmet.prism.api.util;

import lombok.Getter;
import lombok.ToString;

@ToString
public class Coordinate {
    /**
     * The x coordinate.
     */
    @Getter
    private final double x;

    /**
     * The y coordinate.
     */
    @Getter
    private final double y;

    /**
     * The z coordinate.
     */
    @Getter
    private final double z;

    /**
     * Construct a new coordinate.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     */
    public Coordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get the x as an int.
     *
     * @return The x coordinate
     */
    public int intX() {
        return (int) x;
    }

    /**
     * Get the y as an int.
     *
     * @return The y coordinate
     */
    public int intY() {
        return (int) y;
    }

    /**
     * Get the z as an int.
     *
     * @return The z coordinate
     */
    public int intZ() {
        return (int) z;
    }
}
