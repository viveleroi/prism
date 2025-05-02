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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import network.darkhelmet.prism.api.actions.BlockAction;
import network.darkhelmet.prism.api.actions.CustomData;
import network.darkhelmet.prism.api.actions.EntityAction;
import network.darkhelmet.prism.api.actions.ItemAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.storage.ActivityBatch;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

public class SqlActivityProcedureBatch implements ActivityBatch {
    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * The hikari data source.
     */
    private final HikariDataSource hikariDataSource;

    /**
     * The serializer version.
     */
    private final short serializerVersion;

    /**
     * The connection.
     */
    private Connection connection;

    /**
     * The schema/table prefix.
     */
    private final String prefix;

    /**
     * The statement.
     */
    private CallableStatement statement;

    /**
     * Construct a new batch handler.
     *
     * @param loggingService The logging service
     * @param hikariDataSource The hikari datasource
     * @param serializerVersion The serializer version
     * @param prefix The schema/table prefix
     */
    public SqlActivityProcedureBatch(
            LoggingService loggingService,
            HikariDataSource hikariDataSource,
            short serializerVersion,
            String prefix) {
        this.loggingService = loggingService;
        this.hikariDataSource = hikariDataSource;
        this.serializerVersion = serializerVersion;
        this.prefix = prefix;
    }

    @Override
    public void startBatch() throws SQLException {
        connection = hikariDataSource.getConnection();

        statement = connection.prepareCall(
            "{ CALL " + prefix
                + "create_activity(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }");
    }

    @Override
    public void add(Activity activity) throws SQLException {
        statement.setLong(1, activity.timestamp() / 1000);
        statement.setInt(2, activity.coordinate().intX());
        statement.setInt(3, activity.coordinate().intY());
        statement.setInt(4, activity.coordinate().intZ());
        statement.setString(5, activity.action().type().key());

        // Cause/player
        if (activity.player() != null) {
            statement.setNull(6, Types.VARCHAR);
            statement.setString(7, activity.player().value());
            statement.setString(8, activity.player().key().toString());
        } else {
            if (activity.cause() != null) {
                statement.setString(6, activity.cause());
            } else {
                statement.setNull(6, Types.VARCHAR);
            }

            statement.setNull(7, Types.VARCHAR);
            statement.setNull(8, Types.VARCHAR);
        }

        // Entity
        if (activity.action() instanceof EntityAction) {
            statement.setString(9, ((EntityAction) activity.action()).serializeEntityType());
        } else {
            statement.setNull(9, Types.VARCHAR);
        }

        // Material
        if (activity.action() instanceof ItemAction itemAction) {
            statement.setString(10, itemAction.serializeMaterial());
            statement.setShort(11, (short) itemAction.quantity());
            statement.setString(12, itemAction.serializeItemData());
        } else {
            statement.setNull(10, Types.VARCHAR);
            statement.setNull(11, Types.SMALLINT);
            statement.setNull(12, Types.VARCHAR);
        }

        // Block data
        if (activity.action() instanceof BlockAction blockAction) {
            statement.setString(13, blockAction.blockNamespace());
            statement.setString(14, blockAction.blockName());
            statement.setString(15, blockAction.serializeBlockData());
        } else {
            statement.setNull(13, Types.VARCHAR);
            statement.setNull(14, Types.VARCHAR);
            statement.setNull(15, Types.VARCHAR);
        }

        // Replaced block data
        if (activity.action() instanceof BlockAction blockAction) {
            statement.setString(16, blockAction.replacedBlockNamespace());
            statement.setString(17, blockAction.replacedBlockName());
            statement.setString(18, blockAction.serializeReplacedBlockData());
        } else {
            statement.setNull(16, Types.VARCHAR);
            statement.setNull(17, Types.VARCHAR);
            statement.setNull(18, Types.VARCHAR);
        }

        // World
        statement.setString(19, activity.world().value());
        statement.setString(20, activity.world().key().toString());

        // Custom data
        if (activity.action() instanceof CustomData) {
            statement.setInt(21, serializerVersion);

            String customData = ((CustomData) activity.action()).serializeCustomData();
            statement.setString(22, customData);
        } else {
            statement.setNull(21, Types.SMALLINT);
            statement.setNull(22, Types.VARCHAR);
        }

        // Descriptor
        if (activity.action().descriptor() != null) {
            statement.setString(23, activity.action().descriptor());
        } else {
            statement.setNull(23, Types.VARCHAR);
        }

        // Serialize the metadata
        if (activity.action().metadata() != null) {
            try {
                statement.setString(24, activity.action().serializeMetadata());
            } catch (Exception e) {
                loggingService.handleException(e);
                statement.setNull(24, Types.VARCHAR);
            }
        } else {
            statement.setNull(24, Types.VARCHAR);
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
