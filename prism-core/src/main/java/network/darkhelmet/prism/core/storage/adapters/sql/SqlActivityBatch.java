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

package network.darkhelmet.prism.core.storage.adapters.sql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import network.darkhelmet.prism.api.actions.IBlockAction;
import network.darkhelmet.prism.api.actions.ICustomData;
import network.darkhelmet.prism.api.actions.IEntityAction;
import network.darkhelmet.prism.api.actions.IMaterialAction;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.storage.IActivityBatch;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.core.services.cache.CacheService;

import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.types.UByte;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIONS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES_CUSTOM_DATA;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_CAUSES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ENTITY_TYPES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_MATERIALS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_PLAYERS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_WORLDS;

public class SqlActivityBatch implements IActivityBatch {
    /**
     * The data source.
     */
    private final HikariDataSource dataSource;

    /**
     * The serializer version.
     */
    protected final short serializerVersion;

    /**
     * The cache service.
     */
    protected final CacheService cacheService;

    /**
     * The dsl context.
     */
    protected final DSLContext create;

    /**
     * The connection.
     */
    protected Connection connection;

    /**
     * The statement.
     */
    protected PreparedStatement statement;

    /**
     * Cache a map of the activities with extra data.
     *
     * <p>The integer is "index" of the activity in this batch. Used to map
     * generated keys to the activity when we need to write custom data.</p>
     */
    private final Map<Integer, IActivity> activitiesWithCustomData = new HashMap<>();

    /**
     * Count the "index" of the activities in this batch.
     */
    private int activityBatchIndex = 0;

    /**
     * Construct a new batch handler.
     *
     * @param create The DSL context
     * @param serializerVersion The serializer version
     * @param cacheService The cache service
     */
    public SqlActivityBatch(
            HikariDataSource dataSource,
            DSLContext create,
            short serializerVersion,
            CacheService cacheService) {
        this.dataSource = dataSource;
        this.create = create;
        this.serializerVersion = serializerVersion;
        this.cacheService = cacheService;
    }

    @Override
    public void startBatch() throws SQLException {
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        // Build the INSERT query
        String sql = create.insertInto(PRISM_ACTIVITIES,
            PRISM_ACTIVITIES.TIMESTAMP,
            PRISM_ACTIVITIES.X,
            PRISM_ACTIVITIES.Y,
            PRISM_ACTIVITIES.Z,
            PRISM_ACTIVITIES.ACTION_ID,
            PRISM_ACTIVITIES.ENTITY_TYPE_ID,
            PRISM_ACTIVITIES.MATERIAL_ID,
            PRISM_ACTIVITIES.OLD_MATERIAL_ID,
            PRISM_ACTIVITIES.WORLD_ID,
            PRISM_ACTIVITIES.CAUSE_ID,
            PRISM_ACTIVITIES.DESCRIPTOR
        ).values((UInteger) null, null, null, null, null, null, null, null, null, null, null).getSQL();

        statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    @Override
    public void add(ISingleActivity activity) throws SQLException {
        statement.setLong(1, activity.timestamp() / 1000);
        statement.setInt(2, activity.location().intX());
        statement.setInt(3, activity.location().intY());
        statement.setInt(4, activity.location().intZ());

        // Set the action relationship
        byte actionId = getOrCreateActionId(activity.action().type().key());
        statement.setByte(5, actionId);

        // Set the entity relationship
        if (activity.action() instanceof IEntityAction) {
            int entityTypeId = getOrCreateEntityTypeId(((IEntityAction) activity.action()).serializeEntityType());
            statement.setInt(6, entityTypeId);
        } else {
            statement.setNull(6, Types.INTEGER);
        }

        // Set the material relationship
        if (activity.action() instanceof IMaterialAction) {
            String material = ((IMaterialAction) activity.action()).serializeMaterial();
            String data = null;

            if (activity.action() instanceof IBlockAction) {
                data = ((IBlockAction) activity.action()).serializeBlockData();
            }

            statement.setInt(7, getOrCreateMaterialId(material, data));
        } else {
            statement.setNull(7, Types.INTEGER);
        }

        // Set the replaced material relationship
        if (activity.action() instanceof IBlockAction blockAction) {
            String replacedMaterial = blockAction.serializeReplacedMaterial();
            String replacedData = blockAction.serializeReplacedBlockData();

            int oldMaterialId = getOrCreateMaterialId(replacedMaterial, replacedData);
            statement.setInt(8, oldMaterialId);
        } else {
            statement.setNull(8, Types.INTEGER);
        }

        // Set the world relationship
        NamedIdentity world = activity.location().world();
        byte worldId = getOrCreateWorldId(world.uuid(), world.name());
        statement.setByte(9, worldId);

        // Set the player relationship
        Long playerId = null;
        if (activity.player() != null) {
            playerId = getOrCreatePlayerId(activity.player().uuid(), activity.player().name());
        }

        // Set the cause relationship
        long causeId = getOrCreateCauseId(activity.cause(), playerId);
        statement.setLong(10, causeId);

        // Set the descriptor
        statement.setString(11, activity.action().descriptor());

        if (activity.action() instanceof ICustomData) {
            if (((ICustomData) activity.action()).hasCustomData()) {
                activitiesWithCustomData.put(activityBatchIndex, activity);
            }
        }

        activityBatchIndex++;

        statement.addBatch();
    }

    /**
     * Get or create the action record and return the primary key.
     *
     * @param actionKey The action key
     * @return The primary key
     * @throws SQLException The database exception
     */
    private byte getOrCreateActionId(String actionKey) throws SQLException {
        if (cacheService.actionKeyPkMap().containsKey(actionKey)) {
            return cacheService.actionKeyPkMap().getByte(actionKey);
        }

        byte primaryKey;

        // Select any existing record
        UByte bytePk = create
            .select(PRISM_ACTIONS.ACTION_ID)
            .from(PRISM_ACTIONS)
            .where(PRISM_ACTIONS.ACTION.equal(actionKey))
            .fetchOne(PRISM_ACTIONS.ACTION_ID);

        if (bytePk != null) {
            primaryKey = bytePk.byteValue();
        } else {
            // Create the record
            bytePk = create
                .insertInto(PRISM_ACTIONS, PRISM_ACTIONS.ACTION)
                .values(actionKey)
                .returningResult(PRISM_ACTIONS.ACTION_ID)
                .fetchOne(PRISM_ACTIONS.ACTION_ID);

            if (bytePk != null) {
                primaryKey = bytePk.byteValue();
            } else {
                throw new SQLException(
                    String.format("Failed to get or create an action record. Action: %s", actionKey));
            }
        }

        cacheService.actionKeyPkMap().put(actionKey, primaryKey);

        return primaryKey;
    }

    /**
     * Get or create the cause record and return the primary key.
     *
     * @param cause The cause name
     * @param playerId The player id, if a player
     * @return The primary key
     * @throws SQLException The database exception
     */
    private long getOrCreateCauseId(String cause, @Nullable Long playerId) throws SQLException {
        if (playerId != null && cacheService.playerCausePkMap().containsKey(playerId.longValue())) {
            return cacheService.playerCausePkMap().get(playerId.longValue());
        }

        if (cause != null && cacheService.namedCausePkMap().containsKey(cause)) {
            return cacheService.namedCausePkMap().getLong(cause);
        }

        long primaryKey;

        UInteger intPk;
        if (playerId != null) {
            // Select the existing record on player
            intPk = create
                .select(PRISM_CAUSES.CAUSE_ID)
                .from(PRISM_CAUSES)
                .where(PRISM_CAUSES.PLAYER_ID.equal(UInteger.valueOf(playerId)))
                .fetchOne(PRISM_CAUSES.CAUSE_ID);
        } else {
            // Select the existing record on cause
            intPk = create
                .select(PRISM_CAUSES.CAUSE_ID)
                .from(PRISM_CAUSES)
                .where(PRISM_CAUSES.CAUSE.equal(cause))
                .fetchOne(PRISM_CAUSES.CAUSE_ID);
        }

        if (intPk != null) {
            primaryKey = intPk.longValue();
        } else {
            // Create the record
            intPk = create
                .insertInto(PRISM_CAUSES)
                .set(PRISM_CAUSES.CAUSE, cause)
                .set(PRISM_CAUSES.PLAYER_ID, playerId == null ? null : UInteger.valueOf(playerId))
                .returningResult(PRISM_CAUSES.CAUSE_ID)
                .fetchOne(PRISM_CAUSES.CAUSE_ID);

            if (intPk != null) {
                primaryKey = intPk.longValue();
            } else {
                throw new SQLException(
                    String.format("Failed to get or create a cause record. Cause: %s, %d", cause, playerId));
            }
        }

        if (cause != null) {
            cacheService.namedCausePkMap().put(cause, primaryKey);
        }

        if (playerId != null) {
            cacheService.playerCausePkMap().put(playerId.longValue(), primaryKey);
        }

        return primaryKey;
    }

    /**
     * Get or create the entity type record and return the primary key.
     *
     * @param entityType The entity type
     * @return The primary key
     * @throws SQLException The database exception
     */
    private int getOrCreateEntityTypeId(String entityType) throws SQLException {
        if (cacheService.entityTypePkMap().containsKey(entityType)) {
            return cacheService.entityTypePkMap().getInt(entityType);
        }

        int primaryKey;

        // Select the existing record
        UShort shortPk = create
            .select(PRISM_ENTITY_TYPES.ENTITY_TYPE_ID)
            .from(PRISM_ENTITY_TYPES)
            .where(PRISM_ENTITY_TYPES.ENTITY_TYPE.equal(entityType))
            .fetchOne(PRISM_ENTITY_TYPES.ENTITY_TYPE_ID);

        if (shortPk != null) {
            primaryKey = shortPk.intValue();
        } else {
            // Create the record
            shortPk = create
                .insertInto(PRISM_ENTITY_TYPES, PRISM_ENTITY_TYPES.ENTITY_TYPE)
                .values(entityType)
                .returningResult(PRISM_ENTITY_TYPES.ENTITY_TYPE_ID)
                .fetchOne(PRISM_ENTITY_TYPES.ENTITY_TYPE_ID);

            if (shortPk != null) {
                primaryKey = shortPk.intValue();
            } else {
                throw new SQLException(
                    String.format("Failed to get or create a entity type record. Material: %s", entityType));
            }
        }

        cacheService.entityTypePkMap().put(entityType, primaryKey);

        return primaryKey;
    }

    /**
     * Get or create the material data record and return the primary key.
     *
     * @param material The material
     * @param blockData The data, if any
     * @return The primary key
     * @throws SQLException The database exception
     */
    private int getOrCreateMaterialId(String material, String blockData) throws SQLException {
        if (blockData == null && cacheService.materialPkMap().containsKey(material)) {
            return cacheService.materialPkMap().getInt(material);
        }

        int primaryKey;

        // Select the existing material or material+data
        UShort shortPk = create
            .select(PRISM_MATERIALS.MATERIAL_ID)
            .from(PRISM_MATERIALS)
            .where(PRISM_MATERIALS.MATERIAL.equal(material), PRISM_MATERIALS.DATA.eq(blockData))
            .fetchOne(PRISM_MATERIALS.MATERIAL_ID);

        if (shortPk != null) {
            primaryKey = shortPk.intValue();
        } else {
            // Create the record
            shortPk = create
                .insertInto(PRISM_MATERIALS, PRISM_MATERIALS.MATERIAL, PRISM_MATERIALS.DATA)
                .values(material, blockData)
                .returningResult(PRISM_MATERIALS.MATERIAL_ID)
                .fetchOne(PRISM_MATERIALS.MATERIAL_ID);

            if (shortPk != null) {
                primaryKey = shortPk.intValue();
            } else {
                throw new SQLException(
                    String.format("Failed to get or create a material record. Material: %s %s",
                        material, blockData));
            }
        }

        if (blockData == null) {
            cacheService.materialPkMap().put(material, primaryKey);
        }

        return primaryKey;
    }

    /**
     * Get or create the player record and return the primary key.
     *
     * <p>Note: This will update the player name.</p>
     *
     * @param playerUuid The player uuid
     * @param playerName The player name
     * @return The primary key
     * @throws SQLException The database exception
     */
    private long getOrCreatePlayerId(UUID playerUuid, String playerName) throws SQLException {
        if (cacheService.playerUuidPkMap().containsKey(playerUuid)) {
            return cacheService.playerUuidPkMap().getLong(playerUuid);
        }

        long primaryKey;

        // Select the existing record
        UInteger intPk = create
            .select(PRISM_PLAYERS.PLAYER_ID)
            .from(PRISM_PLAYERS)
            .where(PRISM_PLAYERS.PLAYER_UUID.equal(playerUuid.toString()))
            .fetchOne(PRISM_PLAYERS.PLAYER_ID);

        if (intPk != null) {
            primaryKey = intPk.longValue();
        } else {
            // Attempt to create the record, or update the world name
            intPk = create
                .insertInto(PRISM_PLAYERS, PRISM_PLAYERS.PLAYER_UUID, PRISM_PLAYERS.PLAYER)
                .values(playerUuid.toString(), playerName)
                .returningResult(PRISM_PLAYERS.PLAYER_ID)
                .fetchOne(PRISM_PLAYERS.PLAYER_ID);

            if (intPk != null) {
                primaryKey = intPk.longValue();
            } else {
                throw new SQLException(
                    String.format("Failed to get or create a player record. Player: %s", playerUuid));
            }
        }

        cacheService.playerUuidPkMap().put(playerUuid, primaryKey);

        return primaryKey;
    }

    /**
     * Get or create the world record and return the primary key.
     *
     * <p>Note: This will update the world name.</p>
     *
     * @param worldUuid The world uuid
     * @param worldName The world name
     * @return The primary key
     * @throws SQLException The database exception
     */
    private byte getOrCreateWorldId(UUID worldUuid, String worldName) throws SQLException {
        if (cacheService.worldUuidPkMap().containsKey(worldUuid)) {
            return cacheService.worldUuidPkMap().getByte(worldUuid);
        }

        byte primaryKey;

        // Select any existing record
        // Note: We check *then* insert instead of using on duplicate key because ODK would
        // generate a new auto-increment primary key and update it every time, leading to ballooning PKs
        UByte bytePk = create
            .select(PRISM_WORLDS.WORLD_ID)
            .from(PRISM_WORLDS)
            .where(PRISM_WORLDS.WORLD_UUID.equal(worldUuid.toString()))
            .fetchOne(PRISM_WORLDS.WORLD_ID);

        if (bytePk != null) {
            primaryKey = bytePk.byteValue();
        } else {
            // Create the record
            bytePk = create
                .insertInto(PRISM_WORLDS, PRISM_WORLDS.WORLD_UUID, PRISM_WORLDS.WORLD)
                .values(worldUuid.toString(), worldName)
                .returningResult(PRISM_WORLDS.WORLD_ID)
                .fetchOne(PRISM_WORLDS.WORLD_ID);

            if (bytePk != null) {
                primaryKey = bytePk.byteValue();
            } else {
                throw new SQLException(
                    String.format("Failed to get or create a world record. World: %s", worldUuid));
            }
        }

        cacheService.worldUuidPkMap().put(worldUuid, primaryKey);

        return primaryKey;
    }

    @Override
    public void commitBatch() throws SQLException {
        statement.executeBatch();
        connection.commit();

        insertCustomData(statement.getGeneratedKeys());

        // Clear queue data, reset just in case the batch is restarted.
        activitiesWithCustomData.clear();
        activityBatchIndex = 0;

        // Restore auto-commit
        connection.setAutoCommit(true);

        // Close stuff
        statement.close();
        connection.close();
    }

    /**
     * Inserts additional activity data when necessary (tile entities, items, etc).
     *
     * @param keys The generate keys resultset from the parent activity batch insert
     * @throws SQLException Database exception
     */
    private void insertCustomData(ResultSet keys) throws SQLException {
        String insert = create.insertInto(PRISM_ACTIVITIES_CUSTOM_DATA,
            PRISM_ACTIVITIES_CUSTOM_DATA.ACTIVITY_ID,
            PRISM_ACTIVITIES_CUSTOM_DATA.VERSION,
            PRISM_ACTIVITIES_CUSTOM_DATA.DATA).values((UInteger) null, null, null).getSQL();

        PreparedStatement dataStatement = connection.prepareStatement(insert);

        int i = 0;
        while (keys.next()) {
            if (activitiesWithCustomData.containsKey(i)) {
                IActivity activity = activitiesWithCustomData.get(i);
                ICustomData customDataAction = (ICustomData) activity.action();

                int activityId = keys.getInt(1);
                String customData = customDataAction.serializeCustomData();

                dataStatement.setInt(1, activityId);
                dataStatement.setShort(2, serializerVersion);
                dataStatement.setString(3, customData);
                dataStatement.addBatch();
            }

            i++;
        }

        dataStatement.executeBatch();
        connection.commit();
    }
}
