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
import org.jooq.Record17;
import org.jooq.Row17;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UByte;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismActivitiesRecord extends UpdatableRecordImpl<PrismActivitiesRecord> implements
        Record17<
            UInteger,
            UInteger,
            UByte,
            Integer,
            Integer,
            Integer,
            UByte,
            UShort,
            UInteger,
            UInteger,
            UShort,
            UInteger,
            String,
            String,
            UShort,
            String,
            Boolean> {
    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>prism_activities.activity_id</code>.
     */
    public PrismActivitiesRecord setActivityId(UInteger value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.activity_id</code>.
     */
    public UInteger getActivityId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>prism_activities.timestamp</code>.
     */
    public PrismActivitiesRecord setTimestamp(UInteger value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.timestamp</code>.
     */
    public UInteger getTimestamp() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>prism_activities.world_id</code>.
     */
    public PrismActivitiesRecord setWorldId(UByte value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.world_id</code>.
     */
    public UByte getWorldId() {
        return (UByte) get(2);
    }

    /**
     * Setter for <code>prism_activities.x</code>.
     */
    public PrismActivitiesRecord setX(Integer value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.x</code>.
     */
    public Integer getX() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>prism_activities.y</code>.
     */
    public PrismActivitiesRecord setY(Integer value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.y</code>.
     */
    public Integer getY() {
        return (Integer) get(4);
    }

    /**
     * Setter for <code>prism_activities.z</code>.
     */
    public PrismActivitiesRecord setZ(Integer value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.z</code>.
     */
    public Integer getZ() {
        return (Integer) get(5);
    }

    /**
     * Setter for <code>prism_activities.action_id</code>.
     */
    public PrismActivitiesRecord setActionId(UByte value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.action_id</code>.
     */
    public UByte getActionId() {
        return (UByte) get(6);
    }

    /**
     * Setter for <code>prism_activities.material_id</code>.
     */
    public PrismActivitiesRecord setMaterialId(UShort value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.material_id</code>.
     */
    public UShort getMaterialId() {
        return (UShort) get(7);
    }

    /**
     * Setter for <code>prism_activities.block_id</code>.
     */
    public PrismActivitiesRecord setBlockId(UInteger value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.block_id</code>.
     */
    public UInteger getBlockId() {
        return (UInteger) get(8);
    }

    /**
     * Setter for <code>prism_activities.replaced_block_id</code>.
     */
    public PrismActivitiesRecord setReplacedBlockId(UInteger value) {
        set(9, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.replaced_block_id</code>.
     */
    public UInteger getReplacedBlockId() {
        return (UInteger) get(9);
    }

    /**
     * Setter for <code>prism_activities.entity_type_id</code>.
     */
    public PrismActivitiesRecord setEntityTypeId(UShort value) {
        set(10, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.entity_type_id</code>.
     */
    public UShort getEntityTypeId() {
        return (UShort) get(10);
    }

    /**
     * Setter for <code>prism_activities.cause_id</code>.
     */
    public PrismActivitiesRecord setCauseId(UInteger value) {
        set(11, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.cause_id</code>.
     */
    public UInteger getCauseId() {
        return (UInteger) get(11);
    }

    /**
     * Setter for <code>prism_activities.descriptor</code>.
     */
    public PrismActivitiesRecord setDescriptor(String value) {
        set(12, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.descriptor</code>.
     */
    public String getDescriptor() {
        return (String) get(12);
    }

    /**
     * Setter for <code>prism_activities.metadata</code>.
     */
    public PrismActivitiesRecord setMetadata(String value) {
        set(13, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.metadata</code>.
     */
    public String getMetadata() {
        return (String) get(13);
    }

    /**
     * Setter for <code>prism_activities.serializer_version</code>.
     */
    public PrismActivitiesRecord setSerializerVersion(UShort value) {
        set(14, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.serializer_version</code>.
     */
    public UShort getSerializerVersion() {
        return (UShort) get(14);
    }

    /**
     * Setter for <code>prism_activities.serialized_data</code>.
     */
    public PrismActivitiesRecord setSerializedData(String value) {
        set(15, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.serialized_data</code>.
     */
    public String getSerializedData() {
        return (String) get(15);
    }

    /**
     * Setter for <code>prism_activities.reversed</code>.
     */
    public PrismActivitiesRecord setReversed(Boolean value) {
        set(16, value);
        return this;
    }

    /**
     * Getter for <code>prism_activities.reversed</code>.
     */
    public Boolean getReversed() {
        return (Boolean) get(16);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record13 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row17<
        UInteger,
        UInteger,
        UByte,
        Integer,
        Integer,
        Integer,
        UByte,
        UShort,
        UInteger,
        UInteger,
        UShort,
        UInteger,
        String,
        String,
        UShort,
        String,
        Boolean> fieldsRow() {
        return (Row17) super.fieldsRow();
    }

    @Override
    public Row17<
        UInteger,
        UInteger,
        UByte,
        Integer,
        Integer,
        Integer,
        UByte,
        UShort,
        UInteger,
        UInteger,
        UShort,
        UInteger,
        String,
        String,
        UShort,
        String,
        Boolean> valuesRow() {
        return (Row17) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return PRISM_ACTIVITIES.ACTIVITY_ID;
    }

    @Override
    public Field<UInteger> field2() {
        return PRISM_ACTIVITIES.TIMESTAMP;
    }

    @Override
    public Field<UByte> field3() {
        return PRISM_ACTIVITIES.WORLD_ID;
    }

    @Override
    public Field<Integer> field4() {
        return PRISM_ACTIVITIES.X;
    }

    @Override
    public Field<Integer> field5() {
        return PRISM_ACTIVITIES.Y;
    }

    @Override
    public Field<Integer> field6() {
        return PRISM_ACTIVITIES.Z;
    }

    @Override
    public Field<UByte> field7() {
        return PRISM_ACTIVITIES.ACTION_ID;
    }

    @Override
    public Field<UShort> field8() {
        return PRISM_ACTIVITIES.MATERIAL_ID;
    }

    @Override
    public Field<UInteger> field9() {
        return PRISM_ACTIVITIES.BLOCK_ID;
    }

    @Override
    public Field<UInteger> field10() {
        return PRISM_ACTIVITIES.REPLACED_BLOCK_ID;
    }

    @Override
    public Field<UShort> field11() {
        return PRISM_ACTIVITIES.ENTITY_TYPE_ID;
    }

    @Override
    public Field<UInteger> field12() {
        return PRISM_ACTIVITIES.CAUSE_ID;
    }

    @Override
    public Field<String> field13() {
        return PRISM_ACTIVITIES.DESCRIPTOR;
    }

    @Override
    public Field<String> field14() {
        return PRISM_ACTIVITIES.METADATA;
    }

    @Override
    public Field<UShort> field15() {
        return PRISM_ACTIVITIES.SERIALIZER_VERSION;
    }

    @Override
    public Field<String> field16() {
        return PRISM_ACTIVITIES.SERIALIZED_DATA;
    }

    @Override
    public Field<Boolean> field17() {
        return PRISM_ACTIVITIES.REVERSED;
    }

    @Override
    public UInteger component1() {
        return getActivityId();
    }

    @Override
    public UInteger component2() {
        return getTimestamp();
    }

    @Override
    public UByte component3() {
        return getWorldId();
    }

    @Override
    public Integer component4() {
        return getX();
    }

    @Override
    public Integer component5() {
        return getY();
    }

    @Override
    public Integer component6() {
        return getZ();
    }

    @Override
    public UByte component7() {
        return getActionId();
    }

    @Override
    public UShort component8() {
        return getMaterialId();
    }

    @Override
    public UInteger component9() {
        return getBlockId();
    }

    @Override
    public UInteger component10() {
        return getReplacedBlockId();
    }

    @Override
    public UShort component11() {
        return getEntityTypeId();
    }

    @Override
    public UInteger component12() {
        return getCauseId();
    }

    @Override
    public String component13() {
        return getDescriptor();
    }

    @Override
    public String component14() {
        return getMetadata();
    }

    @Override
    public UShort component15() {
        return getSerializerVersion();
    }

    @Override
    public String component16() {
        return getSerializedData();
    }

    @Override
    public Boolean component17() {
        return getReversed();
    }

    @Override
    public UInteger value1() {
        return getActivityId();
    }

    @Override
    public PrismActivitiesRecord value1(UInteger value) {
        setActivityId(value);
        return this;
    }

    @Override
    public UInteger value2() {
        return getTimestamp();
    }

    @Override
    public PrismActivitiesRecord value2(UInteger value) {
        setTimestamp(value);
        return this;
    }

    @Override
    public UByte value3() {
        return getWorldId();
    }

    @Override
    public PrismActivitiesRecord value3(UByte value) {
        setWorldId(value);
        return this;
    }

    @Override
    public Integer value4() {
        return getX();
    }

    @Override
    public PrismActivitiesRecord value4(Integer value) {
        setX(value);
        return this;
    }

    @Override
    public Integer value5() {
        return getY();
    }

    @Override
    public PrismActivitiesRecord value5(Integer value) {
        setY(value);
        return this;
    }

    @Override
    public Integer value6() {
        return getZ();
    }

    @Override
    public PrismActivitiesRecord value6(Integer value) {
        setZ(value);
        return this;
    }

    @Override
    public UByte value7() {
        return getActionId();
    }

    @Override
    public PrismActivitiesRecord value7(UByte value) {
        setActionId(value);
        return this;
    }

    @Override
    public UShort value8() {
        return getMaterialId();
    }

    @Override
    public PrismActivitiesRecord value8(UShort value) {
        setMaterialId(value);
        return this;
    }

    @Override
    public UInteger value9() {
        return getBlockId();
    }

    @Override
    public PrismActivitiesRecord value9(UInteger value) {
        setBlockId(value);
        return this;
    }

    @Override
    public UInteger value10() {
        return getReplacedBlockId();
    }

    @Override
    public PrismActivitiesRecord value10(UInteger value) {
        setReplacedBlockId(value);
        return this;
    }

    @Override
    public UShort value11() {
        return getEntityTypeId();
    }

    @Override
    public PrismActivitiesRecord value11(UShort value) {
        setEntityTypeId(value);
        return this;
    }

    @Override
    public UInteger value12() {
        return getCauseId();
    }

    @Override
    public PrismActivitiesRecord value12(UInteger value) {
        setCauseId(value);
        return this;
    }

    @Override
    public String value13() {
        return getDescriptor();
    }

    @Override
    public PrismActivitiesRecord value13(String value) {
        setDescriptor(value);
        return this;
    }

    @Override
    public String value14() {
        return getMetadata();
    }

    @Override
    public PrismActivitiesRecord value14(String value) {
        setMetadata(value);
        return this;
    }

    @Override
    public UShort value15() {
        return getSerializerVersion();
    }

    @Override
    public PrismActivitiesRecord value15(UShort value) {
        setSerializerVersion(value);
        return this;
    }

    @Override
    public String value16() {
        return getSerializedData();
    }

    @Override
    public PrismActivitiesRecord value16(String value) {
        setSerializedData(value);
        return this;
    }

    @Override
    public Boolean value17() {
        return getReversed();
    }

    @Override
    public PrismActivitiesRecord value17(Boolean value) {
        setReversed(value);
        return this;
    }

    @Override
    public PrismActivitiesRecord values(
            UInteger value1,
            UInteger value2,
            UByte value3,
            Integer value4,
            Integer value5,
            Integer value6,
            UByte value7,
            UShort value8,
            UInteger value9,
            UInteger value10,
            UShort value11,
            UInteger value12,
            String value13,
            String value14,
            UShort value15,
            String value16,
            Boolean value17) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        value12(value12);
        value13(value13);
        value14(value14);
        value15(value15);
        value16(value16);
        value17(value17);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PrismActivitiesRecord.
     */
    public PrismActivitiesRecord() {
        super(PRISM_ACTIVITIES);
    }

    /**
     * Create a detached, initialised PrismActivitiesRecord.
     */
    public PrismActivitiesRecord(
            UInteger activityId,
            UInteger timestamp,
            UByte worldId,
            Integer x,
            Integer y,
            Integer z,
            UByte actionId,
            UShort materialId,
            UInteger blockId,
            UInteger replacedBlockId,
            UShort entityTypeId,
            UInteger causeId,
            String descriptor,
            String metadata,
            UShort serializerVersion,
            String serializedData,
            Boolean reversed) {
        super(PRISM_ACTIVITIES);

        setActivityId(activityId);
        setTimestamp(timestamp);
        setWorldId(worldId);
        setX(x);
        setY(y);
        setZ(z);
        setActionId(actionId);
        setMaterialId(materialId);
        setBlockId(blockId);
        setReplacedBlockId(replacedBlockId);
        setEntityTypeId(entityTypeId);
        setCauseId(causeId);
        setDescriptor(descriptor);
        setMetadata(metadata);
        setSerializerVersion(serializerVersion);
        setSerializedData(serializedData);
        setReversed(reversed);
    }
}
