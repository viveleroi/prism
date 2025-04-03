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

package network.darkhelmet.prism.core.storage.adapters.sql;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

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
import network.darkhelmet.prism.api.util.Pair;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.core.injection.factories.ISqlActivityQueryBuilderFactory;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.core.storage.HikariConfigFactory;
import network.darkhelmet.prism.core.storage.dbo.Indexes;
import network.darkhelmet.prism.core.storage.dbo.PrismDatabase;
import network.darkhelmet.prism.core.storage.dbo.records.PrismActionsRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismEntityTypesRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismMaterialsRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismWorldsRecord;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismActions;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismActivities;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismActivitiesCustomData;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismCauses;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismEntityTypes;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismMaterials;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismMeta;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismPlayers;
import network.darkhelmet.prism.core.storage.dbo.tables.PrismWorlds;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.loader.storage.StorageType;

import org.jooq.DSLContext;
import org.jooq.Index;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.constraint;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.min;

public abstract class AbstractSqlStorageAdapter implements IStorageAdapter {
    /**
     * The prism database object model.
     */
    public static PrismDatabase PRISM_DATABASE;

    /**
     * The actions dbo.
     */
    public static PrismActions PRISM_ACTIONS;

    /**
     * The activities dbo.
     */
    public static PrismActivities PRISM_ACTIVITIES;

    /**
     * The custom data dbo.
     */
    public static PrismActivitiesCustomData PRISM_ACTIVITIES_CUSTOM_DATA;

    /**
     * The causes dbo.
     */
    public static PrismCauses PRISM_CAUSES;

    /**
     * The entity types dbo.
     */
    public static PrismEntityTypes PRISM_ENTITY_TYPES;

    /**
     * The materials dbo.
     */
    public static PrismMaterials PRISM_MATERIALS;

    /**
     * The meta dbo.
     */
    public static PrismMeta PRISM_META;

    /**
     * The players dbo.
     */
    public static PrismPlayers PRISM_PLAYERS;

    /**
     * The worlds dbo.
     */
    public static PrismWorlds PRISM_WORLDS;

    /**
     * The serializer version.
     */
    protected final short serializerVersion;

    /**
     * The logging service.
     */
    protected final LoggingService loggingService;

    /**
     * The configuration service.
     */
    protected final ConfigurationService configurationService;

    /**
     * The action type registry.
     */
    protected final IActionTypeRegistry actionRegistry;

    /**
     * The schema updater.
     */
    protected final SqlSchemaUpdater schemaUpdater;

    /**
     * The query factory.
     */
    protected final ISqlActivityQueryBuilderFactory queryBuilderFactory;

    /**
     * The query builder.
     */
    protected SqlActivityQueryBuilder queryBuilder;

    /**
     * The cache service.
     */
    protected final CacheService cacheService;

    /**
     * The hikari data source.
     */
    protected HikariDataSource dataSource;

    /**
     * The dsl context.
     */
    protected DSLContext create;

    /**
     * The aliases materials table.
     */
    protected final PrismMaterials OLD_MATERIALS;

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
     * @param cacheService The cache service
     * @param queryBuilderFactory The query builder
     * @param serializerVersion The serializer version
     */
    @Inject
    public AbstractSqlStorageAdapter(
            LoggingService loggingService,
            ConfigurationService configurationService,
            IActionTypeRegistry actionRegistry,
            SqlSchemaUpdater schemaUpdater,
            ISqlActivityQueryBuilderFactory queryBuilderFactory,
            CacheService cacheService,
            @Named("serializerVersion") short serializerVersion) {
        this.loggingService = loggingService;
        this.configurationService = configurationService;
        this.actionRegistry = actionRegistry;
        this.schemaUpdater = schemaUpdater;
        this.cacheService = cacheService;
        this.queryBuilderFactory = queryBuilderFactory;
        this.serializerVersion = serializerVersion;

        String prefix = configurationService.storageConfig().primaryDataSource().prefix();

        // Initialize all of our DBOs
        PRISM_ACTIONS = new PrismActions(prefix);
        PRISM_ACTIVITIES = new PrismActivities(prefix);
        PRISM_ACTIVITIES_CUSTOM_DATA = new PrismActivitiesCustomData(prefix);
        PRISM_CAUSES = new PrismCauses(prefix);
        PRISM_ENTITY_TYPES = new PrismEntityTypes(prefix);
        PRISM_MATERIALS = new PrismMaterials(prefix);
        PRISM_META = new PrismMeta(prefix);
        PRISM_PLAYERS = new PrismPlayers(prefix);
        PRISM_WORLDS = new PrismWorlds(prefix);
        PRISM_DATABASE = new PrismDatabase(
                configurationService.storageConfig().primaryDataSource().database(), Arrays.asList(
            PRISM_ACTIONS,
            PRISM_ACTIVITIES,
            PRISM_ACTIVITIES_CUSTOM_DATA,
            PRISM_CAUSES,
            PRISM_ENTITY_TYPES,
            PRISM_MATERIALS,
            PRISM_META,
            PRISM_PLAYERS,
            PRISM_WORLDS
        ));

        // Table aliases
        OLD_MATERIALS = PRISM_MATERIALS.as("old_materials");

        // Turn of jooq crap. Lame
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        // Load any drivers
        HikariConfigFactory.loadDriver(configurationService.storageConfig().primaryStorageType());
        listDrivers();
    }

    /**
     * Connect to the data source.
     *
     * @param hikariConfig The hikari config
     */
    protected boolean connect(HikariConfig hikariConfig, SQLDialect sqlDialect) {
        String url = hikariConfig.getDataSourceProperties().getProperty("url");
        if (url == null) {
            url = hikariConfig.getJdbcUrl();
        }

        loggingService.logger().info(String.format("Connecting to %s", url));

        try {
            dataSource = new HikariDataSource(hikariConfig);

            create = DSL.using(dataSource.getConnection(), sqlDialect);
            if (queryBuilderFactory != null) {
                this.queryBuilder = queryBuilderFactory.create(create);
            }

            return true;
        }  catch (Exception e) {
            String msg = "Failed to connect to your database server. Please check:\n"
                + "- the ip/address\n"
                + "- the port\n"
                + "- any firewall rules\n"
                + "- the db server is running\n";
            loggingService.logger().warn(msg);
        }

        return false;
    }

    /**
     * List all drivers available in the driver manager.
     */
    protected void listDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        if (!drivers.hasMoreElements()) {
            loggingService.logger().info("No database drivers detected!");
        }
        while (drivers.hasMoreElements()) {
            loggingService.logger().info(String.format("Database driver: %s", drivers.nextElement().getClass()));
        }
    }

    /**
     * Detect version and other information regarding the database.
     *
     * @throws SQLException The database exception
     */
    protected void describeDatabase(boolean usingHikariProperties) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            String databaseProduct = databaseMetaData.getDatabaseProductName();
            String databaseVersion = databaseMetaData.getDatabaseProductVersion();

            String versionMsg = String.format("Database: %s %s", databaseProduct, databaseVersion);
            loggingService.logger().info(versionMsg);
        }
    }

    /**
     * Create tables.
     *
     * @throws SQLException The database exception
     */
    protected void prepareSchema() throws Exception {
        // Create the metadata table
        create.createTableIfNotExists(PRISM_META)
            .column(PRISM_META.META_ID)
            .column(PRISM_META.K)
            .column(PRISM_META.V)
            .primaryKey(PRISM_META.META_ID)
            .unique(PRISM_META.K)
            .execute();

        // Query for an existing schema version
        String schemaVersion = create
            .select(PRISM_META.V)
            .from(PRISM_META)
            .where(PRISM_META.K.eq("schema_ver"))
            .fetchOne(PRISM_META.V);

        if (schemaVersion != null) {
            loggingService.logger().info(String.format("Prism schema version: %s", schemaVersion));

            updateSchemas(schemaVersion);
        } else {
            // Insert the schema version
            create.insertInto(PRISM_META, PRISM_META.K, PRISM_META.V)
                .values("schema_ver", "v4")
                .execute();
        }

        // Create the players table
        create.createTableIfNotExists(PRISM_PLAYERS)
            .column(PRISM_PLAYERS.PLAYER_ID)
            .column(PRISM_PLAYERS.PLAYER)
            .column(PRISM_PLAYERS.PLAYER_UUID)
            .primaryKey(PRISM_PLAYERS.PLAYER_ID)
            .unique(PRISM_PLAYERS.PLAYER_UUID)
            .execute();

        // Create the causes table
        create.createTableIfNotExists(PRISM_CAUSES)
            .column(PRISM_CAUSES.CAUSE_ID)
            .column(PRISM_CAUSES.CAUSE)
            .column(PRISM_CAUSES.PLAYER_ID)
            .primaryKey(PRISM_CAUSES.CAUSE_ID)
            .unique(PRISM_CAUSES.CAUSE)
            .constraints(
                constraint("playerId").foreignKey(PRISM_CAUSES.PLAYER_ID)
                    .references(PRISM_PLAYERS, PRISM_PLAYERS.PLAYER_ID)
            )
            .execute();

        // Create the entity types table
        create.createTableIfNotExists(PRISM_ENTITY_TYPES)
            .column(PRISM_ENTITY_TYPES.ENTITY_TYPE_ID)
            .column(PRISM_ENTITY_TYPES.ENTITY_TYPE)
            .primaryKey(PRISM_ENTITY_TYPES.ENTITY_TYPE_ID)
            .unique(PRISM_ENTITY_TYPES.ENTITY_TYPE)
            .execute();

        // Create actions table
        create.createTableIfNotExists(PRISM_ACTIONS)
            .column(PRISM_ACTIONS.ACTION_ID)
            .column(PRISM_ACTIONS.ACTION)
            .primaryKey(PRISM_ACTIONS.ACTION_ID)
            .unique(PRISM_ACTIONS.ACTION)
            .execute();

        // Create the material data table
        create.createTableIfNotExists(PRISM_MATERIALS)
            .column(PRISM_MATERIALS.MATERIAL_ID)
            .column(PRISM_MATERIALS.MATERIAL)
            .column(PRISM_MATERIALS.DATA)
            .primaryKey(PRISM_MATERIALS.MATERIAL_ID)
            .unique(PRISM_MATERIALS.MATERIAL, PRISM_MATERIALS.DATA)
            .execute();

        // Create the worlds table
        create.createTableIfNotExists(PRISM_WORLDS)
            .column(PRISM_WORLDS.WORLD_ID)
            .column(PRISM_WORLDS.WORLD)
            .column(PRISM_WORLDS.WORLD_UUID)
            .primaryKey(PRISM_WORLDS.WORLD_ID)
            .unique(PRISM_WORLDS.WORLD_UUID)
            .execute();

        // Create the activities table. This one's the fatso.
        create.createTableIfNotExists(PRISM_ACTIVITIES)
            .column(PRISM_ACTIVITIES.ACTIVITY_ID)
            .column(PRISM_ACTIVITIES.TIMESTAMP)
            .column(PRISM_ACTIVITIES.WORLD_ID)
            .column(PRISM_ACTIVITIES.X)
            .column(PRISM_ACTIVITIES.Y)
            .column(PRISM_ACTIVITIES.Z)
            .column(PRISM_ACTIVITIES.ACTION_ID)
            .column(PRISM_ACTIVITIES.MATERIAL_ID)
            .column(PRISM_ACTIVITIES.OLD_MATERIAL_ID)
            .column(PRISM_ACTIVITIES.ENTITY_TYPE_ID)
            .column(PRISM_ACTIVITIES.CAUSE_ID)
            .column(PRISM_ACTIVITIES.DESCRIPTOR)
            .column(PRISM_ACTIVITIES.METADATA)
            .column(PRISM_ACTIVITIES.REVERSED)
            .primaryKey(PRISM_ACTIVITIES.ACTIVITY_ID)
            .constraints(
                constraint("actionId").foreignKey(PRISM_ACTIVITIES.ACTION_ID)
                    .references(PRISM_ACTIONS, PRISM_ACTIONS.ACTION_ID),
                constraint("causeId").foreignKey(PRISM_ACTIVITIES.CAUSE_ID)
                    .references(PRISM_CAUSES, PRISM_CAUSES.CAUSE_ID),
                constraint("entityTypeId").foreignKey(PRISM_ACTIVITIES.ENTITY_TYPE_ID)
                    .references(PRISM_ENTITY_TYPES, PRISM_ENTITY_TYPES.ENTITY_TYPE_ID),
                constraint("materialId").foreignKey(PRISM_ACTIVITIES.MATERIAL_ID)
                    .references(PRISM_MATERIALS, PRISM_MATERIALS.MATERIAL_ID),
                constraint("oldMaterialId").foreignKey(PRISM_ACTIVITIES.OLD_MATERIAL_ID)
                    .references(PRISM_MATERIALS, PRISM_MATERIALS.MATERIAL_ID),
                constraint("worldId").foreignKey(PRISM_ACTIVITIES.WORLD_ID)
                    .references(PRISM_WORLDS, PRISM_WORLDS.WORLD_ID)
            )
            .execute();

        // Jooq doesn't support creating indexes inline with create table.
        List<Index> indexes = create.meta(PRISM_DATABASE).getIndexes();
        if (!indexes.contains(Indexes.PRISM_ACTIVITIES_COORDINATE)) {
            create.createIndex("coordinate")
                .on(PRISM_ACTIVITIES, PRISM_ACTIVITIES.X, PRISM_ACTIVITIES.Y, PRISM_ACTIVITIES.Z).execute();
        }

        // Create the custom data table
        create.createTableIfNotExists(PRISM_ACTIVITIES_CUSTOM_DATA)
            .column(PRISM_ACTIVITIES_CUSTOM_DATA.EXTRA_ID)
            .column(PRISM_ACTIVITIES_CUSTOM_DATA.ACTIVITY_ID)
            .column(PRISM_ACTIVITIES_CUSTOM_DATA.VERSION)
            .column(PRISM_ACTIVITIES_CUSTOM_DATA.DATA)
            .primaryKey(PRISM_ACTIVITIES_CUSTOM_DATA.EXTRA_ID)
            .constraints(
                constraint("activityId").foreignKey(PRISM_ACTIVITIES_CUSTOM_DATA.ACTIVITY_ID)
                    .references(PRISM_ACTIVITIES, PRISM_ACTIVITIES.ACTIVITY_ID).onDeleteCascade()
            )
            .execute();
    }

    /**
     * Caching often-used object->primary-key lookups greatly reduce the number of queries/network requests.
     *
     * <p>Note: Player UUIDs are cached as needed and removed on disconnect.</p>
     */
    protected void prepareCache() {
        // Actions
        List<PrismActionsRecord> actions = create
            .select(PRISM_ACTIONS.ACTION, PRISM_ACTIONS.ACTION_ID)
            .from(PRISM_ACTIONS)
            .fetchInto(PrismActionsRecord.class);

        for (PrismActionsRecord actionsRecord : actions) {
            byte actionId = actionsRecord.getActionId().byteValue();
            cacheService.actionKeyPkMap().put(actionsRecord.getAction(), actionId);
        }

        // Entity Types
        List<PrismEntityTypesRecord> entityTypes = create
            .select(PRISM_ENTITY_TYPES.ENTITY_TYPE, PRISM_ENTITY_TYPES.ENTITY_TYPE_ID)
            .from(PRISM_ENTITY_TYPES)
            .fetchInto(PrismEntityTypesRecord.class);

        for (PrismEntityTypesRecord entityTypesRecord : entityTypes) {
            int entityTypeId = entityTypesRecord.getEntityTypeId().intValue();
            cacheService.entityTypePkMap().put(entityTypesRecord.getEntityType(), entityTypeId);
        }

        // Materials (base, no data)
        List<PrismMaterialsRecord> materials = create
            .select(PRISM_MATERIALS.MATERIAL, PRISM_MATERIALS.MATERIAL_ID)
            .from(PRISM_MATERIALS)
            .where(PRISM_MATERIALS.DATA.isNull())
            .fetchInto(PrismMaterialsRecord.class);

        for (PrismMaterialsRecord material : materials) {
            int materialId = material.getMaterialId().intValue();
            cacheService.materialDataPkMap().put(material.getMaterial(), materialId);
        }

        // World
        List<PrismWorldsRecord> worlds = create
            .select(PRISM_WORLDS.WORLD_UUID, PRISM_WORLDS.WORLD_ID)
            .from(PRISM_WORLDS)
            .fetchInto(PrismWorldsRecord.class);

        for (PrismWorldsRecord worldsRecord : worlds) {
            byte worldId = worldsRecord.getWorldId().byteValue();
            UUID worldUuid = UUID.fromString(worldsRecord.getWorldUuid());
            cacheService.worldUuidPkMap().put(worldUuid, worldId);
        }
    }

    /**
     * Update the schema as needed.
     *
     * @throws SQLException The database exception
     */
    protected void updateSchemas(String schemaVersion) throws Exception {}

    @Override
    public List<IActivity> queryActivities(ActivityQuery query) throws Exception {
        Result<org.jooq.Record> results = queryBuilder.queryActivities(query);
        return activityMapper(results, query);
    }

    @Override
    public PaginatedResults<IActivity> queryActivitiesPaginated(ActivityQuery query) throws Exception {
        Result<org.jooq.Record> result = queryBuilder.queryActivities(query);

        int totalResults = result.size();
        if (!result.isEmpty()) {
            if (configurationService.storageConfig().primaryStorageType().equals(StorageType.MYSQL)
                    && configurationService.storageConfig().mysql().useDeprecated()) {
                totalResults = create.fetchOne("SELECT FOUND_ROWS();").into(Integer.class);
            } else {
                totalResults = result.get(0).getValue("totalrows", Integer.class);
            }
        }

        int currentPage = (query.offset() / query.limit()) + 1;

        return new PaginatedResults<>(activityMapper(result, query), query.limit(), totalResults, currentPage);
    }

    /**
     * Maps activity data to an action and IActivity.
     *
     * @param result The result
     * @param query The original query
     * @return The activity list
     */
    protected List<IActivity> activityMapper(Result<org.jooq.Record> result, ActivityQuery query) {
        List<IActivity> activities = new ArrayList<>();

        for (org.jooq.Record r : result) {
            String actionKey = r.getValue(PRISM_ACTIONS.ACTION);
            Optional<IActionType> optionalActionType = actionRegistry.actionType(actionKey);
            if (optionalActionType.isEmpty()) {
                String msg = "Failed to find action type. Type: %s";
                loggingService.logger().warn(String.format(msg, actionKey));
                continue;
            }

            IActionType actionType = optionalActionType.get();

            // World
            UUID worldUuid = UUID.fromString(r.getValue(PRISM_WORLDS.WORLD_UUID));

            // Location
            int x = 0;
            int y = 0;
            int z = 0;
            if (query.grouped()) {
                x = r.getValue(avg(PRISM_ACTIVITIES.X)).intValue();
                y = r.getValue(avg(PRISM_ACTIVITIES.Y)).intValue();
                z = r.getValue(avg(PRISM_ACTIVITIES.Z)).intValue();
            } else {
                x = r.getValue(PRISM_ACTIVITIES.X);
                y = r.getValue(PRISM_ACTIVITIES.Y);
                z = r.getValue(PRISM_ACTIVITIES.Z);
            }

            WorldCoordinate coordinate = new WorldCoordinate(new NamedIdentity(worldUuid, null), x, y, z);

            // Entity type
            String entityType = null;
            String entityTypeName = r.getValue(PRISM_ENTITY_TYPES.ENTITY_TYPE);
            if (entityTypeName != null) {
                entityType = entityTypeName.toUpperCase(Locale.ENGLISH);
            }

            // Material/serialization data
            String material = null;
            String materialName = r.getValue(PRISM_MATERIALS.MATERIAL);
            if (materialName != null) {
                material = materialName.toUpperCase(Locale.ENGLISH);
            }

            // Cause
            String cause = r.getValue(PRISM_CAUSES.CAUSE);
            NamedIdentity player = null;

            // Player
            if (r.getValue(PRISM_PLAYERS.PLAYER_UUID) != null) {
                String playerName = r.getValue(PRISM_PLAYERS.PLAYER);
                UUID playerUuid = UUID.fromString(r.getValue(PRISM_PLAYERS.PLAYER_UUID));

                player = new NamedIdentity(playerUuid, playerName);
            }

            String descriptor = query.lookup() ? r.getValue(PRISM_ACTIVITIES.DESCRIPTOR) : null;
            String metadata = query.lookup() ? r.getValue(PRISM_ACTIVITIES.METADATA) : null;

            long timestamp;
            if (query.grouped()) {
                timestamp = r.getValue(avg(PRISM_ACTIVITIES.TIMESTAMP)).longValue();
            } else {
                timestamp = r.getValue(PRISM_ACTIVITIES.TIMESTAMP).longValue();
            }

            if (!query.grouped() && query.modification()) {
                long activityId = r.getValue(PRISM_ACTIVITIES.ACTIVITY_ID).longValue();

                String materialData = r.getValue(PRISM_MATERIALS.DATA);
                String customData = r.getValue(PRISM_ACTIVITIES_CUSTOM_DATA.DATA);
                Short customDataVersion = r.getValue(PRISM_ACTIVITIES_CUSTOM_DATA.VERSION.as("version"));

                // Material/serialization data
                String replacedMaterial = null;
                String replacedMaterialName = r.getValue(OLD_MATERIALS.MATERIAL);
                if (replacedMaterialName != null) {
                    replacedMaterial = replacedMaterialName.toUpperCase(Locale.ENGLISH);
                }

                String replacedMaterialData = r.getValue(OLD_MATERIALS.DATA);

                // Build the action data
                ActionData actionData = new ActionData(
                    material, materialData, replacedMaterial, replacedMaterialData,
                    entityType, customData, descriptor, metadata, customDataVersion);

                // Build the activity
                try {
                    IActivity activity = new Activity(activityId, actionType.createAction(actionData),
                        coordinate, cause, player, timestamp);

                    // Add to result list
                    activities.add(activity);
                } catch (Exception e) {
                    loggingService.handleException(e);
                }
            } else if (!query.grouped()) {
                long activityId = r.getValue(PRISM_ACTIVITIES.ACTIVITY_ID).longValue();

                // Build the action data
                ActionData actionData = new ActionData(
                    material, null, null, null,
                    entityType, null, descriptor, metadata, (short) 0);

                // Build the activity
                try {
                    IActivity activity = new Activity(activityId, actionType.createAction(actionData),
                        coordinate, cause, player, timestamp);

                    // Add to result list
                    activities.add(activity);
                } catch (Exception e) {
                    loggingService.handleException(e);
                }
            } else {
                // Build the action data
                ActionData actionData = new ActionData(
                    material, null, null, null,
                    entityType, null, descriptor, metadata, (short) 0);

                // Count
                int count = r.getValue("groupcount", Integer.class);

                // Build the grouped activity
                try {
                    IActivity activity = new GroupedActivity(
                        actionType.createAction(actionData), coordinate, cause, player, timestamp, count);

                    // Add to result list
                    activities.add(activity);
                } catch (Exception e) {
                    loggingService.handleException(e);
                }
            }
        }

        return activities;
    }

    @Override
    public IActivityBatch createActivityBatch() {
        return new SqlActivityBatch(loggingService, dataSource, create, serializerVersion, cacheService);
    }

    @Override
    public int deleteActivities(ActivityQuery query, int cycleMinPrimaryKey, int cycleMaxPrimaryKey) {
        return queryBuilder.deleteActivities(query, cycleMinPrimaryKey, cycleMaxPrimaryKey);
    }

    @Override
    public Pair<Integer, Integer> getActivitiesPkBounds() {
        Result<Record2<UInteger, UInteger>> result = create
            .select(min(PRISM_ACTIVITIES.ACTIVITY_ID), max(PRISM_ACTIVITIES.ACTIVITY_ID))
            .from(PRISM_ACTIVITIES).fetch();

        int minPk = result.get(0).get(min(PRISM_ACTIVITIES.ACTIVITY_ID)).intValue();
        int maxPk = result.get(0).get(max(PRISM_ACTIVITIES.ACTIVITY_ID)).intValue();

        return new Pair<>(minPk, maxPk);
    }

    @Override
    public void markReversed(List<Long> activityIds, boolean reversed) throws Exception {
        if (activityIds.isEmpty()) {
            return;
        }

        create.update(PRISM_ACTIVITIES)
            .set(PRISM_ACTIVITIES.REVERSED, reversed)
            .where(PRISM_ACTIVITIES.ACTIVITY_ID.in(activityIds))
            .execute();
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public boolean ready() {
        return ready;
    }
}
