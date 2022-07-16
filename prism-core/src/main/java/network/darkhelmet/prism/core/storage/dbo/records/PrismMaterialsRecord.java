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

package network.darkhelmet.prism.core.storage.dbo.records;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UShort;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_MATERIALS;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismMaterialsRecord extends UpdatableRecordImpl<PrismMaterialsRecord> implements
        Record3<UShort, String, String> {
    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>prism_materials.material_id</code>.
     */
    public PrismMaterialsRecord setMaterialId(UShort value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>prism_materials.material_id</code>.
     */
    public UShort getMaterialId() {
        return (UShort) get(0);
    }

    /**
     * Setter for <code>prism_materials.material</code>.
     */
    public PrismMaterialsRecord setMaterial(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>prism_materials.material</code>.
     */
    public String getMaterial() {
        return (String) get(1);
    }

    /**
     * Setter for <code>prism_materials.data</code>.
     */
    public PrismMaterialsRecord setData(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>prism_materials.data</code>.
     */
    public String getData() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UShort> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UShort, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UShort, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UShort> field1() {
        return PRISM_MATERIALS.MATERIAL_ID;
    }

    @Override
    public Field<String> field2() {
        return PRISM_MATERIALS.MATERIAL;
    }

    @Override
    public Field<String> field3() {
        return PRISM_MATERIALS.DATA;
    }

    @Override
    public UShort component1() {
        return getMaterialId();
    }

    @Override
    public String component2() {
        return getMaterial();
    }

    @Override
    public String component3() {
        return getData();
    }

    @Override
    public UShort value1() {
        return getMaterialId();
    }

    @Override
    public PrismMaterialsRecord value1(UShort value) {
        setMaterialId(value);
        return this;
    }

    @Override
    public String value2() {
        return getMaterial();
    }

    @Override
    public PrismMaterialsRecord value2(String value) {
        setMaterial(value);
        return this;
    }

    @Override
    public String value3() {
        return getData();
    }

    @Override
    public PrismMaterialsRecord value3(String value) {
        setData(value);
        return this;
    }

    @Override
    public PrismMaterialsRecord values(UShort value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PrismMaterialsRecord.
     */
    public PrismMaterialsRecord() {
        super(PRISM_MATERIALS);
    }

    /**
     * Create a detached, initialised PrismMaterialsRecord.
     */
    public PrismMaterialsRecord(UShort materialId, String material, String data) {
        super(PRISM_MATERIALS);

        setMaterialId(materialId);
        setMaterial(material);
        setData(data);
    }
}
