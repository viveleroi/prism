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

import co.aikar.idb.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import network.darkhelmet.prism.api.actions.IBlockAction;
import network.darkhelmet.prism.api.actions.ICustomData;
import network.darkhelmet.prism.api.actions.IEntityAction;
import network.darkhelmet.prism.api.actions.IMaterialAction;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.storage.IActivityBatch;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.core.services.configuration.StorageConfiguration;
import network.darkhelmet.prism.core.utils.TypeUtils;

public class MysqlActivityProcedureBatch implements IActivityBatch {
    /**
     * The serializer version.
     */
    private final short serializerVersion;

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
     * @param serializerVersion The serializer version
     * @param storageConfiguration The storage configuration
     */
    public MysqlActivityProcedureBatch(short serializerVersion, StorageConfiguration storageConfiguration) {
        this.serializerVersion = serializerVersion;
        this.storageConfig = storageConfiguration;
    }

    @Override
    public void startBatch() throws SQLException {
        connection = DB.getGlobalDatabase().getConnection();

        statement = connection.prepareCall(
            "{ CALL createActivity(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }");
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
            String uuidStr = TypeUtils.uuidToDbString(activity.player().uuid());

            statement.setNull(6, Types.VARCHAR);
            statement.setString(7, activity.player().name());
            statement.setString(8, uuidStr);
        } else {
            String cause = activity.cause() == null ? "unknown" : activity.cause();

            statement.setString(6, cause);
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
        if (activity.action() instanceof IBlockAction) {
            IBlockAction blockAction = (IBlockAction) activity.action();
            statement.setString(12, blockAction.serializeReplacedMaterial());
            statement.setString(13, blockAction.serializeReplacedBlockData());
        } else {
            statement.setNull(12, Types.VARCHAR);
            statement.setNull(13, Types.VARCHAR);
        }

        // World
        NamedIdentity world = activity.location().world();
        String uuidStr = TypeUtils.uuidToDbString(world.uuid());
        statement.setString(14, world.name());
        statement.setString(15, uuidStr);

        // Custom data
        if (activity.action() instanceof ICustomData) {
            statement.setShort(16, serializerVersion);

            String customData = ((ICustomData) activity.action()).serializeCustomData();
            statement.setString(17, customData);
        } else {
            statement.setNull(16, Types.SMALLINT);
            statement.setNull(17, Types.VARCHAR);
        }

        // Descriptor
        statement.setString(18, activity.action().descriptor());

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
