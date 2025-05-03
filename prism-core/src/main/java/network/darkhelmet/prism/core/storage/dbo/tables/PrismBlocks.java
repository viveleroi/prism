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

import network.darkhelmet.prism.core.storage.dbo.Keys;
import network.darkhelmet.prism.core.storage.dbo.records.PrismBlocksRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row5;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_BLOCKS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_DATABASE;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismBlocks extends TableImpl<PrismBlocksRecord> {
    private static final long serialVersionUID = 1L;

    /**
     * The table prefix.
     */
    private final String prefix;

    /**
     * The class holding records for this type.
     */
    @Override
    public Class<PrismBlocksRecord> getRecordType() {
        return PrismBlocksRecord.class;
    }

    /**
     * The column <code>prism_blocks.block_id</code>.
     */
    public final TableField<PrismBlocksRecord, UInteger> BLOCK_ID = createField(
        DSL.name("block_id"),
        SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true),
        this,
        "");

    /**
     * The column <code>prism_blocks.ns</code>.
     */
    public final TableField<PrismBlocksRecord, String> NS = createField(
        DSL.name("ns"),
        SQLDataType.VARCHAR(55),
        this,
        "");

    /**
     * The column <code>prism_blocks.name</code>.
     */
    public final TableField<PrismBlocksRecord, String> NAME = createField(
        DSL.name("name"),
        SQLDataType.VARCHAR(55),
        this,
        "");

    /**
     * The column <code>prism_blocks.data</code>.
     */
    public final TableField<PrismBlocksRecord, String> DATA = createField(
        DSL.name("data"),
        SQLDataType.VARCHAR(255),
        this,
        "");

    /**
     * The column <code>prism_blocks.translation_key</code>.
     */
    public final TableField<PrismBlocksRecord, String> TRANSLATION_KEY = createField(
        DSL.name("translation_key"),
        SQLDataType.VARCHAR(155),
        this,
        "");

    private PrismBlocks(String prefix, Name alias, Table<PrismBlocksRecord> aliased) {
        this(prefix, alias, aliased, null);
    }

    private PrismBlocks(String prefix, Name alias, Table<PrismBlocksRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());

        this.prefix = prefix;
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     */
    public PrismBlocks(String prefix) {
        this(prefix, DSL.name(prefix + "blocks"), null);
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     * @param child The child table
     * @param key The key
     * @param <O> The record type
     */
    public <O extends Record> PrismBlocks(String prefix, Table<O> child, ForeignKey<O, PrismBlocksRecord> key) {
        super(child, key, PRISM_BLOCKS);

        this.prefix = prefix;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : PRISM_DATABASE;
    }

    @Override
    public Identity<PrismBlocksRecord, UShort> getIdentity() {
        return (Identity<PrismBlocksRecord, UShort>) super.getIdentity();
    }

    @Override
    public UniqueKey<PrismBlocksRecord> getPrimaryKey() {
        return Keys.KEY_PRISM_BLOCKS_PRIMARY;
    }

    @Override
    public List<UniqueKey<PrismBlocksRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEY_PRISM_BLOCKS_BLOCK);
    }

    @Override
    public PrismBlocks as(String alias) {
        return new PrismBlocks(prefix, DSL.name(alias), this);
    }

    @Override
    public PrismBlocks as(Name alias) {
        return new PrismBlocks(prefix, alias, this);
    }

    @Override
    public PrismBlocks rename(String name) {
        return new PrismBlocks(prefix, DSL.name(name), null);
    }

    @Override
    public PrismBlocks rename(Name name) {
        return new PrismBlocks(prefix, name, null);
    }

    @Override
    public Row5<UInteger, String, String, String, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }
}
