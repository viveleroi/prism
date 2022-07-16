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
import org.jooq.types.UInteger;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_CAUSES;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PrismCausesRecord extends UpdatableRecordImpl<PrismCausesRecord> implements
        Record3<UInteger, String, UInteger> {
    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>prism_causes.cause_id</code>.
     */
    public PrismCausesRecord setCauseId(UInteger value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>prism_causes.cause_id</code>.
     */
    public UInteger getCauseId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>prism_causes.cause</code>.
     */
    public PrismCausesRecord setCause(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>prism_causes.cause</code>.
     */
    public String getCause() {
        return (String) get(1);
    }

    /**
     * Setter for <code>prism_causes.player_id</code>.
     */
    public PrismCausesRecord setPlayerId(UInteger value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>prism_causes.player_id</code>.
     */
    public UInteger getPlayerId() {
        return (UInteger) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UInteger, String, UInteger> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UInteger, String, UInteger> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return PRISM_CAUSES.CAUSE_ID;
    }

    @Override
    public Field<String> field2() {
        return PRISM_CAUSES.CAUSE;
    }

    @Override
    public Field<UInteger> field3() {
        return PRISM_CAUSES.PLAYER_ID;
    }

    @Override
    public UInteger component1() {
        return getCauseId();
    }

    @Override
    public String component2() {
        return getCause();
    }

    @Override
    public UInteger component3() {
        return getPlayerId();
    }

    @Override
    public UInteger value1() {
        return getCauseId();
    }

    @Override
    public PrismCausesRecord value1(UInteger value) {
        setCauseId(value);
        return this;
    }

    @Override
    public String value2() {
        return getCause();
    }

    @Override
    public PrismCausesRecord value2(String value) {
        setCause(value);
        return this;
    }

    @Override
    public UInteger value3() {
        return getPlayerId();
    }

    @Override
    public PrismCausesRecord value3(UInteger value) {
        setPlayerId(value);
        return this;
    }

    @Override
    public PrismCausesRecord values(UInteger value1, String value2, UInteger value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PrismCausesRecord.
     */
    public PrismCausesRecord() {
        super(PRISM_CAUSES);
    }

    /**
     * Create a detached, initialised PrismCausesRecord.
     */
    public PrismCausesRecord(UInteger causeId, String cause, UInteger playerId) {
        super(PRISM_CAUSES);

        setCauseId(causeId);
        setCause(cause);
        setPlayerId(playerId);
    }
}
