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

package network.darkhelmet.prism.core.services.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class StorageConfiguration {
    @Comment("Set the max number of records saved to storage per batch.")
    private int batchMax = 500;

    @Comment("Set the datasource. This determines which storage system is used.\n"
        + "Available options: mysql")
    private String datasource = "mysql";

    @Comment("Configure the database name.")
    private String database = "prism";

    @Comment("Configure the hostname.")
    private String host = "localhost";

    @Comment("We strongly recommend using MySQL 8+ or MariaDB 10.2+. Prism uses some newer features,\n"
        + "but we realize you may be limited to older databases. If so, set this to true.\n"
        + "Please see our docs for details on what this does.")
    private boolean mysqlDeprecated = false;

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
    public String datasource() {
        return datasource;
    }

    /**
     * Get the database name.
     *
     * @return The database name
     */
    public String database() {
        return database;
    }

    /**
     * Get the host.
     *
     * @return The host
     */
    public String host() {
        return host;
    }

    /**
     * Get whether to use older mysql/deprecated features.
     *
     * @return True if using older features
     */
    public boolean mysqlDeprecated() {
        return mysqlDeprecated;
    }

    /**
     * Get the password.
     *
     * @return The password
     */
    public String password() {
        return password;
    }

    /**
     * Get the port.
     *
     * @return The port
     */
    public String port() {
        return port;
    }

    /**
     * Get the prefix.
     *
     * @return The prefix
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Get whether to spy.
     *
     * @return True if spying
     */
    public boolean spy() {
        return spy;
    }

    /**
     * Get the username.
     *
     * @return The username
     */
    public String username() {
        return username;
    }

    /**
     * Get whether to use default hikari mysql optimizations.
     *
     * @return Whether to use default hikari mysql optimizations
     */
    public boolean useHikariMysqlOptimizations() {
        return useHikariMysqlOptimizations;
    }

    /**
     * Get whether to use stored procedures.
     *
     * @return Whether to use stored procedures
     */
    public boolean useStoredProcedures() {
        return useStoredProcedures;
    }

    /**
     * Disable the setting for using stored procedures.
     */
    public void disallowStoredProcedures() {
        this.useStoredProcedures = false;
    }
}
