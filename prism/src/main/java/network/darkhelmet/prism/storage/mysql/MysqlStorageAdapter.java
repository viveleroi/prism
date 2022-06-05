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

package network.darkhelmet.prism.storage.mysql;

import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.DbRow;
import co.aikar.idb.PooledDatabaseOptions;

import com.google.inject.Inject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import network.darkhelmet.prism.api.PaginatedResults;
import network.darkhelmet.prism.api.actions.ActionData;
import network.darkhelmet.prism.api.actions.IActionRegistry;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.activities.GroupedActivity;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.storage.IActivityBatch;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.services.configuration.ConfigurationService;
import network.darkhelmet.prism.utils.TypeUtils;

import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.intellij.lang.annotations.Language;

public class MysqlStorageAdapter implements IStorageAdapter {
    /**
     * The logger.
     */
    private final Logger logger;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The action registry.
     */
    protected final IActionRegistry actionRegistry;

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
     * Construct a new instance.
     *
     * @param configurationService The configuration service
     */
    @Inject
    public MysqlStorageAdapter(
            Logger logger,
            ConfigurationService configurationService,
            IActionRegistry actionRegistry,
            MysqlSchemaUpdater schemaUpdater,
            MysqlQueryBuilder queryBuilder) {
        this.logger = logger;
        this.configurationService = configurationService;
        this.actionRegistry = actionRegistry;
        this.schemaUpdater = schemaUpdater;
        this.queryBuilder = queryBuilder;

        try {
            DatabaseOptions options = DatabaseOptions.builder().mysql(
                configurationService.storageConfig().username(),
                configurationService.storageConfig().password(),
                configurationService.storageConfig().database(),
                configurationService.storageConfig().host() + ":"
                        + configurationService.storageConfig().port()).build();
            Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
            DB.setGlobalDatabase(db);

            describeDatabase();
            prepareSchema();

            ready = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Detect version and other information regarding the database.
     *
     * @throws SQLException The database exception
     */
    protected void describeDatabase() throws SQLException {
        Map<String, String> dbInfo = new HashMap<>();

        for (DbRow row : DB.getResults("SHOW VARIABLES")) {
            dbInfo.put(row.getString("Variable_name").toLowerCase(), row.getString("Value"));
        }

        String version = dbInfo.get("version");
        String versionComment = dbInfo.get("version_comment");
        String versionMsg = String.format("Database version: %s / %s", version, versionComment);
        logger.info(versionMsg);

        long innodbSizeMb = Long.parseLong(dbInfo.get("innodb_buffer_pool_size")) / 1024 / 1024;
        logger.info(String.format("innodb_buffer_pool_size: %d", innodbSizeMb));
        logger.info(String.format("sql_mode: %s", dbInfo.get("sql_mode")));

        String grant = DB.getFirstColumn("SHOW GRANTS FOR CURRENT_USER();");
        boolean canCreateRoutines = grant.contains("CREATE ROUTINE");
        logger.info(String.format("can create routines: %b", canCreateRoutines));

        if (configurationService.storageConfig().useStoredProcedures() && !canCreateRoutines) {
            configurationService.storageConfig().disallowStoredProcedures();
        }
    }

    /**
     * Create tables.
     *
     * @throws SQLException The database exception
     */
    protected void prepareSchema() throws SQLException {
        String prefix = configurationService.storageConfig().prefix();

        // Create causes table. This is done here because:
        // 1. We need it for new installs anyway
        // 2. Updater logic needs it for 8->v4
        @Language("SQL") String createCauses = "CREATE TABLE IF NOT EXISTS `" + prefix + "causes` ("
            + "`cause_id` int unsigned NOT NULL AUTO_INCREMENT,"
            + "`cause` varchar(25) NOT NULL,"
            + "`player_id` int NULL,"
            + "PRIMARY KEY (`cause_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(createCauses);

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
            logger.info(String.format("Prism database version: %s", schemaVersion));

            updateSchemas(schemaVersion);
        }

        // Create actions table
        @Language("SQL") String actionsQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "actions` ("
            + "`action_id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,"
            + "`action` varchar(25) NOT NULL,"
            + "PRIMARY KEY (`action_id`), UNIQUE KEY `action` (`action`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(actionsQuery);

        // Create the activities table. This one's the fatso.
        @Language("SQL") String activitiesQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "activities` ("
            + "`activity_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
            + "`timestamp` int(10) unsigned NOT NULL,"
            + "`world_id` tinyint(3) unsigned NOT NULL,"
            + "`x` int(11) NOT NULL,"
            + "`y` int(11) NOT NULL,"
            + "`z` int(11) NOT NULL,"
            + "`action_id` tinyint(3) unsigned NOT NULL,"
            + "`material_id` smallint(6) unsigned DEFAULT NULL,"
            + "`old_material_id` smallint(6) unsigned DEFAULT NULL,"
            + "`entity_type_id` smallint(6) unsigned NULL,"
            + "`cause_id` int(11) NOT NULL,"
            + "PRIMARY KEY (`activity_id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(activitiesQuery);

        // Create the custom data table
        @Language("SQL") String extraQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "activities_custom_data` ("
            + "`extra_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
            + "`activity_id` int(10) unsigned NOT NULL,"
            + "`version` SMALLINT NULL,"
            + "`data` text,"
            + "PRIMARY KEY (`extra_id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(extraQuery);

        // Create the entity types table
        @Language("SQL") String entityTypeQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "entity_types` ("
            + "`entity_type_id` smallint(6) NOT NULL AUTO_INCREMENT,"
            + "`entity_type` varchar(45) DEFAULT NULL,"
            + "PRIMARY KEY (`entity_type_id`),"
            + "UNIQUE KEY `entity_type` (`entity_type`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(entityTypeQuery);

        // Create the material data table
        @Language("SQL") String matDataQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "material_data` ("
            + "`material_id` smallint(6) NOT NULL AUTO_INCREMENT,"
            + "`material` varchar(45) DEFAULT NULL,"
            + "`data` varchar(155) DEFAULT NULL,"
            + "PRIMARY KEY (`material_id`),"
            + "UNIQUE KEY `materialdata` (`material`,`data`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(matDataQuery);

        // Create the meta data table
        @Language("SQL") String metaQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "meta` ("
            + "`meta_id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,"
            + "`k` varchar(25) NOT NULL,"
            + "`v` varchar(155) NOT NULL,"
            + "PRIMARY KEY (`meta_id`),"
            + "UNIQUE KEY `k` (`k`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(metaQuery);

        // Create the players table
        @Language("SQL") String playersQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "players` ("
            + "`player_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
            + "`player` varchar(16) NOT NULL,"
            + "`player_uuid` binary(16) NOT NULL,"
            + "PRIMARY KEY (`player_id`),"
            + "UNIQUE KEY `player_uuid` (`player_uuid`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(playersQuery);

        // Create worlds table
        @Language("SQL") String worldsQuery = "CREATE TABLE IF NOT EXISTS `" + prefix + "worlds` ("
            + "`world_id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,"
            + "`world` varchar(255) NOT NULL,"
            + "`world_uuid` binary(16) NOT NULL,"
            + "PRIMARY KEY (`world_id`),"
            + "UNIQUE KEY `world_uuid` (`world_uuid`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        DB.executeUpdate(worldsQuery);

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
                    + "(IN `actionKey` VARCHAR(25), OUT `actionId` TINYINT(3)) "
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
                    + "(IN `causeStr` VARCHAR(25), IN `playerId` INT(11), OUT `causeId` INT(10)) "
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
                    + "(IN `entityType` VARCHAR(25), OUT `entityTypeId` SMALLINT(6)) "
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
                    + "(IN `materialKey` VARCHAR(45), IN `blockData` VARCHAR(155), OUT `materialId` SMALLINT(6)) "
                    + "BEGIN "
                    + "    IF blockData IS NOT NULL THEN "
                    + "        SELECT material_id INTO `materialId` FROM "
                    + prefix + "material_data WHERE material = `materialKey` AND data = `blockData`; "
                    + "    ELSE "
                    + "        SELECT material_id INTO `materialId` FROM "
                    + prefix + "material_data WHERE material = `materialKey` AND data IS NULL; "
                    + "    END IF; "
                    + "    IF `materialId` IS NULL THEN "
                    + "        INSERT INTO " + prefix + "material_data (`material`, `data`) "
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
                    + "(IN `playerName` VARCHAR(16), IN `uuid` VARCHAR(55), OUT `playerId` INT(10)) "
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
                    + "(IN `worldName` VARCHAR(255), IN `uuid` VARCHAR(55), OUT `worldId` TINYINT(3)) "
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
                    + "IN `timestamp` INT(10),"
                    + "IN `x` INT(11),"
                    + "IN `y` INT(11),"
                    + "IN `z` INT(11),"
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
                    + "IN `customDataVersion` SMALLINT(6),"
                    + "IN `customData` TEXT,"
                    + "OUT `activityId` INT(10)) "
                    + "BEGIN "
                    + "    CALL getOrCreateAction(`action`, @actionId);"
                    + "    IF `playerUuid` IS NOT NULL THEN "
                    + "        CALL getOrCreatePlayer(`player`, `playerUuid`, @playerId); "
                    + "    END IF; "
                    + "    CALL getOrCreateCause(`cause`, @playerId, @causeId); "
                    + "    CALL getOrCreateMaterial(`material`, `blockData`, @materialId); "
                    + "    CALL getOrCreateWorld(`world`, `worldUuid`, @worldId); "
                    + "    IF `entityType` IS NOT NULL THEN "
                    + "        CALL getOrCreateEntityType(entityType, @entityId); "
                    + "    END IF; "
                    + "    IF `oldMaterial` IS NOT NULL THEN "
                    + "        CALL getOrCreateMaterial(oldMaterial, oldBlockData, @oldMaterialId); "
                    + "    END IF; "
                    + "    INSERT INTO `" + prefix + "activities` "
                    + "    (`timestamp`, `world_id`, `x`, `y`, `z`, `action_id`, `material_id`, "
                    + "    `old_material_id`, `entity_type_id`, `cause_id`) "
                    + "    VALUES "
                    + "    (`timestamp`, @worldId, `x`, `y`, `z`, @actionId, @materialId, "
                    + "    @oldMaterialId, @entityId, @causeId); "
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
            if (optionalActionType.isEmpty()) {
                String msg = "Failed to find action type. Type: %s";
                logger.warn(String.format(msg, actionKey));
                continue;
            }

            IActionType actionType = optionalActionType.get();

            // World
            UUID worldUuid = TypeUtils.uuidFromDbString(row.getString("worldUuid"));
            World world = Bukkit.getServer().getWorld(worldUuid);
            if (world == null) {
                String msg = "Failed to find game world for activity query. World UUID: %s";
                logger.warn(String.format(msg, worldUuid));
                continue;
            }

            // Location (average location for grouped)
            int x = row.getInt("x");
            int y = row.getInt("y");
            int z = row.getInt("z");
            Location location = new Location(world, x, y, z);

            // Entity type
            EntityType entityType = null;
            String entityTypeName = row.getString("entity_type");
            if (entityTypeName != null) {
                entityType = EntityType.valueOf(entityTypeName.toUpperCase(Locale.ENGLISH));
            }

            // Material/serialization data
            Material material = null;
            String materialName = row.getString("material");
            if (materialName != null) {
                material = Material.valueOf(materialName.toUpperCase(Locale.ENGLISH));
            }

            // Cause/player
            Object cause = row.getString("cause");
            if (row.getString("playerUuid") != null) {
                UUID playerUuid = TypeUtils.uuidFromDbString(row.getString("playerUuid"));
                cause = Bukkit.getOfflinePlayer(playerUuid);
            }

            if (!query.grouped()) {
                long timestamp = row.get("timestamp");
                String materialData = row.getString("material_data");
                String customData = row.getString("custom_data");
                Integer customDataVersion = row.getInt("data_version");
                short version = customDataVersion.shortValue();

                // Material/serialization data
                Material replacedMaterial = null;
                String replacedMaterialName = row.getString("old_material");
                if (replacedMaterialName != null) {
                    replacedMaterial = Material.valueOf(replacedMaterialName.toUpperCase(Locale.ENGLISH));
                }

                String replacedMaterialData = row.getString("old_material_data");

                // Build the action data
                ActionData actionData = new ActionData(
                    material, materialData, replacedMaterial, replacedMaterialData, entityType, customData, version);

                // Build the activity
                IActivity activity = new Activity(actionType.createAction(actionData), location, cause, timestamp);

                // Add to result list
                activities.add(activity);
            } else {
                // Build the action data
                ActionData actionData = new ActionData(
                    material, null, null, null, entityType, null, (short) 0);

                // Count
                int count = row.getInt("groupCount");

                // Timestamp
                BigDecimal timestamp = row.get("timestamp");

                // Build the grouped activity
                IActivity activity = new GroupedActivity(
                    actionType.createAction(actionData), location, cause, timestamp.longValue(), count);

                // Add to result list
                activities.add(activity);
            }
        }

        return activities;
    }

    @Override
    public IActivityBatch createActivityBatch() {
        if (configurationService.storageConfig().useStoredProcedures()) {
            return new MysqlActivityProcedureBatch(configurationService.storageConfig());
        }

        return new MysqlActivityBatch(configurationService.storageConfig());
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
