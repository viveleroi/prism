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

package org.prism_mc.prism.core.storage.adapters.clickhouse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.jooq.SQLDialect;
import org.prism_mc.prism.api.actions.types.ActionTypeRegistry;
import org.prism_mc.prism.api.activities.AbstractActivity;
import org.prism_mc.prism.api.activities.Activity;
import org.prism_mc.prism.api.activities.ActivityQuery;
import org.prism_mc.prism.api.services.airtags.AirtagSummary;
import org.prism_mc.prism.api.services.modifications.ActivityStream;
import org.prism_mc.prism.api.services.pagination.PartialListPaginationResult;
import org.prism_mc.prism.api.storage.ActivityBatch;
import org.prism_mc.prism.api.storage.World;
import org.prism_mc.prism.api.util.Pair;
import org.prism_mc.prism.core.injection.factories.SqlActivityQueryBuilderFactory;
import org.prism_mc.prism.core.services.cache.CacheService;
import org.prism_mc.prism.core.storage.HikariConfigFactories;
import org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter;
import org.prism_mc.prism.core.storage.adapters.sql.SqlSchemaUpdater;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;
import org.prism_mc.prism.loader.services.logging.LoggingService;

@Singleton
public class ClickhouseStorageAdapter extends AbstractSqlStorageAdapter {

    /**
     * The activity id sequence.
     */
    private final AtomicLong activityIdSequence = new AtomicLong(0);

    /**
     * The denormalized read/lookup query builder for the flat ClickHouse fact table.
     */
    private ClickhouseActivityQueryBuilder clickhouseQueryBuilder;

    /**
     * The parameterized activities insert statement, loaded once with the prefix resolved.
     */
    private String activityInsertSql;

    /**
     * Constructor.
     *
     * @param loggingService The logging service
     * @param configurationService The configuration service
     * @param actionRegistry The action type registry
     * @param schemaUpdater The schema updater
     * @param queryBuilderFactory The query builder factory
     * @param cacheService The cache service
     * @param serializerVersion The serializer version
     * @param dataPath The plugin file path
     */
    @Inject
    public ClickhouseStorageAdapter(
        LoggingService loggingService,
        ConfigurationService configurationService,
        ActionTypeRegistry actionRegistry,
        SqlSchemaUpdater schemaUpdater,
        SqlActivityQueryBuilderFactory queryBuilderFactory,
        CacheService cacheService,
        @Named("serializerVersion") short serializerVersion,
        Path dataPath
    ) {
        super(
            loggingService,
            configurationService,
            actionRegistry,
            schemaUpdater,
            queryBuilderFactory,
            cacheService,
            serializerVersion,
            dataPath
        );
        try {
            var hikariConfig = HikariConfigFactories.clickhouse(configurationService.storageConfig());
            var usingHikariProperties = false;

            if (hikariPropertiesFile.exists()) {
                loggingService.info("Using hikari.properties");

                hikariConfig = new HikariConfig(hikariPropertiesFile.getPath());
                usingHikariProperties = true;
            }

            if (connect(hikariConfig, SQLDialect.CLICKHOUSE)) {
                describeDatabase(hikariConfig, usingHikariProperties);
                prepareSchema();

                warnIfLightweightUpdateUnsupported();

                clickhouseQueryBuilder = new ClickhouseActivityQueryBuilder(
                    dataSource,
                    prefix,
                    actionRegistry,
                    loggingService
                );

                ready = true;
            }
        } catch (Exception e) {
            loggingService.handleException(e);
        }
    }

    @Override
    protected void prepareSchema() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(loadSqlFromResourceFile("clickhouse", "prism_activities", prefix));
                stmt.execute(loadSqlFromResourceFile("clickhouse", "prism_airtags", prefix));
            }

            new ClickhouseSchemaUpdater(loggingService).update(connection, prefix);
        }

        activityInsertSql = loadSqlFromResourceFile("clickhouse", "prism_activities_insert", prefix);

        seedActivityIdSequence();
    }

    /**
     * Seed the activity id sequence from the highest id already stored so ids stay monotonic across restarts.
     *
     * @throws SQLException The database exception
     */
    private void seedActivityIdSequence() throws SQLException {
        try (
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT max(activity_id) FROM %sactivities", prefix))
        ) {
            if (rs.next()) {
                activityIdSequence.set(rs.getLong(1));
            }
        }
    }

    /**
     * Warn if the connected ClickHouse server is older than 25.8, where the lightweight
     * {@code UPDATE} required for rollback/restore first became available.
     */
    private void warnIfLightweightUpdateUnsupported() {
        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT version()")
        ) {
            if (resultSet.next()) {
                String version = resultSet.getString(1);
                if (!supportsLightweightUpdate(version)) {
                    loggingService.warn(
                        "ClickHouse {0} detected. Rollback and restore require ClickHouse 25.8+ " +
                        "(lightweight UPDATE) and will fail on this server.",
                        version
                    );
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }
    }

    /**
     * Whether a ClickHouse version string is at least 25.8 (lightweight update support).
     *
     * @param version The server version string
     * @return True if lightweight updates are supported (or the version could not be parsed)
     */
    private static boolean supportsLightweightUpdate(String version) {
        try {
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return major > 25 || (major == 25 && minor >= 8);
        } catch (RuntimeException e) {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the denormalized ClickHouse query builder (grouped {@code /lookup} display).</p>
     */
    @Override
    public PartialListPaginationResult<AbstractActivity> queryActivitiesPaginated(ActivityQuery query) {
        return clickhouseQueryBuilder.queryActivitiesPaginated(query);
    }

    @Override
    public int countActivities(ActivityQuery query) {
        return clickhouseQueryBuilder.countActivities(query);
    }

    @Override
    public List<Activity> queryActivities(ActivityQuery query) {
        return clickhouseQueryBuilder.queryActivities(query);
    }

    @Override
    public List<World> worlds() {
        return clickhouseQueryBuilder.worlds();
    }

    @Override
    public ActivityStream streamActivities(ActivityQuery query) {
        int maxPerOperation = configurationService.prismConfig().modifications().maxPerOperation();

        return clickhouseQueryBuilder.streamActivities(query, maxPerOperation);
    }

    @Override
    public Pair<Integer, Integer> getActivitiesPkBounds(ActivityQuery query) {
        return clickhouseQueryBuilder.getActivitiesPkBounds(query);
    }

    @Override
    public int deleteActivities(ActivityQuery query, int cycleMinPrimaryKey, int cycleMaxPrimaryKey) {
        return clickhouseQueryBuilder.deleteActivities(query, cycleMinPrimaryKey, cycleMaxPrimaryKey);
    }

    @Override
    public void markReversed(List<Long> activityIds, boolean reversed) {
        clickhouseQueryBuilder.markReversed(activityIds, reversed);
    }

    @Override
    public boolean airtagExists(String airtag) {
        return clickhouseQueryBuilder.airtagExists(airtag);
    }

    @Override
    public int createAirtag(String airtag, UUID playerUuid, String playerName) throws SQLException {
        return clickhouseQueryBuilder.createAirtag(airtag, playerUuid, playerName);
    }

    @Override
    public int deleteAirtag(String airtag, UUID playerUuid) {
        return clickhouseQueryBuilder.deleteAirtag(airtag, playerUuid);
    }

    @Override
    public List<AirtagSummary> queryAirtagsForPlayer(UUID playerUuid, int limit) {
        return clickhouseQueryBuilder.queryAirtagsForPlayer(playerUuid, limit);
    }

    @Override
    public int countAirtagsForPlayer(UUID playerUuid) {
        return clickhouseQueryBuilder.countAirtagsForPlayer(playerUuid);
    }

    @Override
    public ActivityBatch createActivityBatch() {
        return new ClickhouseActivityBatch(
            loggingService,
            dataSource,
            serializerVersion,
            prefix,
            activityInsertSql,
            activityIdSequence
        );
    }
}
