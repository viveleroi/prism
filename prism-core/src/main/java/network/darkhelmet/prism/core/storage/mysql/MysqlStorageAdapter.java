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

package network.darkhelmet.prism.core.storage.mysql;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import network.darkhelmet.prism.api.PaginatedResults;
import network.darkhelmet.prism.api.actions.ActionData;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.actions.types.IActionTypeRegistry;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.activities.GroupedActivity;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.storage.IActivityBatch;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.core.services.configuration.ConfigurationService;
import network.darkhelmet.prism.core.services.logging.LoggingService;
import network.darkhelmet.prism.core.utils.TypeUtils;
import network.darkhelmet.prism.idb.DB;
import network.darkhelmet.prism.idb.Database;
import network.darkhelmet.prism.idb.DatabaseOptions;
import network.darkhelmet.prism.idb.DbRow;
import network.darkhelmet.prism.idb.PooledDatabaseOptions;

import org.intellij.lang.annotations.Language;

public class MysqlStorageAdapter implements IStorageAdapter {
    /**
     * The serializer version.
     */
    private final short serializerVersion;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The action type registry.
     */
    protected final IActionTypeRegistry actionRegistry;

    /**
     * The schema updater.
     */
    private final MysqlSchemaUpdater schemaUpdater;

    /**
     * The query builder.
     */
    private final MysqlQueryBuilder queryBuilder;

    /**
     * Toggle whether this storage system is enabled and ready.
     */
    protected boolean ready = false;

    /**
     * Constructor.
     *
     * @param loggingService The logging service
     * @param configurationService The configuration service
     * @param actionRegistry The action type registry
     * @param schemaUpdater The schema updater
     * @param queryBuilder The query builder
     * @param serializerVersion The serializer version
     */
    @Inject
    public MysqlStorageAdapter(
            LoggingService loggingService,
            ConfigurationService configurationService,
            IActionTypeRegistry actionRegistry,
            MysqlSchemaUpdater schemaUpdater,
            MysqlQueryBuilder queryBuilder,
            @Named("serializerVersion") short serializerVersion,
            Path dataPath) {
        this.loggingService = loggingService;
        this.configurationService = configurationService;
        this.actionRegistry = actionRegistry;
        this.schemaUpdater = schemaUpdater;
        this.queryBuilder = queryBuilder;
        this.serializerVersion = serializerVersion;

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        if (!drivers.hasMoreElements()) {
            loggingService.logger().info("No database drivers detected!");
        }
        while (drivers.hasMoreElements()) {
            loggingService.logger().info(String.format("Database driver: %s", drivers.nextElement().getClass()));
        }

        try {
            // First, try to use any hikari.properties
            File hikariPropertiesFile = new File(dataPath.toFile(), "hikari.properties");
            if (hikariPropertiesFile.exists()) {
                loggingService.logger().info("Using hikari.properties over storage.conf");

                HikariConfig config = new HikariConfig(hikariPropertiesFile.getPath());
                Database db = PooledDatabaseOptions.builder().hikariConfig(config).createHikariDatabase();
                DB.setGlobalDatabase(db);

                describeDatabase(true);
                prepareSchema();

                ready = true;
            } else {
                loggingService.logger().info("Reading storage.conf. There is no hikari.properties file.");

                DatabaseOptions.DatabaseOptionsBuilder builder = DatabaseOptions.builder().mysql(
                    configurationService.storageConfig().username(),
                    configurationService.storageConfig().password(),
                    configurationService.storageConfig().database(),
                    configurationService.storageConfig().host() + ":"
                        + configurationService.storageConfig().port());
                builder.onDatabaseConnectionFailure(loggingService::handleException);
                builder.onFatalError(loggingService::handleException);
                builder.driverClassName(configurationService.storageConfig().driver());
                builder.poolName("prism");
                builder.logger(loggingService.logger());
                builder.useOptimizations(configurationService.storageConfig().useHikariMysqlOptimizations());
                Database db = PooledDatabaseOptions.builder().options(builder.build()).createHikariDatabase();
                DB.setGlobalDatabase(db);

                describeDatabase(false);
                prepareSchema();

                ready = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Detect version and other information regarding the database.
     *
     * @throws SQLException The database exception
     */
    protected void describeDatabase(boolean usingHikariProperties) throws SQLException {
        Map<String, String> dbInfo = new HashMap<>();

        for (DbRow row : DB.getResults("SHOW VARIABLES")) {
            dbInfo.put(row.getString("Variable_name").toLowerCase(), row.getString("Value"));
        }

        String version = dbInfo.get("version");
        String versionComment = dbInfo.get("version_comment");
        String versionMsg = String.format("Database version: %s / %s", version, versionComment);
        loggingService.logger().info(versionMsg);

        long innodbSizeMb = Long.parseLong(dbInfo.get("innodb_buffer_pool_size")) / 1024 / 1024;
        loggingService.logger().info(String.format("innodb_buffer_pool_size: %d", innodbSizeMb));
        loggingService.logger().info(String.format("sql_mode: %s", dbInfo.get("sql_mode")));

        String grant = DB.getFirstColumn("SHOW GRANTS FOR CURRENT_USER();");
        boolean canCreateRoutines = grant.contains("CREATE ROUTINE");
        loggingService.logger().info(String.format("can create routines: %b", canCreateRoutines));

        if (configurationService.storageConfig().useStoredProcedures() && !canCreateRoutines) {
            configurationService.storageConfig().disallowStoredProcedures();
        }

        if (!usingHikariProperties) {
            boolean usrHikariMysqlOptimizations = configurationService.storageConfig().useHikariMysqlOptimizations();
            loggingService.logger().info(
                String.format("use hikari mysql optimizations: %b", usrHikariMysqlOptimizations));
        }
    }

    /**
     * Create tables.
     *
     * @throws SQLException The database exception
     */
    protected void prepareSchema() throws SQLException {
        String prefix = configurationService.storageConfig().prefix();

        // Create all new tables. This is done here because:
        // 1. We need them for new installs anyway
        // 2. Updater logic needs them for 8->v4
        // Create the players table
        @Language("SQL") String playersQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "players` ("
            + "`player_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "`player` VARCHAR(16) NOT NULL,"
            + "`player_uuid` BINARY(16) NOT NULL,"
            + "PRIMARY KEY (`player_id`),"
            + "UNIQUE KEY `player_uuid` (`player_uuid`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(playersQuery);

        @Language("SQL") String causesQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "causes` ("
            + "`cause_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "`cause` VARCHAR(25) DEFAULT NULL,"
            + "`player_id` INT UNSIGNED DEFAULT NULL,"
            + "PRIMARY KEY (`cause_id`),"
            + "UNIQUE KEY `cause` (`cause`),"
            + "KEY `playerId_idx` (`player_id`),"
            + "CONSTRAINT `playerId` FOREIGN KEY (`player_id`) REFERENCES `"
                + prefix + "players` (`player_id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(causesQuery);

        // Create the entity types table
        @Language("SQL") String entityTypeQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "entity_types` ("
            + "`entity_type_id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "`entity_type` VARCHAR(45) DEFAULT NULL,"
            + "PRIMARY KEY (`entity_type_id`),"
            + "UNIQUE KEY `entityType` (`entity_type`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(entityTypeQuery);

        // Look for existing tables first.
        List<String> tables = DB.getFirstColumnResults("SHOW TABLES LIKE ?",
            prefix + "%");
        if (tables.contains(prefix + "meta")) {
            // Check existing schema version before we do anything.
            // We can't create tables if existing ones are
            // going to be renamed during an update phase.
            // We'd run into collisions
            @Language("SQL") String sql = "SELECT v FROM " + prefix + "meta WHERE k = 'schema_ver'";

            String schemaVersion = DB.getFirstColumn(sql);
            loggingService.logger().info(String.format("Prism database version: %s", schemaVersion));

            updateSchemas(schemaVersion);
        }

        // Create actions table
        @Language("SQL") String actionsQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "actions` ("
            + "`action_id` TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "`action` VARCHAR(25) NOT NULL,"
            + "PRIMARY KEY (`action_id`), UNIQUE KEY `action` (`action`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(actionsQuery);

        // Create the material data table
        @Language("SQL") String matDataQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "materials` ("
            + "`material_id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "`material` VARCHAR(45) DEFAULT NULL,"
            + "`data` VARCHAR(155) DEFAULT NULL,"
            + "PRIMARY KEY (`material_id`),"
            + "UNIQUE KEY `materialData` (`material`,`data`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(matDataQuery);

        // Create the meta data table
        @Language("SQL") String metaQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "meta` ("
            + "`meta_id` TINYINT unsigned NOT NULL AUTO_INCREMENT,"
            + "`k` VARCHAR(25) NOT NULL,"
            + "`v` VARCHAR(155) NOT NULL,"
            + "PRIMARY KEY (`meta_id`),"
            + "UNIQUE KEY `k` (`k`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(metaQuery);

        // Create worlds table
        @Language("SQL") String worldsQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "worlds` ("
            + "`world_id` TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "`world` VARCHAR(255) NOT NULL,"
            + "`world_uuid` BINARY(16) NOT NULL,"
            + "PRIMARY KEY (`world_id`),"
            + "UNIQUE KEY `world_uuid` (`world_uuid`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(worldsQuery);

        // Create the activities table. This one's the fatso.
        @Language("SQL") String activitiesQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "activities` ("
            + "`activity_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "`timestamp` INT UNSIGNED NOT NULL,"
            + "`world_id` TINYINT UNSIGNED NOT NULL,"
            + "`x` INT NOT NULL,"
            + "`y` INT NOT NULL,"
            + "`z` INT NOT NULL,"
            + "`action_id` TINYINT UNSIGNED NOT NULL,"
            + "`material_id` SMALLINT UNSIGNED DEFAULT NULL,"
            + "`old_material_id` SMALLINT UNSIGNED DEFAULT NULL,"
            + "`entity_type_id` SMALLINT UNSIGNED DEFAULT NULL,"
            + "`cause_id` INT UNSIGNED NOT NULL,"
            + "`descriptor` VARCHAR(255) NULL,"
            + "PRIMARY KEY (`activity_id`),"
            + "KEY `actionId_idx` (`action_id`),"
            + "KEY `causeId_idx` (`cause_id`),"
            + "KEY `entityTypeId_idx` (`entity_type_id`),"
            + "KEY `materialId_idx` (`material_id`),"
            + "KEY `oldMaterialId_idx` (`old_material_id`),"
            + "KEY `worldId_idx` (`world_id`),"
            + "CONSTRAINT `actionId` FOREIGN KEY (`action_id`) REFERENCES `"
                + prefix + "actions` (`action_id`),"
            + "CONSTRAINT `causeId` FOREIGN KEY (`cause_id`) REFERENCES `"
                + prefix + "causes` (`cause_id`),"
            + "CONSTRAINT `entityTypeId` FOREIGN KEY (`entity_type_id`) REFERENCES `"
                + prefix + "entity_types` (`entity_type_id`),"
            + "CONSTRAINT `materialId` FOREIGN KEY (`material_id`) REFERENCES `"
                + prefix + "materials` (`material_id`),"
            + "CONSTRAINT `oldMaterialId` FOREIGN KEY (`old_material_id`) REFERENCES `"
                + prefix + "materials` (`material_id`),"
            + "CONSTRAINT `worldId` FOREIGN KEY (`world_id`) REFERENCES `" + prefix + "worlds` (`world_id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(activitiesQuery);

        // Create the custom data table
        @Language("SQL") String extraQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "activities_custom_data` ("
            + "`extra_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "`activity_id` INT UNSIGNED NOT NULL,"
            + "`version` SMALLINT NULL,"
            + "`data` TEXT,"
            + "PRIMARY KEY (`extra_id`),"
            + "KEY `activityId_idx` (`activity_id`),"
            + "CONSTRAINT `activityId` FOREIGN KEY (`activity_id`) REFERENCES `"
                + prefix + "activities` (`activity_id`) ON DELETE CASCADE"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(extraQuery);

        // Insert the schema version
        @Language("SQL") String setSchemaVer = "INSERT INTO `" + prefix + "meta` "
            + " (`k`, `v`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `v` = `v`";
        DB.executeInsert(setSchemaVer, "schema_ver", "v4");

        if (configurationService.storageConfig().useStoredProcedures()) {
            try (Statement stmt = DB.getGlobalDatabase().getConnection().createStatement()) {
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
                    + "    `old_material_id`, `entity_type_id`, `cause_id`, `descriptor`) "
                    + "    VALUES "
                    + "    (`timestamp`, @worldId, `x`, `y`, `z`, @actionId, @materialId, "
                    + "    @oldMaterialId, @entityId, @causeId, `descriptor`); "
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

    /**
     * Update the schema as needed.
     *
     * @throws SQLException The database exception
     */
    protected void updateSchemas(String schemaVersion) throws SQLException {
        // Update: 8 -> v4
        if (schemaVersion.equalsIgnoreCase("8")) {
            DB.createTransaction(stm -> schemaUpdater.update_8_to_v4(configurationService.storageConfig()));
        }
    }

    @Override
    public PaginatedResults<IActivity> queryActivitiesAsInformation(ActivityQuery query) throws SQLException {
        List<DbRow> rows = queryBuilder.queryActivities(query, configurationService.storageConfig().prefix());

        int totalResults = 0;
        if (!rows.isEmpty()) {
            totalResults = rows.get(0).getInt("totalRows");
        }

        int currentPage = (query.offset() / query.limit()) + 1;

        return new PaginatedResults<>(activityMapper(rows, query), query.limit(), totalResults, currentPage);
    }

    @Override
    public List<IActivity> queryActivitiesAsModification(ActivityQuery query) throws SQLException {
        List<DbRow> results = queryBuilder.queryActivities(query, configurationService.storageConfig().prefix());
        return activityMapper(results, query);
    }

    /**
     * Maps activity data to an action and IActivity.
     *
     * @param results The results
     * @param query The original query
     * @return The activity list
     */
    protected List<IActivity> activityMapper(List<DbRow> results, ActivityQuery query) {
        List<IActivity> activities = new ArrayList<>();

        for (DbRow row : results) {
            String actionKey = row.getString("action");
            Optional<IActionType> optionalActionType = actionRegistry.getActionType(actionKey);
            if (!optionalActionType.isPresent()) {
                String msg = "Failed to find action type. Type: %s";
                loggingService.logger().warn(String.format(msg, actionKey));
                continue;
            }

            IActionType actionType = optionalActionType.get();

            // World
            UUID worldUuid = TypeUtils.uuidFromDbString(row.getString("worldUuid"));

            // Location (average location for grouped)
            int x = row.getInt("x");
            int y = row.getInt("y");
            int z = row.getInt("z");
            WorldCoordinate coordinate = new WorldCoordinate(new NamedIdentity(worldUuid, null), x, y, z);

            // Entity type
            String entityType = null;
            String entityTypeName = row.getString("entity_type");
            if (entityTypeName != null) {
                entityType = entityTypeName.toUpperCase(Locale.ENGLISH);
            }

            // Material/serialization data
            String material = null;
            String materialName = row.getString("material");
            if (materialName != null) {
                material = materialName.toUpperCase(Locale.ENGLISH);
            }

            // Cause
            String cause = row.getString("cause");

            // Player
            NamedIdentity player = null;
            if (row.getString("playerUuid") != null) {
                String playerName = row.getString("player");
                UUID playerUuid = TypeUtils.uuidFromDbString(row.getString("playerUuid"));

                player = new NamedIdentity(playerUuid, playerName);
            }

            // Descriptor
            String descriptor = row.getString("descriptor");

            if (!query.grouped()) {
                long timestamp = row.getLong("timestamp");

                String materialData = row.getString("material_data");
                String customData = row.getString("custom_data");
                Integer customDataVersion = row.getInt("data_version");
                short version = customDataVersion.shortValue();

                // Material/serialization data
                String replacedMaterial = null;
                String replacedMaterialName = row.getString("old_material");
                if (replacedMaterialName != null) {
                    replacedMaterial = replacedMaterialName.toUpperCase(Locale.ENGLISH);
                }

                String replacedMaterialData = row.getString("old_material_data");

                // Build the action data
                ActionData actionData = new ActionData(
                    material, materialData, replacedMaterial, replacedMaterialData,
                    entityType, customData, descriptor, version);

                // Build the activity
                IActivity activity = new Activity(
                    actionType.createAction(actionData), coordinate, cause, player, timestamp);

                // Add to result list
                activities.add(activity);
            } else {
                // Timestamp
                BigDecimal timestamp = row.get("timestamp");

                // Build the action data
                ActionData actionData = new ActionData(
                    material, null, null, null,
                    entityType, null, descriptor, (short) 0);

                // Count
                int count = row.getInt("groupCount");

                // Build the grouped activity
                IActivity activity = new GroupedActivity(
                    actionType.createAction(actionData), coordinate, cause, player, timestamp.longValue(), count);

                // Add to result list
                activities.add(activity);
            }
        }

        return activities;
    }

    @Override
    public IActivityBatch createActivityBatch() {
        if (configurationService.storageConfig().useStoredProcedures()) {
            return new MysqlActivityProcedureBatch(serializerVersion, configurationService.storageConfig());
        }

        return new MysqlActivityBatch(serializerVersion, configurationService.storageConfig());
    }

    @Override
    public void close() {
        DB.close();
    }

    @Override
    public boolean ready() {
        return ready;
    }
}
