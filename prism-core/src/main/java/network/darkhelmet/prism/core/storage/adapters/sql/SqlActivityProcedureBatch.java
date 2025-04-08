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

import network.darkhelmet.prism.api.actions.IBlockAction;
import network.darkhelmet.prism.api.actions.ICustomData;
import network.darkhelmet.prism.api.actions.IEntityAction;
import network.darkhelmet.prism.api.actions.IMaterialAction;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.storage.IActivityBatch;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

public class SqlActivityProcedureBatch implements IActivityBatch {
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
    private final int serializerVersion;

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
            int serializerVersion,
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
            "{ CALL " + prefix + "create_activity(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }");
    }

    @Override
    public void add(ISingleActivity activity) throws SQLException {
        statement.setLong(1, activity.timestamp() / 1000);
        statement.setInt(2, activity.location().intX());
        statement.setInt(3, activity.location().intY());
        statement.setInt(4, activity.location().intZ());
        statement.setString(5, activity.action().type().key());

        // Cause/player
        if (activity.player() != null) {
            statement.setNull(6, Types.VARCHAR);
            statement.setString(7, activity.player().name());
            statement.setString(8, activity.player().uuid().toString());
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
        if (activity.action() instanceof IEntityAction) {
            statement.setString(9, ((IEntityAction) activity.action()).serializeEntityType());
        } else {
            statement.setNull(9, Types.VARCHAR);
        }

        // Material
        if (activity.action() instanceof IMaterialAction) {
            statement.setString(10, ((IMaterialAction) activity.action()).serializeMaterial());
        } else {
            statement.setNull(10, Types.VARCHAR);
        }

        // Material data
        if (activity.action() instanceof IBlockAction) {
            statement.setString(11, ((IBlockAction) activity.action()).serializeBlockData());
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
        NamedIdentity world = activity.location().world();
        statement.setString(14, world.name());
        statement.setString(15, world.uuid().toString());

        // Custom data
        if (activity.action() instanceof ICustomData) {
            statement.setInt(16, serializerVersion);

            String customData = ((ICustomData) activity.action()).serializeCustomData();
            statement.setString(17, customData);
        } else {
            statement.setNull(16, Types.SMALLINT);
            statement.setNull(17, Types.VARCHAR);
        }

        // Descriptor
        if (activity.action().descriptor() != null) {
            statement.setString(18, activity.action().descriptor());
        } else {
            statement.setNull(18, Types.VARCHAR);
        }

        // Serialize the metadata
        if (activity.action().metadata() != null) {
            try {
                statement.setString(19, activity.action().serializeMetadata());
            } catch (Exception e) {
                loggingService.handleException(e);
                statement.setNull(19, Types.VARCHAR);
            }
        } else {
            statement.setNull(19, Types.VARCHAR);
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
