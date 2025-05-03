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

package org.prism_mc.prism.core.storage.dbo;

import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_DATABASE;

import java.io.Serial;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jooq.Schema;
import org.jooq.impl.CatalogImpl;

public class DefaultCatalog extends CatalogImpl {

    @Serial
    private static final long serialVersionUID = 1L;

    public DefaultCatalog(String name) {
        super(name);
    }

    @Override
    public final @NotNull List<Schema> getSchemas() {
        return List.of(PRISM_DATABASE);
    }
}
