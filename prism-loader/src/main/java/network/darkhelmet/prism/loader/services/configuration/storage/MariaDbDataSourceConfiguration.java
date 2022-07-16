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

package network.darkhelmet.prism.loader.services.configuration.storage;

import lombok.Getter;
import lombok.Setter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class MariaDbDataSourceConfiguration extends SqlDataSourceConfiguration {
    // Deprecated configuration is managed automatically.
    @Setter
    private transient boolean useDeprecated = false;

    @Comment("Toggle recommended Hikari Maria/MySQL datasource optimizations.\n"
            + "You can read about these here: https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration")
    private boolean useHikariOptimizations = true;

    @Comment("Enable stored procedures. Stored procedures allow Prism to modify database records\n"
            + "more efficiently and with reduced network traffic.\n"
            + "However, your MariaDB account must have privileges to `CREATE ROUTINE`.\n"
            + "If you use a shared MariaDB database, you likely do NOT have such permission.\n"
            + "If you're unsure, Prism tells you in the server console during server startup.\n"
            + "Prism will force disable this setting if you do not have necessary permission.")
    private boolean useStoredProcedures = true;

    /**
     * Constructor.
     */
    public MariaDbDataSourceConfiguration() {
        super("3306", "root");
    }

    /**
     * Disable the setting for using stored procedures.
     */
    public void disallowStoredProcedures() {
        this.useStoredProcedures = false;
    }
}
