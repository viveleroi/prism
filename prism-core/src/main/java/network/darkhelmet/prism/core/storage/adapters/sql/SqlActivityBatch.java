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

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.UUID;

import network.darkhelmet.prism.api.actions.BlockAction;
import network.darkhelmet.prism.api.actions.CustomData;
import network.darkhelmet.prism.api.actions.EntityAction;
import network.darkhelmet.prism.api.actions.MaterialAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.storage.ActivityBatch;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.types.UByte;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIONS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_CAUSES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ENTITY_TYPES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_MATERIALS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_PLAYERS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_WORLDS;

public class SqlActivityBatch implements ActivityBatch {
    /**
     * The logging service.
     */
    private final LoggingService loggingService;

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
     * Construct a new batch handler.
     *
     * @param create The DSL context
     * @param serializerVersion The serializer version
     * @param cacheService The cache service
     */
    public SqlActivityBatch(
            LoggingService loggingService,
            HikariDataSource dataSource,
            DSLContext create,
            short serializerVersion,
            CacheService cacheService) {
        this.loggingService = loggingService;
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
            PRISM_ACTIVITIES.DESCRIPTOR,
            PRISM_ACTIVITIES.METADATA,
            PRISM_ACTIVITIES.SERIALIZER_VERSION,
            PRISM_ACTIVITIES.SERIALIZED_DATA
        ).values((UInteger) null, null, null, null, null, null, null, null, null, null, null, null, null, null)
            .getSQL();

        statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    @Override
    public void add(Activity activity) throws SQLException {
        statement.setLong(1, activity.timestamp() / 1000);
        statement.setInt(2, activity.coordinate().intX());
        statement.setInt(3, activity.coordinate().intY());
        statement.setInt(4, activity.coordinate().intZ());

        // Set the action relationship
        byte actionId = getOrCreateActionId(activity.action().type().key());
        statement.setByte(5, actionId);

        // Set the entity relationship
        if (activity.action() instanceof EntityAction) {
            int entityTypeId = getOrCreateEntityTypeId(((EntityAction) activity.action()).serializeEntityType());
            statement.setInt(6, entityTypeId);
        } else {
            statement.setNull(6, Types.INTEGER);
        }

        // Set the material relationship
        if (activity.action() instanceof MaterialAction) {
            String material = ((MaterialAction) activity.action()).serializeMaterial();
            String data = null;

            if (activity.action() instanceof BlockAction) {
                data = ((BlockAction) activity.action()).serializeBlockData();
            }

            statement.setInt(7, getOrCreateMaterialId(material, data));
        } else {
            statement.setNull(7, Types.INTEGER);
        }

        // Set the replaced material relationship
        if (activity.action() instanceof BlockAction blockAction) {
            String replacedMaterial = blockAction.serializeReplacedMaterial();
            String replacedData = blockAction.serializeReplacedBlockData();

            int oldMaterialId = getOrCreateMaterialId(replacedMaterial, replacedData);
            statement.setInt(8, oldMaterialId);
        } else {
            statement.setNull(8, Types.INTEGER);
        }

        // Set the world relationship
        byte worldId = getOrCreateWorldId(activity.world().key(), activity.world().value());
        statement.setByte(9, worldId);

        // Set the player relationship
        Long playerId = null;
        if (activity.player() != null) {
            playerId = getOrCreatePlayerId(activity.player().key(), activity.player().value());
        }

        // Set the cause relationship
        long causeId = getOrCreateCauseId(activity.cause(), playerId);
        statement.setLong(10, causeId);

        // Set the descriptor
        statement.setString(11, activity.action().descriptor());

        // Serialize the metadata
        if (activity.action().metadata() != null) {
            try {
                statement.setString(12, activity.action().serializeMetadata());
            } catch (Exception e) {
                loggingService.handleException(e);
                statement.setNull(12, Types.VARCHAR);
            }
        } else {
            statement.setNull(12, Types.VARCHAR);
        }

        if (activity.action() instanceof CustomData customDataAction) {
            if (customDataAction.hasCustomData()) {
                statement.setShort(13, serializerVersion);
                statement.setString(14, customDataAction.serializeCustomData());
            } else {
                statement.setNull(13, Types.SMALLINT);
                statement.setNull(14, Types.VARCHAR);
            }
        } else {
            statement.setNull(13, Types.SMALLINT);
            statement.setNull(14, Types.VARCHAR);
        }

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
        Byte actionKeyPk = cacheService.actionKeyPkMap().getIfPresent(actionKey);
        if (actionKeyPk != null) {
            return actionKeyPk;
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
        if (playerId != null) {
            Long playerCausePk = cacheService.playerCausePkMap().getIfPresent(playerId);
            if (playerCausePk != null) {
                return playerCausePk;
            }
        }  else if (cause != null) {
            Long causePk = cacheService.namedCausePkMap().getIfPresent(cause);
            if (causePk != null) {
                return causePk;
            }
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
            cacheService.playerCausePkMap().put(playerId, primaryKey);
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
        Integer entityPk = cacheService.entityTypePkMap().getIfPresent(entityType);
        if (entityPk != null) {
            return entityPk;
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
        String materialData = material + (blockData == null ? "" : blockData);
        Integer materialPk = cacheService.materialDataPkMap().getIfPresent(materialData);
        if (materialPk != null) {
            return materialPk;
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

        cacheService.materialDataPkMap().put(materialData, primaryKey);

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
        Long playerPk = cacheService.playerUuidPkMap().getIfPresent(playerUuid);
        if (playerPk != null) {
            return playerPk;
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
        Byte worldPk = cacheService.worldUuidPkMap().getIfPresent(worldUuid);
        if (worldPk != null) {
            return worldPk;
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

        // Restore auto-commit
        connection.setAutoCommit(true);

        // Close stuff
        statement.close();
        connection.close();
    }
}
