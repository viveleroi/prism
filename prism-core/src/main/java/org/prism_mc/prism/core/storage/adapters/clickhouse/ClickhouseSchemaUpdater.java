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

package org.prism_mc.prism.core.storage.adapters.clickhouse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.prism_mc.prism.loader.services.logging.LoggingService;

public class ClickhouseSchemaUpdater {

    /**
     * The current/latest ClickHouse schema version for fresh installations. Uses the same 4xx numbering
     * as the normalized {@code SqlSchemaUpdater} so schema versions read consistently across backends.
     */
    public static final String CURRENT_SCHEMA_VERSION = "400";

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * Construct the updater.
     *
     * @param loggingService The logging service
     */
    public ClickhouseSchemaUpdater(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Create the meta table if absent, then read the stored schema version and reconcile it with the
     * current version: record the current version on a fresh install, run pending migrations when the
     * stored version is older, and reject a database that is newer than this plugin supports.
     *
     * @param connection The database connection
     * @param prefix The schema/table prefix
     * @throws SQLException The database exception
     */
    public void update(Connection connection, String prefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                String.format(
                    "CREATE TABLE IF NOT EXISTS %smeta (k String, v String, updated UInt64) " +
                    "ENGINE = ReplacingMergeTree(updated) ORDER BY k",
                    prefix
                )
            );
        }

        String stored = readVersion(connection, prefix);
        if (stored == null) {
            writeVersion(connection, prefix, CURRENT_SCHEMA_VERSION);
            return;
        }

        int storedVersion = Integer.parseInt(stored);
        int currentVersion = Integer.parseInt(CURRENT_SCHEMA_VERSION);

        if (storedVersion > currentVersion) {
            throw new IllegalStateException(
                String.format(
                    "ClickHouse schema version (%s) is newer than this version of Prism supports (%s). " +
                    "Please update Prism.",
                    stored,
                    CURRENT_SCHEMA_VERSION
                )
            );
        }

        if (storedVersion < currentVersion) {
            migrate(connection, prefix, storedVersion, currentVersion);
            writeVersion(connection, prefix, CURRENT_SCHEMA_VERSION);
        }
    }

    /**
     * Read the stored schema version from the meta table.
     *
     * @param connection The database connection
     * @param prefix The schema/table prefix
     * @return The stored version string, or null if none has been recorded
     * @throws SQLException The database exception
     */
    private String readVersion(Connection connection, String prefix) throws SQLException {
        try (
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                String.format("SELECT v FROM %smeta FINAL WHERE k = 'schema_ver'", prefix)
            )
        ) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }

        return null;
    }

    /**
     * Record the schema version in the meta table.
     *
     * @param connection The database connection
     * @param prefix The schema/table prefix
     * @param version The version to store
     * @throws SQLException The database exception
     */
    private void writeVersion(Connection connection, String prefix, String version) throws SQLException {
        try (
            PreparedStatement statement = connection.prepareStatement(
                String.format("INSERT INTO %smeta (k, v, updated) VALUES ('schema_ver', ?, ?)", prefix)
            )
        ) {
            statement.setString(1, version);
            statement.setLong(2, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    /**
     * Apply schema migrations.
     *
     * @param connection The database connection
     * @param prefix The schema/table prefix
     * @param fromVersion The stored version
     * @param toVersion The target version
     * @throws SQLException The database exception
     */
    private void migrate(Connection connection, String prefix, int fromVersion, int toVersion) throws SQLException {
        for (int version = fromVersion + 1; version <= toVersion; version++) {
            loggingService.info("Applying schema migration for version {0}", version);
        }
    }
}
