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

package network.darkhelmet.prism.core.storage.adapters.postgres;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import network.darkhelmet.prism.api.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.storage.ActivityBatch;
import network.darkhelmet.prism.core.injection.factories.SqlActivityQueryBuilderFactory;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.core.storage.HikariConfigFactory;
import network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlActivityProcedureBatch;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlSchemaUpdater;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.jooq.SQLDialect;

@Singleton
public class PostgresStorageAdapter extends AbstractSqlStorageAdapter {
    /**
     * The schema/table prefix.
     */
    private String prefix;

    /**
     * Constructor.
     *
     * @param loggingService The logging service
     * @param configurationService The configuration service
     * @param actionRegistry The action type registry
     * @param schemaUpdater The schema updater
     * @param cacheService The cache service
     * @param queryBuilderFactory The query builder factory
     * @param serializerVersion The serializer version
     * @param dataPath The plugin file path
     */
    @Inject
    public PostgresStorageAdapter(
            LoggingService loggingService,
            ConfigurationService configurationService,
            ActionTypeRegistry actionRegistry,
            SqlSchemaUpdater schemaUpdater,
            SqlActivityQueryBuilderFactory queryBuilderFactory,
            CacheService cacheService,
            @Named("serializerVersion") short serializerVersion,
            Path dataPath) {
        super(
            loggingService,
            configurationService,
            actionRegistry,
            schemaUpdater,
            queryBuilderFactory,
            cacheService,
            serializerVersion);

        try {
            prefix = configurationService.storageConfig().postgres().prefix();

            // First, try to use any hikari.properties
            File hikariPropertiesFile = new File(dataPath.toFile(), "hikari.properties");
            if (hikariPropertiesFile.exists()) {
                loggingService.info("Using hikari.properties over storage.conf");

                if (connect(new HikariConfig(hikariPropertiesFile.getPath()), SQLDialect.POSTGRES)) {
                    describeDatabase(true);
                    prepareSchema();
                    prepareCache();

                    ready = true;
                }
            } else {
                loggingService.info("Reading storage.conf. There is no hikari.properties file.");

                if (connect(HikariConfigFactory.postgres(configurationService.storageConfig()), SQLDialect.POSTGRES)) {
                    describeDatabase(false);
                    prepareSchema();
                    prepareCache();

                    ready = true;
                }
            }
        } catch (Exception e) {
            loggingService.handleException(e);
        }
    }

    @Override
    protected void describeDatabase(boolean usingHikariProperties) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            String databaseProduct = databaseMetaData.getDatabaseProductName();
            String databaseVersion = databaseMetaData.getDatabaseProductVersion();

            loggingService.info("Database: {0} {1}", databaseProduct, databaseVersion);

            var usingStoredProcedures = false;
            if (configurationService.storageConfig().postgres().useStoredProcedures()) {
                boolean supportsProcedures = databaseMetaData.supportsStoredProcedures();
                loggingService.info("supports procedures: {0}", supportsProcedures);

                var canCreateFunctions = create.fetchSingle(
                    "SELECT bool_or(has_schema_privilege(oid, 'CREATE')) FROM pg_catalog.pg_namespace;")
                        .into(Boolean.class);
                loggingService.info("can create functions: {0}", canCreateFunctions);

                usingStoredProcedures = supportsProcedures && canCreateFunctions
                    && configurationService.storageConfig().postgres().useStoredProcedures();

                if (!usingStoredProcedures) {
                    configurationService.storageConfig().postgres().disallowStoredProcedures();
                }
            }

            loggingService.info("using stored procedures: {0}", usingStoredProcedures);
        }
    }

    @Override
    protected void prepareSchema() throws Exception {
        create.setSchema(configurationService.storageConfig().postgres().schema()).execute();

        super.prepareSchema();

        if (configurationService.storageConfig().postgres().useStoredProcedures()) {
            try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {
                // Drop procedures just in case the parameters change, if so OR REPLACE won't work
                stmt.execute(String.format("DROP FUNCTION IF EXISTS %screate_activity", prefix));
                stmt.execute(String.format("DROP FUNCTION IF EXISTS %sget_or_create_action", prefix));
                stmt.execute(String.format("DROP FUNCTION IF EXISTS %sget_or_create_cause", prefix));
                stmt.execute(String.format("DROP FUNCTION IF EXISTS %sget_or_create_entity_type", prefix));
                stmt.execute(String.format("DROP FUNCTION IF EXISTS %sget_or_create_material", prefix));
                stmt.execute(String.format("DROP FUNCTION IF EXISTS %sget_or_create_player", prefix));
                stmt.execute(String.format("DROP FUNCTION IF EXISTS %sget_or_create_world", prefix));

                stmt.execute(loadSqlFromResourceFile("postgres", "prism_get_or_create_action", prefix));
                stmt.execute(loadSqlFromResourceFile("postgres", "prism_get_or_create_cause", prefix));
                stmt.execute(loadSqlFromResourceFile("postgres", "prism_get_or_create_entity_type", prefix));
                stmt.execute(loadSqlFromResourceFile("postgres", "prism_get_or_create_material", prefix));
                stmt.execute(loadSqlFromResourceFile("postgres", "prism_get_or_create_player", prefix));
                stmt.execute(loadSqlFromResourceFile("postgres", "prism_get_or_create_world", prefix));
                stmt.execute(loadSqlFromResourceFile("postgres", "prism_create_activity", prefix));
            }
        }
    }

    @Override
    public ActivityBatch createActivityBatch() {
        if (configurationService.storageConfig().postgres().useStoredProcedures()) {
            return new SqlActivityProcedureBatch(loggingService, dataSource, serializerVersion, prefix);
        }

        return super.createActivityBatch();
    }
}
