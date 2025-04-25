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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import network.darkhelmet.prism.api.actions.BlockAction;
import network.darkhelmet.prism.api.actions.CustomData;
import network.darkhelmet.prism.api.actions.EntityAction;
import network.darkhelmet.prism.api.actions.MaterialAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.storage.ActivityBatch;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.core.storage.dbo.records.PrismActivitiesRecord;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.types.UByte;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIONS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_BLOCKS;
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
     * An array of records to batch insert.
     */
    private List<PrismActivitiesRecord> records = new ArrayList<>();

    /**
     * Construct a new batch handler.
     *
     * @param loggingService The logging service
     * @param create The DSL context
     * @param serializerVersion The serializer version
     * @param cacheService The cache service
     */
    public SqlActivityBatch(
            LoggingService loggingService,
            DSLContext create,
            short serializerVersion,
            CacheService cacheService) {
        this.loggingService = loggingService;
        this.create = create;
        this.serializerVersion = serializerVersion;
        this.cacheService = cacheService;
    }

    @Override
    public void startBatch() {
        records = new ArrayList<>();
    }

    @Override
    public void add(Activity activity) throws SQLException {
        var record = create.newRecord(PRISM_ACTIVITIES);

        record.setTimestamp(UInteger.valueOf(activity.timestamp() / 1000));
        record.setX(activity.coordinate().intX());
        record.setY(activity.coordinate().intY());
        record.setZ(activity.coordinate().intZ());

        // Set the action relationship
        record.setActionId(UByte.valueOf(getOrCreateActionId(activity.action().type().key())));

        // Set the entity relationship
        if (activity.action() instanceof EntityAction entityAction) {
            record.setEntityTypeId(UShort.valueOf(
                getOrCreateEntityTypeId(entityAction.serializeEntityType())));
        }

        // Set the material relationship
        if (activity.action() instanceof MaterialAction materialAction) {
            record.setMaterialId(UShort.valueOf(getOrCreateMaterialId(materialAction.serializeMaterial())));
        }

        // Set the block relationship
        if (activity.action() instanceof BlockAction blockAction) {
            record.setBlockId(UInteger.valueOf(getOrCreateBlockId(
                blockAction.blockNamespace(),
                blockAction.blockName(),
                blockAction.serializeBlockData())));

            if (blockAction.replacedBlockName() != null) {
                record.setReplacedBlockId(UInteger.valueOf(getOrCreateBlockId(
                    blockAction.replacedBlockNamespace(),
                    blockAction.replacedBlockName(),
                    blockAction.serializeReplacedBlockData())));
            }
        }

        // Set the world relationship
        record.setWorldId(UByte.valueOf(getOrCreateWorldId(activity.world().key(), activity.world().value())));

        // Set the player relationship
        Long playerId = null;
        if (activity.player() != null) {
            playerId = getOrCreatePlayerId(activity.player().key(), activity.player().value());
        }

        // Set the cause relationship
        record.setCauseId(UInteger.valueOf(getOrCreateCauseId(activity.cause(), playerId)));

        // Set the descriptor
        record.setDescriptor(activity.action().descriptor());

        // Serialize the metadata
        if (activity.action().metadata() != null) {
            try {
                record.setMetadata(activity.action().serializeMetadata());
            } catch (Exception e) {
                loggingService.handleException(e);
            }
        }

        if (activity.action() instanceof CustomData customDataAction) {
            if (customDataAction.hasCustomData()) {
                record.setSerializerVersion(UShort.valueOf(serializerVersion));
                record.setSerializedData(customDataAction.serializeCustomData());
            }
        }

        records.add(record);
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
     * Get or create the block data record and return the primary key.
     *
     * @param blockData The block data
     * @return The primary key
     * @throws SQLException The database exception
     */
    private int getOrCreateBlockId(String namespace, String name, String blockData) throws SQLException {
        String blockKey = namespace + ":" + name + (blockData == null ? "" : blockData);
        Integer blockPk = cacheService.blockDataPkMap().getIfPresent(blockKey);
        if (blockPk != null) {
            return blockPk;
        }

        int primaryKey;

        // Select the existing block
        UInteger intPk = create
            .select(PRISM_BLOCKS.BLOCK_ID)
            .from(PRISM_BLOCKS)
            .where(PRISM_BLOCKS.NS.equal(namespace),
                PRISM_BLOCKS.NAME.equal(name),
                PRISM_BLOCKS.DATA.equal(blockData))
            .fetchOne(PRISM_BLOCKS.BLOCK_ID);

        if (intPk != null) {
            primaryKey = intPk.intValue();
        } else {
            // Create the record
            intPk = create
                .insertInto(PRISM_BLOCKS, PRISM_BLOCKS.NS, PRISM_BLOCKS.NAME, PRISM_BLOCKS.DATA)
                .values(namespace, name, blockData)
                .returningResult(PRISM_BLOCKS.BLOCK_ID)
                .fetchOne(PRISM_BLOCKS.BLOCK_ID);

            if (intPk != null) {
                primaryKey = intPk.intValue();
            } else {
                throw new SQLException(
                    String.format("Failed to get or create a block record. Block: %s:%s %s",
                        namespace, name, blockData));
            }
        }

        cacheService.blockDataPkMap().put(blockKey, primaryKey);

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
     * Get or create the material record and return the primary key.
     *
     * @param material The material
     * @return The primary key
     * @throws SQLException The database exception
     */
    private int getOrCreateMaterialId(String material) throws SQLException {
        Integer materialPk = cacheService.materialDataPkMap().getIfPresent(material);
        if (materialPk != null) {
            return materialPk;
        }

        int primaryKey;

        // Select the existing material or material+data
        UShort shortPk = create
            .select(PRISM_MATERIALS.MATERIAL_ID)
            .from(PRISM_MATERIALS)
            .where(PRISM_MATERIALS.MATERIAL.equal(material))
            .fetchOne(PRISM_MATERIALS.MATERIAL_ID);

        if (shortPk != null) {
            primaryKey = shortPk.intValue();
        } else {
            // Create the record
            shortPk = create
                .insertInto(PRISM_MATERIALS, PRISM_MATERIALS.MATERIAL)
                .values(material)
                .returningResult(PRISM_MATERIALS.MATERIAL_ID)
                .fetchOne(PRISM_MATERIALS.MATERIAL_ID);

            if (shortPk != null) {
                primaryKey = shortPk.intValue();
            } else {
                throw new SQLException(
                    String.format("Failed to get or create a material record. Material: %s", material));
            }
        }

        cacheService.materialDataPkMap().put(material, primaryKey);

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

        // Create the player or update the name
        create
            .insertInto(PRISM_PLAYERS, PRISM_PLAYERS.PLAYER_UUID, PRISM_PLAYERS.PLAYER)
            .values(playerUuid.toString(), playerName)
            .onConflict(PRISM_PLAYERS.PLAYER_UUID)
            .doUpdate()
            .set(PRISM_PLAYERS.PLAYER, playerName)
            .execute();

        // Get the primary key.
        // Every but postgres needs a second query to get the pk, so we just do this for everyone.
        var result = create.select(PRISM_PLAYERS.PLAYER_ID)
            .from(PRISM_PLAYERS)
            .where(PRISM_PLAYERS.PLAYER_UUID.eq(playerUuid.toString()))
            .fetchOne();

        if (result != null) {
            primaryKey = result.value1().longValue();
        } else {
            throw new SQLException(
                String.format("Failed to get or create a player record. Player: %s", playerUuid));
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
    public void commitBatch() {
        create.batchInsert(records).execute();
    }
}
