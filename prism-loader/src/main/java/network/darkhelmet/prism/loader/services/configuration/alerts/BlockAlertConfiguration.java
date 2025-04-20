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

package network.darkhelmet.prism.loader.services.configuration.alerts;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@Getter
public class BlockAlertConfiguration extends AlertConfiguration {
    /**
     * Block tags.
     */
    private List<String> blockTags;

    /**
     * Empty constructor for serializer.
     */
    public BlockAlertConfiguration() {}

    /**
     * Constructor.
     *
     * @param materials The materials
     * @param hexColor The hex color
     */
    public BlockAlertConfiguration(List<String> materials, String hexColor) {
        this(materials, null, hexColor);
    }

    /**
     * Constructor.
     *
     * @param materials The materials
     * @param blockTags The block tags
     * @param hexColor The hex color
     */
    public BlockAlertConfiguration(
            List<String> materials, List<String> blockTags, String hexColor) {
        super(materials, hexColor);

        this.blockTags = blockTags;
    }
}
