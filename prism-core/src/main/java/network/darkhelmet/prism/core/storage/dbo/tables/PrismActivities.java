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

package network.darkhelmet.prism.core.storage.dbo.tables;

import java.util.Arrays;
import java.util.List;

import network.darkhelmet.prism.core.storage.dbo.Indexes;
import network.darkhelmet.prism.core.storage.dbo.Keys;
import network.darkhelmet.prism.core.storage.dbo.records.PrismActivitiesRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row16;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UByte;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_DATABASE;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismActivities extends TableImpl<PrismActivitiesRecord> {
    private static final long serialVersionUID = 1L;

    /**
     * The table prefix.
     */
    private final String prefix;

    /**
     * The class holding records for this type.
     */
    @Override
    public Class<PrismActivitiesRecord> getRecordType() {
        return PrismActivitiesRecord.class;
    }

    /**
     * The column <code>prism_activities.activity_id</code>.
     */
    public final TableField<PrismActivitiesRecord, UInteger> ACTIVITY_ID = createField(
        DSL.name("activity_id"),
        SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true),
        this,
        "");

    /**
     * The column <code>prism_activities.timestamp</code>.
     */
    public final TableField<PrismActivitiesRecord, UInteger> TIMESTAMP = createField(
        DSL.name("timestamp"),
        SQLDataType.INTEGERUNSIGNED.nullable(false),
        this,
        "");

    /**
     * The column <code>prism_activities.world_id</code>.
     */
    public final TableField<PrismActivitiesRecord, UByte> WORLD_ID = createField(
        DSL.name("world_id"),
        SQLDataType.TINYINTUNSIGNED.nullable(false),
        this,
        "");

    /**
     * The column <code>prism_activities.x</code>.
     */
    public final TableField<PrismActivitiesRecord, Integer> X = createField(
        DSL.name("x"),
        SQLDataType.INTEGER.nullable(false),
        this,
        "");

    /**
     * The column <code>prism_activities.y</code>.
     */
    public final TableField<PrismActivitiesRecord, Integer> Y = createField(
        DSL.name("y"),
        SQLDataType.INTEGER.nullable(false),
        this,
        "");

    /**
     * The column <code>prism_activities.z</code>.
     */
    public final TableField<PrismActivitiesRecord, Integer> Z = createField(
        DSL.name("z"),
        SQLDataType.INTEGER.nullable(false),
        this,
        "");

    /**
     * The column <code>prism_activities.action_id</code>.
     */
    public final TableField<PrismActivitiesRecord, UByte> ACTION_ID = createField(
        DSL.name("action_id"),
        SQLDataType.TINYINTUNSIGNED.nullable(false),
        this,
        "");

    /**
     * The column <code>prism_activities.material_id</code>.
     */
    public final TableField<PrismActivitiesRecord, UShort> MATERIAL_ID = createField(
        DSL.name("material_id"),
        SQLDataType.SMALLINTUNSIGNED,
        this,
        "");

    /**
     * The column <code>prism_activities.old_material_id</code>.
     */
    public final TableField<PrismActivitiesRecord, UShort> OLD_MATERIAL_ID = createField(
        DSL.name("old_material_id"),
        SQLDataType.SMALLINTUNSIGNED,
        this,
        "");

    /**
     * The column <code>prism_activities.entity_type_id</code>.
     */
    public final TableField<PrismActivitiesRecord, UShort> ENTITY_TYPE_ID = createField(
        DSL.name("entity_type_id"),
        SQLDataType.SMALLINTUNSIGNED,
        this,
        "");

    /**
     * The column <code>prism_activities.cause_id</code>.
     */
    public final TableField<PrismActivitiesRecord, UInteger> CAUSE_ID = createField(
        DSL.name("cause_id"),
        SQLDataType.INTEGERUNSIGNED.nullable(false),
        this,
        "");

    /**
     * The column <code>prism_activities.descriptor</code>.
     */
    public final TableField<PrismActivitiesRecord, String> DESCRIPTOR = createField(
        DSL.name("descriptor"),
        SQLDataType.VARCHAR(155),
        this,
        "");

    /**
     * The column <code>prism_activities.metadata</code>.
     */
    public final TableField<PrismActivitiesRecord, String> METADATA = createField(
        DSL.name("metadata"),
        SQLDataType.VARCHAR(255),
        this,
        "");

    /**
     * The column <code>prism_activities.serializer_version</code>.
     */
    public final TableField<PrismActivitiesRecord, UShort> SERIALIZER_VERSION = createField(
        DSL.name("serializer_version"),
        SQLDataType.SMALLINTUNSIGNED,
        this,
        "");

    /**
     * The column <code>prism_activities.serialized_data</code>.
     */
    public final TableField<PrismActivitiesRecord, String> SERIALIZED_DATA = createField(
        DSL.name("serialized_data"),
        SQLDataType.CLOB,
        this,
        "");

    /**
     * The column <code>prism_activities.reversed</code>.
     */
    public final TableField<PrismActivitiesRecord, Boolean> REVERSED = createField(
        DSL.name("reversed"),
        SQLDataType.BIT.nullable(false).defaultValue(false),
        this,
        "");

    private PrismActivities(String prefix, Name alias, Table<PrismActivitiesRecord> aliased) {
        this(prefix, alias, aliased, null);
    }

    private PrismActivities(String prefix, Name alias, Table<PrismActivitiesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
        this.prefix = prefix;
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     */
    public PrismActivities(String prefix) {
        this(prefix, DSL.name(prefix + "activities"), null);
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     * @param child The child table
     * @param key The key
     * @param <O> The record type
     */
    public <O extends Record> PrismActivities(String prefix, Table<O> child, ForeignKey<O, PrismActivitiesRecord> key) {
        super(child, key, PRISM_ACTIVITIES);

        this.prefix = prefix;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : PRISM_DATABASE;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(
            Indexes.PRISM_ACTIVITIES_ACTIONID,
            Indexes.PRISM_ACTIVITIES_CAUSEID,
            Indexes.PRISM_ACTIVITIES_COORDINATE,
            Indexes.PRISM_ACTIVITIES_ENTITYTYPEID,
            Indexes.PRISM_ACTIVITIES_MATERIALID,
            Indexes.PRISM_ACTIVITIES_OLDMATERIALID,
            Indexes.PRISM_ACTIVITIES_WORLDID);
    }

    @Override
    public Identity<PrismActivitiesRecord, UInteger> getIdentity() {
        return (Identity<PrismActivitiesRecord, UInteger>) super.getIdentity();
    }

    @Override
    public UniqueKey<PrismActivitiesRecord> getPrimaryKey() {
        return Keys.KEY_PRISM_ACTIVITIES_PRIMARY;
    }

    @Override
    public List<ForeignKey<PrismActivitiesRecord, ?>> getReferences() {
        return Arrays.asList(
            Keys.WORLDID,
            Keys.ACTIONID,
            Keys.MATERIALID,
            Keys.OLDMATERIALID,
            Keys.ENTITYTYPEID,
            Keys.CAUSEID);
    }

    private transient PrismWorlds prismWorlds;
    private transient PrismActions prismActions;
    private transient PrismMaterials materialid;
    private transient PrismMaterials oldmaterialid;
    private transient PrismEntityTypes prismEntityTypes;
    private transient PrismCauses prismCauses;

    /**
     * Get the implicit join path to the <code>prism_worlds</code>
     * table.
     */
    public PrismWorlds prismWorlds() {
        if (prismWorlds == null) {
            prismWorlds = new PrismWorlds(prefix, this, Keys.WORLDID);
        }

        return prismWorlds;
    }

    /**
     * Get the implicit join path to the <code>prism_actions</code>
     * table.
     */
    public PrismActions prismActions() {
        if (prismActions == null) {
            prismActions = new PrismActions(prefix, this, Keys.ACTIONID);
        }

        return prismActions;
    }

    /**
     * Get the implicit join path to the <code>prism_materials</code>
     * table, via the <code>materialId</code> key.
     */
    public PrismMaterials materialid() {
        if (materialid == null) {
            materialid = new PrismMaterials(prefix, this, Keys.MATERIALID);
        }

        return materialid;
    }

    /**
     * Get the implicit join path to the <code>prism_materials</code>
     * table, via the <code>oldMaterialId</code> key.
     */
    public PrismMaterials oldmaterialid() {
        if (oldmaterialid == null) {
            oldmaterialid = new PrismMaterials(prefix, this, Keys.OLDMATERIALID);
        }

        return oldmaterialid;
    }

    /**
     * Get the implicit join path to the
     * <code>prism_entity_types</code> table.
     */
    public PrismEntityTypes prismEntityTypes() {
        if (prismEntityTypes == null) {
            prismEntityTypes = new PrismEntityTypes(prefix, this, Keys.ENTITYTYPEID);
        }

        return prismEntityTypes;
    }

    /**
     * Get the implicit join path to the <code>prism_causes</code>
     * table.
     */
    public PrismCauses prismCauses() {
        if (prismCauses == null) {
            prismCauses = new PrismCauses(prefix, this, Keys.CAUSEID);
        }

        return prismCauses;
    }

    @Override
    public PrismActivities as(String alias) {
        return new PrismActivities(prefix, DSL.name(alias), this);
    }

    @Override
    public PrismActivities as(Name alias) {
        return new PrismActivities(prefix, alias, this);
    }

    @Override
    public PrismActivities rename(String name) {
        return new PrismActivities(prefix, DSL.name(name), null);
    }

    @Override
    public PrismActivities rename(Name name) {
        return new PrismActivities(prefix, name, null);
    }

    @Override
    public Row16<
        UInteger,
        UInteger,
        UByte,
        Integer,
        Integer,
        Integer,
        UByte,
        UShort,
        UShort,
        UShort,
        UInteger,
        String,
        String,
        Short,
        String,
        Boolean> fieldsRow() {
        return (Row16) super.fieldsRow();
    }
}
