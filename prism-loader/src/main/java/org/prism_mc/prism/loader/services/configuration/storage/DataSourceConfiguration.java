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

package org.prism_mc.prism.loader.services.configuration.storage;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class DataSourceConfiguration {

    @Comment("Set the max number of records saved to storage per batch.")
    private int batchMax = 500;

    @Comment("Configure the database name.")
    private String database = "prism";

    @Comment("Enter the prefix prism should use for database table names. i.e. prism_activities.")
    private String prefix = "prism_";

    /**
     * Get the catalog name.
     * <p>
     * In MySQL/MariaDB, "databases" are effectively both "catalog" and "schema", however that
     * isn't the case in Postgres.
     * </p>
     * @return The catalog name
     */
    public String catalog() {
        return "";
    }

    /**
     * Get the schema name.
     * <p>
     * In MySQL/MariaDB, "databases" are effectively both "catalog" and "schema", however that
     * isn't the case in Postgres.
     * </p>
     * @return The schema name
     */
    public String schema() {
        return database;
    }
}
