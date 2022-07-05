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

    @Comment("Set the jdbc driver class. If using Java 8/Connector/J 5.1 API, use `com.mysql.jdbc.Driver`.")
    private String driver = "com.mysql.cj.jdbc.Driver";

    @Comment("Configure the hostname.")
    private String host = "localhost";

    @Comment("Enter the password, if the selected datasource uses authentication.")
    private String password = "";

    @Comment("Configure the port.")
    private String port = "3306";

    @Comment("Enter the prefix prism should use for database table names. i.e. prism_activities.")
    private String prefix = "prism_";

    @Comment("Enter the username, if the selected datasource uses authentication.")
    private String username = "root";

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
     * Get the driver class.
     *
     * @return The driver class
     */
    public String driver() {
        return driver;
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
     * Get the username.
     *
     * @return The username
     */
    public String username() {
        return username;
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
