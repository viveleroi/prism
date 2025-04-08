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

package network.darkhelmet.prism.core.storage.dbo;

import java.io.Serial;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

public class PrismDatabase extends SchemaImpl {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * All tables.
     */
    private final List<Table<?>> tables;

    /**
     * Constructor.
     *
     * @param database The database name
     * @param tables All tables
     */
    public PrismDatabase(DefaultCatalog catalog, String database, List<Table<?>> tables) {
        super(database, catalog);

        this.tables = tables;
    }

    @Override
    public final @NotNull List<Table<?>> getTables() {
        return tables;
    }
}
