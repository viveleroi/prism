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
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES_CUSTOM_DATA;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismActivitiesCustomDataRecord extends UpdatableRecordImpl<PrismActivitiesCustomDataRecord> implements
        Record4<UInteger, UInteger, Short, String> {
    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>prism_activities_custom_data.extra_id</code>.
     */
    public PrismActivitiesCustomDataRecord setExtraId(UInteger value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities_custom_data.extra_id</code>.
     */
    public UInteger getExtraId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for
     * <code>prism_activities_custom_data.activity_id</code>.
     */
    public PrismActivitiesCustomDataRecord setActivityId(UInteger value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for
     * <code>prism_activities_custom_data.activity_id</code>.
     */
    public UInteger getActivityId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>prism_activities_custom_data.version</code>.
     */
    public PrismActivitiesCustomDataRecord setVersion(Short value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities_custom_data.version</code>.
     */
    public Short getVersion() {
        return (Short) get(2);
    }

    /**
     * Setter for <code>prism_activities_custom_data.data</code>.
     */
    public PrismActivitiesCustomDataRecord setData(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities_custom_data.data</code>.
     */
    public String getData() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UInteger, UInteger, Short, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UInteger, UInteger, Short, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return PRISM_ACTIVITIES_CUSTOM_DATA.EXTRA_ID;
    }

    @Override
    public Field<UInteger> field2() {
        return PRISM_ACTIVITIES_CUSTOM_DATA.ACTIVITY_ID;
    }

    @Override
    public Field<Short> field3() {
        return PRISM_ACTIVITIES_CUSTOM_DATA.VERSION;
    }

    @Override
    public Field<String> field4() {
        return PRISM_ACTIVITIES_CUSTOM_DATA.DATA;
    }

    @Override
    public UInteger component1() {
        return getExtraId();
    }

    @Override
    public UInteger component2() {
        return getActivityId();
    }

    @Override
    public Short component3() {
        return getVersion();
    }

    @Override
    public String component4() {
        return getData();
    }

    @Override
    public UInteger value1() {
        return getExtraId();
    }

    @Override
    public PrismActivitiesCustomDataRecord value1(UInteger value) {
        setExtraId(value);
        return this;
    }

    @Override
    public UInteger value2() {
        return getActivityId();
    }

    @Override
    public PrismActivitiesCustomDataRecord value2(UInteger value) {
        setActivityId(value);
        return this;
    }

    @Override
    public Short value3() {
        return getVersion();
    }

    @Override
    public PrismActivitiesCustomDataRecord value3(Short value) {
        setVersion(value);
        return this;
    }

    @Override
    public String value4() {
        return getData();
    }

    @Override
    public PrismActivitiesCustomDataRecord value4(String value) {
        setData(value);
        return this;
    }

    @Override
    public PrismActivitiesCustomDataRecord values(UInteger value1, UInteger value2, Short value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PrismActivitiesCustomDataRecord.
     */
    public PrismActivitiesCustomDataRecord() {
        super(PRISM_ACTIVITIES_CUSTOM_DATA);
    }

    /**
     * Create a detached, initialised PrismActivitiesCustomDataRecord.
     */
    public PrismActivitiesCustomDataRecord(UInteger extraId, UInteger activityId, Short version, String data) {
        super(PRISM_ACTIVITIES_CUSTOM_DATA);

        setExtraId(extraId);
        setActivityId(activityId);
        setVersion(version);
        setData(data);
    }
}
