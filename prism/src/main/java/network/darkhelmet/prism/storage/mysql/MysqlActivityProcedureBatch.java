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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;

import network.darkhelmet.prism.Prism;
import network.darkhelmet.prism.api.actions.IBlockAction;
import network.darkhelmet.prism.api.actions.ICustomData;
import network.darkhelmet.prism.api.actions.IEntityAction;
import network.darkhelmet.prism.api.actions.IMaterialAction;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.storage.IActivityBatch;
import network.darkhelmet.prism.services.configuration.StorageConfiguration;
import network.darkhelmet.prism.utils.TypeUtils;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MysqlActivityProcedureBatch implements IActivityBatch {
    /**
     * The storage configuration.
     */
    private final StorageConfiguration storageConfig;

    /**
     * The connection.
     */
    private Connection connection;

    /**
     * The statement.
     */
    private PreparedStatement statement;

    /**
     * Construct a new batch handler.
     *
     * @param storageConfiguration The storage configuration
     */
    public MysqlActivityProcedureBatch(StorageConfiguration storageConfiguration) {
        this.storageConfig = storageConfiguration;
    }

    @Override
    public void startBatch() throws SQLException {
        connection = DB.getGlobalDatabase().getConnection();

        statement = connection.prepareCall(
            "{ CALL createActivity(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }");
    }

    @Override
    public void add(IActivity activity) throws SQLException {
        statement.setLong(1, activity.timestamp() / 1000);
        statement.setInt(2, activity.location().getBlockX());
        statement.setInt(3, activity.location().getBlockY());
        statement.setInt(4, activity.location().getBlockZ());
        statement.setString(5, activity.action().type().key());

        // Cause/player
        if (activity.cause() instanceof Player player) {
            String uuidStr = TypeUtils.uuidToDbString(player.getUniqueId());

            statement.setString(6, "unknown");
            statement.setString(7, player.getName());
            statement.setString(8, uuidStr);
        } else {
            String cause = "unknown";
            if (activity.cause() instanceof Entity causeEntity) {
                cause = causeEntity.getType().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
            } else if (activity.cause() instanceof Block causeBlock) {
                cause = causeBlock.getType().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
            } else if (activity.cause() instanceof String causeStr) {
                cause = causeStr;
            }

            statement.setString(6, cause);
            statement.setNull(7, Types.VARCHAR);
            statement.setNull(8, Types.VARCHAR);
        }

        // Entity
        if (activity.action() instanceof IEntityAction entityAction) {
            statement.setString(9, entityAction.serializeEntityType());
        } else {
            statement.setNull(9, Types.VARCHAR);
        }

        // Material
        if (activity.action() instanceof IMaterialAction materialAction) {
            statement.setString(10, materialAction.serializeMaterial());
        } else {
            statement.setNull(10, Types.VARCHAR);
        }

        // Material data
        if (activity.action() instanceof IBlockAction blockAction) {
            statement.setString(11, blockAction.serializeBlockData());
        } else {
            statement.setNull(11, Types.VARCHAR);
        }

        // Replaced material & data
        if (activity.action() instanceof IBlockAction blockAction) {
            statement.setString(12, blockAction.serializeReplacedMaterial());
            statement.setString(13, blockAction.serializeReplacedBlockData());
        } else {
            statement.setNull(12, Types.VARCHAR);
            statement.setNull(13, Types.VARCHAR);
        }

        // World
        World world = activity.location().getWorld();
        String uuidStr = TypeUtils.uuidToDbString(world.getUID());
        statement.setString(14, world.getName());
        statement.setString(15, uuidStr);

        // Custom data
        if (activity.action() instanceof ICustomData customActionData) {
            short version = Prism.getInstance().serializerVersion();
            statement.setShort(16, version);

            String customData = customActionData.serializeCustomData();
            statement.setString(17, customData);
        } else {
            statement.setNull(16, Types.SMALLINT);
            statement.setNull(17, Types.VARCHAR);
        }

        statement.addBatch();
    }

    @Override
    public void commitBatch() throws SQLException {
        statement.executeBatch();

        // Close stuff
        statement.close();
        connection.close();
    }
}
