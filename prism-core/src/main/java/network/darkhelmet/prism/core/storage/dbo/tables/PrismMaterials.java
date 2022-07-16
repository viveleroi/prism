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

package network.darkhelmet.prism.core.storage.dbo.tables;

import java.util.Arrays;
import java.util.List;

import network.darkhelmet.prism.core.storage.dbo.Keys;
import network.darkhelmet.prism.core.storage.dbo.records.PrismMaterialsRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UShort;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_DATABASE;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_MATERIALS;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismMaterials extends TableImpl<PrismMaterialsRecord> {
    private static final long serialVersionUID = 1L;

    /**
     * The table prefix.
     */
    private final String prefix;

    /**
     * The class holding records for this type.
     */
    @Override
    public Class<PrismMaterialsRecord> getRecordType() {
        return PrismMaterialsRecord.class;
    }

    /**
     * The column <code>prism_materials.material_id</code>.
     */
    public final TableField<PrismMaterialsRecord, UShort> MATERIAL_ID = createField(
        DSL.name("material_id"),
        SQLDataType.SMALLINTUNSIGNED.nullable(false).identity(true),
        this,
        "");

    /**
     * The column <code>prism_materials.material</code>.
     */
    public final TableField<PrismMaterialsRecord, String> MATERIAL = createField(
        DSL.name("material"),
        SQLDataType.VARCHAR(45),
        this,
        "");

    /**
     * The column <code>prism_materials.data</code>.
     */
    public final TableField<PrismMaterialsRecord, String> DATA = createField(
        DSL.name("data"),
        SQLDataType.VARCHAR(155),
        this,
        "");

    private PrismMaterials(String prefix, Name alias, Table<PrismMaterialsRecord> aliased) {
        this(prefix, alias, aliased, null);
    }

    private PrismMaterials(String prefix, Name alias, Table<PrismMaterialsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());

        this.prefix = prefix;
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     */
    public PrismMaterials(String prefix) {
        this(prefix, DSL.name(prefix + "materials"), null);
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     * @param child The child table
     * @param key The key
     * @param <O> The record type
     */
    public <O extends Record> PrismMaterials(String prefix, Table<O> child, ForeignKey<O, PrismMaterialsRecord> key) {
        super(child, key, PRISM_MATERIALS);

        this.prefix = prefix;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : PRISM_DATABASE;
    }

    @Override
    public Identity<PrismMaterialsRecord, UShort> getIdentity() {
        return (Identity<PrismMaterialsRecord, UShort>) super.getIdentity();
    }

    @Override
    public UniqueKey<PrismMaterialsRecord> getPrimaryKey() {
        return Keys.KEY_PRISM_MATERIALS_PRIMARY;
    }

    @Override
    public List<UniqueKey<PrismMaterialsRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEY_PRISM_MATERIALS_MATERIALDATA);
    }

    @Override
    public PrismMaterials as(String alias) {
        return new PrismMaterials(prefix, DSL.name(alias), this);
    }

    @Override
    public PrismMaterials as(Name alias) {
        return new PrismMaterials(prefix, alias, this);
    }

    @Override
    public PrismMaterials rename(String name) {
        return new PrismMaterials(prefix, DSL.name(name), null);
    }

    @Override
    public PrismMaterials rename(Name name) {
        return new PrismMaterials(prefix, name, null);
    }

    @Override
    public Row3<UShort, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
