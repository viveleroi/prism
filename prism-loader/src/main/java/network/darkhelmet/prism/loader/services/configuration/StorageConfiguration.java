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

package network.darkhelmet.prism.loader.services.configuration;

import lombok.Getter;
import lombok.Setter;

import network.darkhelmet.prism.loader.storage.StorageType;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class StorageConfiguration {
    @Comment("Set the max number of records saved to storage per batch.")
    private int batchMax = 500;

    @Comment("Set the datasource. This determines which storage system is used.\n"
        + "Available options: mysql")
    private StorageType datasource = StorageType.MYSQL;

    @Comment("Configure the database name.")
    private String database = "prism";

    @Comment("Configure the hostname.")
    private String host = "localhost";

    // Configuration is now managed automatically.
    @Setter
    private transient boolean mysqlDeprecated = false;

    @Comment("Enter the password, if the selected datasource uses authentication.")
    private String password = "";

    @Comment("Configure the port.")
    private String port = "3306";

    @Comment("Enter the prefix prism should use for database table names. i.e. prism_activities.")
    private String prefix = "prism_";

    @Comment("Enable query spy. This logs queries and helpful debug information.\n"
        + "Used primarily for development and debugging. Use carefully.")
    private boolean spy = false;

    @Comment("Enter the username, if the selected datasource uses authentication.")
    private String username = "root";

    @Comment("Toggle recommended Hikari MySQL datasource optimizations.\n"
        + "You can read about these here: https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration\n"
        + "Naturally, this setting is useless for non-MySQL storage.")
    private boolean useHikariMysqlOptimizations = true;

    @Comment("Enable stored procedures. Stored procedures allow Prism to modify database records\n"
        + "more efficiently and with reduced network traffic.\n"
        + "However, your MySQL account must have privileges to `CREATE ROUTINE`.\n"
        + "If you use a shared MySQL database, you likely do NOT have such permission.\n"
        + "If you're unsure, Prism tells you in the server console during server startup.\n"
        + "Prism will force disable this setting if you do not have necessary permission.")
    private boolean useStoredProcedures = true;

    /**
     * Get the batch max.
     *
     * @return The batch max
     */
    public int batchMax() {
        return batchMax;
    }

    /**
     * Get the datasource setting.
     *
     * @return The datasource
     */
    public StorageType datasource() {
        return datasource;
    }

    /**
     * Disable the setting for using stored procedures.
     */
    public void disallowStoredProcedures() {
        this.useStoredProcedures = false;
    }
}
