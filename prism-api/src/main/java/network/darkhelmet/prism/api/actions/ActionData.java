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

package network.darkhelmet.prism.api.actions;

public final class ActionData {
    /**
     * The material.
     */
    private final String material;

    /**
     * The material data.
     */
    private final String materialData;

    /**
     * The replaced material.
     */
    private final String replacedMaterial;

    /**
     * The replace material data.
     */
    private final String replacedMaterialData;

    /**
     * The entity type.
     */
    private final String entityType;

    /**
     * The custom data.
     */
    private final String customData;

    /**
     * The descriptor.
     */
    private final String descriptor;

    /**
     * The custom data version.
     */
    private final short customDataVersion;

    /**
     * Construct action data.
     *
     * @param material The material
     * @param materialData The material data
     * @param replacedMaterial The replaced material
     * @param replacedMaterialData The replaced material data
     * @param entityType The entity type
     * @param customData The custom data
     * @param descriptor The description
     * @param customDataVersion The custom data version
     */
    public ActionData(
            String material,
            String materialData,
            String replacedMaterial,
            String replacedMaterialData,
            String entityType,
            String customData,
            String descriptor,
            short customDataVersion) {
        this.material = material;
        this.materialData = materialData;
        this.replacedMaterial = replacedMaterial;
        this.replacedMaterialData = replacedMaterialData;
        this.entityType = entityType;
        this.customData = customData;
        this.descriptor = descriptor;
        this.customDataVersion = customDataVersion;
    }

    /**
     * Get the material.
     *
     * @return The material
     */
    public String material() {
        return material;
    }

    /**
     * Get the material data.
     *
     * @return The material data
     */
    public String materialData() {
        return materialData;
    }

    /**
     * Get the replaced material.
     *
     * @return The replaced material
     */
    public String replacedMaterial() {
        return replacedMaterial;
    }

    /**
     * Get the replaced material data.
     *
     * @return The replaced material data.
     */
    public String replacedMaterialData() {
        return replacedMaterialData;
    }

    /**
     * Get the entity type.
     *
     * @return The entity type
     */
    public String entityType() {
        return entityType;
    }

    /**
     * Get the custom data.
     *
     * @return The custom data
     */
    public String customData() {
        return customData;
    }

    /**
     * Get the descriptor.
     *
     * @return The descriptor
     */
    public String descriptor() {
        return descriptor;
    }

    /**
     * Get the custom data version.
     *
     * @return The custom data version
     */
    public short customDataVersion() {
        return customDataVersion;
    }
}
