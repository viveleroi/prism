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

package org.prism_mc.prism.loader.services.configuration.limits;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class NumericLimitConfiguration {

    @Comment("The smallest value the holder may use, or unset for no minimum.")
    private Integer min;

    @Comment("The largest value the holder may use, or unset for no maximum.")
    private Integer max;

    /**
     * Empty constructor for configurate.
     */
    public NumericLimitConfiguration() {}

    /**
     * Constructor.
     *
     * @param min The minimum, or null
     * @param max The maximum, or null
     */
    public NumericLimitConfiguration(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }
}
