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

package org.prism_mc.prism.loader.services.configuration.alerts;

import java.util.List;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class BlockBreakAlertConfiguration extends BlockAlertConfiguration {
    @Comment("Limit how many neighboring blocks are scanned.")
    private int maxScanCount = 100;

    /**
     * Empty constructor for serializer.
     */
    public BlockBreakAlertConfiguration() {}

    /**
     * Constructor.
     *
     * @param materials The materials
     * @param blockTags The block tags
     * @param hexColor The hex color
     * @param maxScanCount The max scan count
     */
    public BlockBreakAlertConfiguration(
            List<String> materials, List<String> blockTags, String hexColor, int maxScanCount) {
        super(materials, blockTags, hexColor);

        this.maxScanCount = maxScanCount;
    }
}
