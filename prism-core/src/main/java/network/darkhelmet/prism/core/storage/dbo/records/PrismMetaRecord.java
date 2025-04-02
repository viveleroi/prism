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

package network.darkhelmet.prism.core.storage.dbo.records;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UByte;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_META;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismMetaRecord extends UpdatableRecordImpl<PrismMetaRecord> implements Record3<UByte, String, String> {
    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>prism_meta.meta_id</code>.
     */
    public PrismMetaRecord setMetaId(UByte value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>prism_meta.meta_id</code>.
     */
    public UByte getMetaId() {
        return (UByte) get(0);
    }

    /**
     * Setter for <code>prism_meta.k</code>.
     */
    public PrismMetaRecord setK(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>prism_meta.k</code>.
     */
    public String getK() {
        return (String) get(1);
    }

    /**
     * Setter for <code>prism_meta.v</code>.
     */
    public PrismMetaRecord setV(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>prism_meta.v</code>.
     */
    public String getV() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UByte> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UByte, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UByte, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UByte> field1() {
        return PRISM_META.META_ID;
    }

    @Override
    public Field<String> field2() {
        return PRISM_META.K;
    }

    @Override
    public Field<String> field3() {
        return PRISM_META.V;
    }

    @Override
    public UByte component1() {
        return getMetaId();
    }

    @Override
    public String component2() {
        return getK();
    }

    @Override
    public String component3() {
        return getV();
    }

    @Override
    public UByte value1() {
        return getMetaId();
    }

    @Override
    public PrismMetaRecord value1(UByte value) {
        setMetaId(value);
        return this;
    }

    @Override
    public String value2() {
        return getK();
    }

    @Override
    public PrismMetaRecord value2(String value) {
        setK(value);
        return this;
    }

    @Override
    public String value3() {
        return getV();
    }

    @Override
    public PrismMetaRecord value3(String value) {
        setV(value);
        return this;
    }

    @Override
    public PrismMetaRecord values(UByte value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PrismMetaRecord.
     */
    public PrismMetaRecord() {
        super(PRISM_META);
    }

    /**
     * Create a detached, initialised PrismMetaRecord.
     */
    public PrismMetaRecord(UByte metaId, String k, String v) {
        super(PRISM_META);

        setMetaId(metaId);
        setK(k);
        setV(v);
    }
}
