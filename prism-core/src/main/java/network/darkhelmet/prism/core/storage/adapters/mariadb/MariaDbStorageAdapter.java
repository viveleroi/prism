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

package network.darkhelmet.prism.core.storage.adapters.mariadb;

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
import java.util.List;
import java.util.Map;

import network.darkhelmet.prism.api.actions.types.IActionTypeRegistry;
import network.darkhelmet.prism.api.storage.IActivityBatch;
import network.darkhelmet.prism.core.injection.factories.ISqlActivityQueryBuilderFactory;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.core.storage.HikariConfigFactory;
import network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlActivityProcedureBatch;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlSchemaUpdater;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.intellij.lang.annotations.Language;
import org.jooq.SQLDialect;

@Singleton
public class MariaDbStorageAdapter extends AbstractSqlStorageAdapter {
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
    public MariaDbStorageAdapter(
            LoggingService loggingService,
            ConfigurationService configurationService,
            IActionTypeRegistry actionRegistry,
            SqlSchemaUpdater schemaUpdater,
            ISqlActivityQueryBuilderFactory queryBuilderFactory,
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
            // First, try to use any hikari.properties
            File hikariPropertiesFile = new File(dataPath.toFile(), "hikari.properties");
            if (hikariPropertiesFile.exists()) {
                loggingService.logger().info("Using hikari.properties over storage.conf");

                if (connect(new HikariConfig(hikariPropertiesFile.getPath()), SQLDialect.MARIADB)) {
                    describeDatabase(true);
                    prepareSchema();

                    if (!configurationService.storageConfig().mariadb().useStoredProcedures()) {
                        prepareCache();
                    }

                    ready = true;
                }
            } else {
                loggingService.logger().info("Reading storage.conf. There is no hikari.properties file.");

                if (connect(HikariConfigFactory.mariadb(configurationService.storageConfig()), SQLDialect.MARIADB)) {
                    describeDatabase(false);
                    prepareSchema();

                    if (!configurationService.storageConfig().mariadb().useStoredProcedures()) {
                        prepareCache();
                    }

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

            String versionMsg = String.format("Database: %s %s", databaseProduct, databaseVersion);
            loggingService.logger().info(versionMsg);

            int majorVersion = databaseMetaData.getDatabaseMajorVersion();
            int minorVersion = databaseMetaData.getDatabaseMinorVersion();

            configurationService.storageConfig().mariadb().useDeprecated(majorVersion < 10 || minorVersion < 2);

            if (configurationService.storageConfig().mariadb().useDeprecated()) {
                String updateMsg = "Using older/deprecated database features. We strongly recommend using"
                    + " MySQL 8+ or MariaDB 10.2+";
                loggingService.logger().info(updateMsg);
            }

            if (configurationService.storageConfig().mariadb().useStoredProcedures()) {
                boolean supportsProcedures = databaseMetaData.supportsStoredProcedures();

                List<String> grants = create.fetch("SHOW GRANTS FOR CURRENT_USER();").into(String.class);
                boolean canCreateRoutines = grants.get(0).contains("CREATE ROUTINE");
                loggingService.logger().info(String.format("can create routines: %b", canCreateRoutines));

                if (supportsProcedures || !canCreateRoutines) {
                    configurationService.storageConfig().mariadb().disallowStoredProcedures();
                }
            }

            Map<String, String> dbVars = create.fetch("SHOW VARIABLES").intoMap(
                r -> r.get(0, String.class),
                r -> r.get(1, String.class)
            );

            long innodbSizeMb = Long.parseLong(dbVars.get("innodb_buffer_pool_size")) / 1024 / 1024;
            loggingService.logger().info(String.format("innodb_buffer_pool_size: %d", innodbSizeMb));
            loggingService.logger().info(String.format("sql_mode: %s", dbVars.get("sql_mode")));

            if (!usingHikariProperties) {
                boolean usrHikariOptimizations = configurationService.storageConfig()
                    .mariadb().useHikariOptimizations();
                loggingService.logger().info(
                    String.format("use hikari optimizations: %b", usrHikariOptimizations));
            }
        }
    }

    @Override
    protected void prepareSchema() throws Exception {
        super.prepareSchema();

        String prefix = configurationService.storageConfig().mariadb().prefix();

        if (configurationService.storageConfig().mariadb().useStoredProcedures()) {
            try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {
                // Drop procedures first because MariaDB doesn't support IF NOT EXISTS in CREATE PROCEDURE
                // MySQL does, but only in 8.0.29+
                @Language("SQL") String dropActionsProcedure = "DROP PROCEDURE IF EXISTS getOrCreateAction";
                stmt.execute(dropActionsProcedure);

                // Create the get-or-create Actions procedure
                @Language("SQL") String actionsProcedure = "CREATE PROCEDURE getOrCreateAction "
                    + "(IN `actionKey` VARCHAR(25), OUT `actionId` TINYINT) "
                    + "BEGIN "
                    + "    SELECT action_id INTO `actionId` FROM " + prefix + "actions WHERE action = `actionKey`; "
                    + "    IF `actionId` IS NULL THEN "
                    + "        INSERT INTO " + prefix + "actions (`action`) VALUES (`actionKey`); "
                    + "        SET `actionId` = LAST_INSERT_ID(); "
                    + "    END IF; "
                    + "END";
                stmt.execute(actionsProcedure);

                // Cause procedure
                @Language("SQL") String dropCauseProcedure = "DROP PROCEDURE IF EXISTS getOrCreateCause";
                stmt.execute(dropCauseProcedure);

                // Create the get-or-create Cause procedure
                @Language("SQL") String causeProcedure = "CREATE PROCEDURE getOrCreateCause "
                    + "(IN `causeStr` VARCHAR(25), IN `playerId` INT, OUT `causeId` INT) "
                    + "BEGIN "
                    + "    IF `playerId` IS NOT NULL THEN "
                    + "        SELECT cause_id INTO `causeId` FROM "
                    + prefix + "causes WHERE player_id = `playerId`; "
                    + "    ELSEIF `causeStr` IS NOT NULL THEN "
                    + "        SELECT cause_id INTO `causeId` FROM " + prefix + "causes WHERE cause = `causeStr`; "
                    + "    END IF; "
                    + "    IF `causeId` IS NULL THEN "
                    + "        INSERT INTO "
                    + prefix + "causes (`cause`, `player_id`) VALUES (`causeStr`, `playerId`); "
                    + "        SET `causeId` = LAST_INSERT_ID(); "
                    + "    END IF; "
                    + "END";
                stmt.execute(causeProcedure);

                // EntityType procedure
                @Language("SQL") String dropEntityTypeProcedure = "DROP PROCEDURE IF EXISTS getOrCreateEntityType";
                stmt.execute(dropEntityTypeProcedure);

                // Create the get-or-create Entity type procedure
                @Language("SQL") String entityTypeProcedure = "CREATE PROCEDURE getOrCreateEntityType "
                    + "(IN `entityType` VARCHAR(25), OUT `entityTypeId` SMALLINT) "
                    + "BEGIN "
                    + "    SELECT entity_type_id INTO `entityTypeId` FROM "
                    + prefix + "entity_types WHERE entity_type = `entityType`; "
                    + "    IF `entityTypeId` IS NULL THEN "
                    + "        INSERT INTO " + prefix + "entity_types (`entity_type`) VALUES (`entityType`); "
                    + "        SET `entityTypeId` = LAST_INSERT_ID(); "
                    + "    END IF; "
                    + "END";
                stmt.execute(entityTypeProcedure);

                // Material procedure
                @Language("SQL") String dropMaterialProcedure = "DROP PROCEDURE IF EXISTS getOrCreateMaterial";
                stmt.execute(dropMaterialProcedure);

                // Create the get-or-create Material type procedure
                @Language("SQL") String materialProcedure = "CREATE PROCEDURE getOrCreateMaterial "
                    + "(IN `materialKey` VARCHAR(45), IN `blockData` VARCHAR(155), OUT `materialId` SMALLINT) "
                    + "BEGIN "
                    + "    IF blockData IS NOT NULL THEN "
                    + "        SELECT material_id INTO `materialId` FROM "
                    + prefix + "materials WHERE material = `materialKey` AND data = `blockData`; "
                    + "    ELSE "
                    + "        SELECT material_id INTO `materialId` FROM "
                    + prefix + "materials WHERE material = `materialKey` AND data IS NULL; "
                    + "    END IF; "
                    + "    IF `materialId` IS NULL THEN "
                    + "        INSERT INTO " + prefix + "materials (`material`, `data`) "
                    + "        VALUES (`materialKey`, `blockData`); "
                    + "        SET `materialId` = LAST_INSERT_ID(); "
                    + "    END IF; "
                    + "END";
                stmt.execute(materialProcedure);

                // Player procedure
                @Language("SQL") String dropPlayerProcedure = "DROP PROCEDURE IF EXISTS getOrCreatePlayer";
                stmt.execute(dropPlayerProcedure);

                // Create the get-or-create Player type procedure
                @Language("SQL") String playerProcedure = "CREATE PROCEDURE getOrCreatePlayer "
                    + "(IN `playerName` VARCHAR(16), IN `uuid` VARCHAR(55), OUT `playerId` INT) "
                    + "BEGIN "
                    + "    SELECT player_id INTO `playerId` FROM "
                    + prefix + "players WHERE player_uuid = UNHEX(`uuid`); "
                    + "    IF `playerId` IS NULL THEN "
                    + "        INSERT INTO " + prefix + "players (`player`, `player_uuid`) "
                    + "        VALUES (`playerName`, UNHEX(`uuid`)); "
                    + "        SET `playerId` = LAST_INSERT_ID(); "
                    + "    END IF; "
                    + "END";
                stmt.execute(playerProcedure);

                // World procedure
                @Language("SQL") String dropWorldProcedure = "DROP PROCEDURE IF EXISTS getOrCreateWorld";
                stmt.execute(dropWorldProcedure);

                // Create the get-or-create World procedure
                @Language("SQL") String worldProcedure = "CREATE PROCEDURE getOrCreateWorld "
                    + "(IN `worldName` VARCHAR(255), IN `uuid` VARCHAR(55), OUT `worldId` TINYINT) "
                    + "BEGIN "
                    + "    SELECT world_id INTO `worldId` FROM "
                    + prefix + "worlds WHERE world_uuid = UNHEX(`uuid`); "
                    + "    IF `worldId` IS NULL THEN "
                    + "        INSERT INTO " + prefix + "worlds (`world`, `world_uuid`) "
                    + "             VALUES (`worldName`, UNHEX(`uuid`)); "
                    + "        SET `worldId` = LAST_INSERT_ID(); "
                    + "    END IF; "
                    + "END";
                stmt.execute(worldProcedure);

                // Activity procedure
                @Language("SQL") String dropActivityProcedure = "DROP PROCEDURE IF EXISTS createActivity";
                stmt.execute(dropActivityProcedure);

                // Create the Activity procedure
                @Language("SQL") String activityProcedure = "CREATE PROCEDURE createActivity("
                    + "IN `timestamp` INT,"
                    + "IN `x` INT,"
                    + "IN `y` INT,"
                    + "IN `z` INT,"
                    + "IN `action` VARCHAR(25),"
                    + "IN `cause` VARCHAR(25),"
                    + "IN `player` VARCHAR(16),"
                    + "IN `playerUuid` VARCHAR(55),"
                    + "IN `entityType` VARCHAR(25),"
                    + "IN `material` VARCHAR(45),"
                    + "IN `blockData` VARCHAR(155),"
                    + "IN `oldMaterial` VARCHAR(45),"
                    + "IN `oldBlockData` VARCHAR(155),"
                    + "IN `world` VARCHAR(255),"
                    + "IN `worldUuid` VARCHAR(55),"
                    + "IN `customDataVersion` SMALLINT,"
                    + "IN `customData` TEXT,"
                    + "IN `descriptor` VARCHAR(255),"
                    + "IN `metadata` VARCHAR(255),"
                    + "OUT `activityId` INT) "
                    + "BEGIN "
                    + "    SET @entityId = NULL;"
                    + "    SET @materialId = NULL;"
                    + "    SET @oldMaterialId = NULL;"
                    + "    SET @playerId = NULL;"
                    + "    CALL getOrCreateAction(`action`, @actionId);"
                    + "    IF `playerUuid` IS NOT NULL THEN "
                    + "        CALL getOrCreatePlayer(`player`, `playerUuid`, @playerId); "
                    + "    END IF; "
                    + "    CALL getOrCreateCause(`cause`, @playerId, @causeId); "
                    + "    CALL getOrCreateWorld(`world`, `worldUuid`, @worldId); "
                    + "    IF `entityType` IS NOT NULL THEN "
                    + "        CALL getOrCreateEntityType(entityType, @entityId); "
                    + "    END IF; "
                    + "    IF `material` IS NOT NULL THEN "
                    + "        CALL getOrCreateMaterial(material, blockData, @materialId); "
                    + "    END IF; "
                    + "    IF `oldMaterial` IS NOT NULL THEN "
                    + "        CALL getOrCreateMaterial(oldMaterial, oldBlockData, @oldMaterialId); "
                    + "    END IF; "
                    + "    INSERT INTO `" + prefix + "activities` "
                    + "    (`timestamp`, `world_id`, `x`, `y`, `z`, `action_id`, `material_id`, "
                    + "    `old_material_id`, `entity_type_id`, `cause_id`, `descriptor`, `metadata`) "
                    + "    VALUES "
                    + "    (`timestamp`, @worldId, `x`, `y`, `z`, @actionId, @materialId, "
                    + "    @oldMaterialId, @entityId, @causeId, `descriptor`, `metadata`); "
                    + "    SET `activityId` = LAST_INSERT_ID(); "
                    + "    IF `customData` IS NOT NULL THEN "
                    + "        INSERT INTO `" + prefix + "activities_custom_data` (`activity_id`, `version`, `data`) "
                    + "        VALUES (`activityId`, `customDataVersion`, `customData`); "
                    + "    END IF; "
                    + "END";
                stmt.execute(activityProcedure);
            }
        }
    }

    @Override
    public IActivityBatch createActivityBatch() {
        if (configurationService.storageConfig().mariadb().useStoredProcedures()) {
            return new SqlActivityProcedureBatch(loggingService, dataSource, serializerVersion);
        }

        return super.createActivityBatch();
    }
}
