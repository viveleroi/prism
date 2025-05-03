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

package org.prism_mc.prism.core.storage.dbo.records;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UShort;

import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ENTITY_TYPES;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismEntityTypesRecord extends UpdatableRecordImpl<PrismEntityTypesRecord> implements
        Record2<UShort, String> {
    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>prism_entity_types.entity_type_id</code>.
     */
    public PrismEntityTypesRecord setEntityTypeId(UShort value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>prism_entity_types.entity_type_id</code>.
     */
    public UShort getEntityTypeId() {
        return (UShort) get(0);
    }

    /**
     * Setter for <code>prism_entity_types.entity_type</code>.
     */
    public PrismEntityTypesRecord setEntityType(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>prism_entity_types.entity_type</code>.
     */
    public String getEntityType() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UShort> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<UShort, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<UShort, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<UShort> field1() {
        return PRISM_ENTITY_TYPES.ENTITY_TYPE_ID;
    }

    @Override
    public Field<String> field2() {
        return PRISM_ENTITY_TYPES.ENTITY_TYPE;
    }

    @Override
    public UShort component1() {
        return getEntityTypeId();
    }

    @Override
    public String component2() {
        return getEntityType();
    }

    @Override
    public UShort value1() {
        return getEntityTypeId();
    }

    @Override
    public PrismEntityTypesRecord value1(UShort value) {
        setEntityTypeId(value);
        return this;
    }

    @Override
    public String value2() {
        return getEntityType();
    }

    @Override
    public PrismEntityTypesRecord value2(String value) {
        setEntityType(value);
        return this;
    }

    @Override
    public PrismEntityTypesRecord values(UShort value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PrismEntityTypesRecord.
     */
    public PrismEntityTypesRecord() {
        super(PRISM_ENTITY_TYPES);
    }

    /**
     * Create a detached, initialised PrismEntityTypesRecord.
     */
    public PrismEntityTypesRecord(UShort entityTypeId, String entityType) {
        super(PRISM_ENTITY_TYPES);

        setEntityTypeId(entityTypeId);
        setEntityType(entityType);
    }
}
