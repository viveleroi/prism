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
import network.darkhelmet.prism.core.storage.dbo.records.PrismPlayersRecord;

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
import org.jooq.types.UInteger;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_DATABASE;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_PLAYERS;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismPlayers extends TableImpl<PrismPlayersRecord> {
    private static final long serialVersionUID = 1L;

    /**
     * The table prefix.
     */
    private final String prefix;

    /**
     * The class holding records for this type.
     */
    @Override
    public Class<PrismPlayersRecord> getRecordType() {
        return PrismPlayersRecord.class;
    }

    /**
     * The column <code>prism_players.player_id</code>.
     */
    public final TableField<PrismPlayersRecord, UInteger> PLAYER_ID = createField(
        DSL.name("player_id"),
        SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true),
        this,
        "");

    /**
     * The column <code>prism_players.player</code>.
     */
    public final TableField<PrismPlayersRecord, String> PLAYER = createField(
        DSL.name("player"),
        SQLDataType.VARCHAR(32).nullable(false),
        this,
        "");

    /**
     * The column <code>prism_players.player_uuid</code>.
     */
    public final TableField<PrismPlayersRecord, String> PLAYER_UUID = createField(
        DSL.name("player_uuid"),
        SQLDataType.VARCHAR(36).nullable(false),
        this,
        "");

    private PrismPlayers(String prefix, Name alias, Table<PrismPlayersRecord> aliased) {
        this(prefix, alias, aliased, null);
    }

    private PrismPlayers(String prefix, Name alias, Table<PrismPlayersRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());

        this.prefix = prefix;
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     */
    public PrismPlayers(String prefix) {
        this(prefix, DSL.name(prefix + "players"), null);
    }

    /**
     * Constructor.
     *
     * @param prefix The prefix
     * @param child The child table
     * @param key The key
     * @param <O> The record type
     */
    public <O extends Record> PrismPlayers(String prefix, Table<O> child, ForeignKey<O, PrismPlayersRecord> key) {
        super(child, key, PRISM_PLAYERS);

        this.prefix = prefix;
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : PRISM_DATABASE;
    }

    @Override
    public Identity<PrismPlayersRecord, UInteger> getIdentity() {
        return (Identity<PrismPlayersRecord, UInteger>) super.getIdentity();
    }

    @Override
    public UniqueKey<PrismPlayersRecord> getPrimaryKey() {
        return Keys.KEY_PRISM_PLAYERS_PRIMARY;
    }

    @Override
    public List<UniqueKey<PrismPlayersRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEY_PRISM_PLAYERS_PLAYER_UUID);
    }

    @Override
    public PrismPlayers as(String alias) {
        return new PrismPlayers(prefix, DSL.name(alias), this);
    }

    @Override
    public PrismPlayers as(Name alias) {
        return new PrismPlayers(prefix, alias, this);
    }

    @Override
    public PrismPlayers rename(String name) {
        return new PrismPlayers(prefix, DSL.name(name), null);
    }

    @Override
    public PrismPlayers rename(Name name) {
        return new PrismPlayers(prefix, name, null);
    }

    @Override
    public Row3<UInteger, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
