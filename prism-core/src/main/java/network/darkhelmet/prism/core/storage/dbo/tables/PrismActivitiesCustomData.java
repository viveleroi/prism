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
import network.darkhelmet.prism.core.storage.dbo.records.PrismActivitiesCustomDataRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES_CUSTOM_DATA;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_DATABASE;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismActivitiesCustomData extends TableImpl<PrismActivitiesCustomDataRecord> {
    private static final long serialVersionUID = 1L;

    /**
     * The table prefix.
     */
    private final String prefix;

    /**
     * The class holding records for this type.
     */
    @Override
    public Class<PrismActivitiesCustomDataRecord> getRecordType() {
        return PrismActivitiesCustomDataRecord.class;
    }

    /**
     * The column <code>prism_activities_custom_data.extra_id</code>.
     */
    public final TableField<PrismActivitiesCustomDataRecord, UInteger> EXTRA_ID = createField(
        DSL.name("extra_id"),
        SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true),
        this,
        "");

    /**
     * The column
     * <code>prism_activities_custom_data.activity_id</code>.
     */
    public final TableField<PrismActivitiesCustomDataRecord, UInteger> ACTIVITY_ID = createField(
        DSL.name("activity_id"),
        SQLDataType.INTEGERUNSIGNED.nullable(false),
        this,
        "");

    /**
     * The column <code>prism_activities_custom_data.version</code>.
     */
    public final TableField<PrismActivitiesCustomDataRecord, Short> VERSION = createField(
        DSL.name("version"),
        SQLDataType.SMALLINT,
        this,
        "");

    /**
     * The column <code>prism_activities_custom_data.data</code>.
     */
    public final TableField<PrismActivitiesCustomDataRecord, String> DATA = createField(
        DSL.name("data"),
        SQLDataType.CLOB,
        this,
        "");

    private PrismActivitiesCustomData(String prefix, Name alias, Table<PrismActivitiesCustomDataRecord> aliased) {
        this(prefix, alias, aliased, null);
    }

    private PrismActivitiesCustomData(
            String prefix, Name alias, Table<PrismActivitiesCustomDataRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
        this.prefix = prefix;
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     */
    public PrismActivitiesCustomData(String prefix) {
        this(prefix, DSL.name(prefix + "activities_custom_data"), null);
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     * @param child The child table
     * @param key The key
     * @param <O> The record type
     */
    public <O extends Record> PrismActivitiesCustomData(
            String prefix, Table<O> child, ForeignKey<O, PrismActivitiesCustomDataRecord> key) {
        super(child, key, PRISM_ACTIVITIES_CUSTOM_DATA);

        this.prefix = prefix;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : PRISM_DATABASE;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.PRISM_ACTIVITIES_CUSTOM_DATA_ACTIVITYID);
    }

    @Override
    public Identity<PrismActivitiesCustomDataRecord, UInteger> getIdentity() {
        return (Identity<PrismActivitiesCustomDataRecord, UInteger>) super.getIdentity();
    }

    @Override
    public UniqueKey<PrismActivitiesCustomDataRecord> getPrimaryKey() {
        return Keys.KEY_PRISM_ACTIVITIES_CUSTOM_DATA_PRIMARY;
    }

    @Override
    public List<ForeignKey<PrismActivitiesCustomDataRecord, ?>> getReferences() {
        return Arrays.asList(Keys.ACTIVITYID);
    }

    private transient PrismActivities prismActivities;

    /**
     * Get the implicit join path to the
     * <code>prism_activities</code> table.
     */
    public PrismActivities prismActivities() {
        if (prismActivities == null) {
            prismActivities = new PrismActivities(prefix, this, Keys.ACTIVITYID);
        }

        return prismActivities;
    }

    @Override
    public PrismActivitiesCustomData as(String alias) {
        return new PrismActivitiesCustomData(prefix, DSL.name(alias), this);
    }

    @Override
    public PrismActivitiesCustomData as(Name alias) {
        return new PrismActivitiesCustomData(prefix, alias, this);
    }

    @Override
    public PrismActivitiesCustomData rename(String name) {
        return new PrismActivitiesCustomData(prefix, DSL.name(name), null);
    }

    @Override
    public PrismActivitiesCustomData rename(Name name) {
        return new PrismActivitiesCustomData(prefix, name, null);
    }

    @Override
    public Row4<UInteger, UInteger, Short, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
