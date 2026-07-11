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

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.prism_mc.prism.api.actions.ActionData;
import org.prism_mc.prism.api.actions.types.ActionTypeRegistry;
import org.prism_mc.prism.api.activities.AbstractActivity;
import org.prism_mc.prism.api.activities.Activity;
import org.prism_mc.prism.api.activities.ActivityQuery;
import org.prism_mc.prism.api.activities.Cause;
import org.prism_mc.prism.api.activities.GroupedActivity;
import org.prism_mc.prism.api.containers.PlayerContainer;
import org.prism_mc.prism.api.containers.StringContainer;
import org.prism_mc.prism.api.containers.TranslatableContainer;
import org.prism_mc.prism.api.services.airtags.AirtagSummary;
import org.prism_mc.prism.api.services.modifications.ActivityStream;
import org.prism_mc.prism.api.services.pagination.PartialListPaginationResult;
import org.prism_mc.prism.api.storage.World;
import org.prism_mc.prism.api.util.Coordinate;
import org.prism_mc.prism.api.util.Pair;
import org.prism_mc.prism.loader.services.logging.LoggingService;

public class ClickhouseActivityQueryBuilder {

    /**
     * The non-aggregate columns selected for both grouped and ungrouped queries.
     *
     * <p>Grouped queries also reuse this exact set as their {@code GROUP BY} key: every non-aggregate
     * column in the grouped {@code SELECT} appears here, and {@code GROUP BY} is order-insensitive.</p>
     */
    private static final String BASE_COLUMNS =
        "world_uuid, world, affected_material, affected_item_data, affected_item_quantity, " +
        "affected_block_ns, affected_block_name, affected_block_translation_key, affected_entity_type, " +
        "action, cause_player_uuid, cause_player, descriptor, metadata, affected_player, affected_player_uuid, " +
        "cause_entity_type_translation_key, cause_block_translation_key, cause, reversed";

    /**
     * The full column set selected for modification (rollback/restore/preview) rows.
     */
    private static final String MODIFICATION_COLUMNS =
        "world_uuid, world, affected_material, affected_item_data, affected_item_quantity, " +
        "affected_block_ns, affected_block_name, affected_block_data, affected_block_translation_key, " +
        "replaced_block_ns, replaced_block_name, replaced_block_data, affected_entity_type, action, " +
        "cause, cause_player, cause_player_uuid, cause_entity_type_translation_key, cause_block_translation_key, " +
        "serializer_version, serialized_data, reversed, activity_id, toUnixTimestamp(`timestamp`) AS ts, x, y, z";

    /**
     * The chunk size for reversed-flag updates, mirroring the normalized backend.
     */
    private static final int MARK_REVERSED_CHUNK_SIZE = 1000;

    /**
     * The hikari data source.
     */
    private final HikariDataSource dataSource;

    /**
     * The schema/table prefix.
     */
    private final String prefix;

    /**
     * The action type registry.
     */
    private final ActionTypeRegistry actionRegistry;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * Construct a new query builder.
     *
     * @param dataSource The hikari data source
     * @param prefix The schema/table prefix
     * @param actionRegistry The action type registry
     * @param loggingService The logging service
     */
    public ClickhouseActivityQueryBuilder(
        HikariDataSource dataSource,
        String prefix,
        ActionTypeRegistry actionRegistry,
        LoggingService loggingService
    ) {
        this.dataSource = dataSource;
        this.prefix = prefix;
        this.actionRegistry = actionRegistry;
        this.loggingService = loggingService;
    }

    /**
     * Query activities and wrap them in a paginated result.
     *
     * @param query The activity query
     * @return The paginated activity results
     */
    public PartialListPaginationResult<AbstractActivity> queryActivitiesPaginated(ActivityQuery query) {
        long[] totalResults = new long[] { 0 };
        List<AbstractActivity> activities = execute(query, totalResults);

        int currentPage = query.limit() > 0 ? (query.offset() / query.limit()) + 1 : 1;

        return new PartialListPaginationResult<>(activities, (int) totalResults[0], query.limit(), currentPage);
    }

    /**
     * Count the activities matching a query.
     *
     * @param query The activity query
     * @return The count of matching activities
     */
    public int countActivities(ActivityQuery query) {
        List<Object> parameters = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT count() AS total FROM ");
        sql.append(tableName());
        appendWhere(query, sql, parameters);

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            bindParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return (int) resultSet.getLong("total");
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return 0;
    }

    /**
     * Query activities as an ungrouped list, keeping only individual (non-grouped) records.
     *
     * @param query The activity query
     * @return The list of activities
     */
    public List<Activity> queryActivities(ActivityQuery query) {
        List<AbstractActivity> mapped = execute(query, null);

        List<Activity> activities = new ArrayList<>();
        for (AbstractActivity abstractActivity : mapped) {
            if (abstractActivity instanceof Activity activity) {
                activities.add(activity);
            }
        }

        return activities;
    }

    /**
     * Open a streaming source of fully-reconstructed activities for a modification (rollback,
     * restore, or preview) query.
     *
     * <p>Mirrors the normalized adapter's strategy: only the matching {@code activity_id}s are held
     * in memory (in rollback ordering), and each {@link ActivityStream#next(int)} batch re-fetches the
     * full rows for its slice, re-applying the modification ordering. This keeps memory bounded on
     * large rollbacks. The primary-key fetch is capped by {@code maxPerOperation} when configured.</p>
     *
     * @param query The activity query (expected to be a modification query)
     * @param maxPerOperation The hard ceiling on the number of activities to stream, or {@code <= 0} for none
     * @return A closeable, replayable activity stream
     */
    public ActivityStream streamActivities(ActivityQuery query, int maxPerOperation) {
        ActivityQuery effectiveQuery = query;
        if (maxPerOperation > 0 && (query.limit() <= 0 || query.limit() > maxPerOperation)) {
            effectiveQuery = query.toBuilder().limit(maxPerOperation).build();
        }

        List<Long> pks = queryActivityPks(effectiveQuery);

        return new ClickhouseBatchedActivityStream(pks, query);
    }

    /**
     * Fetch only the {@code activity_id}s matching a modification query, in rollback ordering.
     *
     * @param query The activity query
     * @return The activity ids in modification ordering
     */
    private List<Long> queryActivityPks(ActivityQuery query) {
        List<Object> parameters = new ArrayList<>();
        List<String> clauses = collectConditions(query, parameters);

        StringBuilder sql = new StringBuilder("SELECT activity_id FROM ");
        sql.append(tableName());
        if (!clauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", clauses));
        }

        appendModificationOrdering(sql);
        appendLimitOffset(query, sql);

        List<Long> pks = new ArrayList<>();

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            bindParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    pks.add(resultSet.getLong("activity_id"));
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return pks;
    }

    /**
     * Fetch the full modification rows for a batch of activity ids, preserving modification ordering.
     *
     * @param pks The activity ids to fetch
     * @param query The original modification query
     * @return The fully-reconstructed activities
     */
    private List<Activity> queryActivitiesByPks(Collection<Long> pks, ActivityQuery query) {
        List<Activity> activities = new ArrayList<>();
        if (pks == null || pks.isEmpty()) {
            return activities;
        }

        List<Object> parameters = new ArrayList<>(pks);

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(MODIFICATION_COLUMNS);
        sql.append(" FROM ").append(tableName());
        sql.append(" WHERE activity_id IN (").append(placeholders(pks.size())).append(")");
        appendModificationOrdering(sql);

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            bindParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Activity activity = mapModificationRow(resultSet, query);
                    if (activity != null) {
                        activities.add(activity);
                    }
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return activities;
    }

    /**
     * Map a single result-set row to a fully-reconstructed modification activity.
     *
     * @param resultSet The result set positioned on a row
     * @param query The original modification query
     * @return The mapped activity, or null if the action type is unknown or the row cannot be built
     * @throws SQLException On error reading the row
     */
    private Activity mapModificationRow(ResultSet resultSet, ActivityQuery query) throws SQLException {
        String actionKey = resultSet.getString("action");
        var optionalActionType = actionRegistry.actionType(actionKey);
        if (optionalActionType.isEmpty()) {
            loggingService.warn("Failed to find action type: {0}", actionKey);
            return null;
        }

        var actionType = optionalActionType.get();

        // World. The flat table stores an absent world uuid as an empty string, which is not a valid
        // UUID, so skip the row rather than let UUID.fromString throw and abort the whole query.
        String worldUuidValue = emptyToNull(resultSet.getString("world_uuid"));
        if (worldUuidValue == null) {
            loggingService.warn("Skipping activity with missing world uuid for action: {0}", actionKey);
            return null;
        }
        UUID worldUuid = UUID.fromString(worldUuidValue);
        var world = new Pair<>(worldUuid, resultSet.getString("world"));

        // Location
        Coordinate coordinate = new Coordinate(resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));

        // Entity type
        String entityType = emptyToNull(resultSet.getString("affected_entity_type"));
        if (entityType != null) {
            entityType = entityType.toUpperCase(Locale.ENGLISH);
        }

        // Material
        String material = emptyToNull(resultSet.getString("affected_material"));
        if (material != null) {
            material = material.toUpperCase(Locale.ENGLISH);
        }

        String itemData = emptyToNull(resultSet.getString("affected_item_data"));

        // Item quantity
        short itemQuantity = resultSet.getShort("affected_item_quantity");
        if (resultSet.wasNull()) {
            itemQuantity = 0;
        }

        // Cause
        Cause cause = buildCause(resultSet, query);

        // Affected block
        String blockNamespace = emptyToNull(resultSet.getString("affected_block_ns"));
        String blockName = emptyToNull(resultSet.getString("affected_block_name"));
        String blockData = emptyToNull(resultSet.getString("affected_block_data"));
        String translationKey = emptyToNull(resultSet.getString("affected_block_translation_key"));

        // Replaced block
        String replacedBlockNamespace = emptyToNull(resultSet.getString("replaced_block_ns"));
        String replacedBlockName = emptyToNull(resultSet.getString("replaced_block_name"));
        String replacedBlockData = emptyToNull(resultSet.getString("replaced_block_data"));

        // Custom data
        String customData = emptyToNull(resultSet.getString("serialized_data"));
        short customDataVersion = resultSet.getShort("serializer_version");
        if (resultSet.wasNull()) {
            customDataVersion = 1;
        }

        long timestamp = resultSet.getLong("ts");
        long activityId = resultSet.getLong("activity_id");

        // Build the action data
        ActionData actionData = new ActionData(
            material,
            itemQuantity,
            itemData,
            blockNamespace,
            blockName,
            blockData,
            replacedBlockNamespace,
            replacedBlockName,
            replacedBlockData,
            entityType,
            customData,
            null,
            null,
            customDataVersion,
            translationKey,
            null,
            null,
            null
        );

        try {
            return new Activity(
                activityId,
                actionType.createAction(actionData),
                world,
                coordinate,
                cause,
                timestamp,
                false
            );
        } catch (Exception e) {
            loggingService.handleException(e);
            return null;
        }
    }

    /**
     * Append the rollback/restore modification ordering, translated to flat columns.
     *
     * <p>Replicates the normalized {@code addModificationOrdering}: hanging/vine blocks sort after
     * everything else, then rows sort by {@code x}/{@code z} ascending, with a per-block {@code y}
     * direction so build-up blocks (pointed dripstone, cave/weeping vine plants, vines) are rebuilt
     * bottom-up while everything else builds top-down. The block-name literals are hard-coded (not
     * user input) so they are inlined, mirroring the normalized query.</p>
     *
     * <p>A final {@code activity_id ASC} key is appended as a unique tiebreaker. ClickHouse
     * parallelizes reads across parts, so rows tied on (block-CASE, x, z, y) come back in
     * nondeterministic order; without a stable tiebreaker the pk-selection query and the per-batch
     * re-order query can disagree and rollback would apply same-coordinate changes in the wrong
     * order. This ordering must stay in sync with {@code SqlActivityQueryBuilder.addModificationOrdering}.</p>
     *
     * @param sql The SQL builder
     */
    private void appendModificationOrdering(StringBuilder sql) {
        sql.append(" ORDER BY ");
        sql.append("(CASE WHEN affected_block_name IN ('cave_vines', 'weeping_vines') THEN 1 ELSE -1 END) ASC, ");
        sql.append(
            "(CASE WHEN affected_block_name IN ('cave_vines_plant', 'weeping_vines_plant') THEN 1 ELSE -1 END) ASC, "
        );
        sql.append("(CASE WHEN affected_block_name IN ('vine', 'pointed_dripstone') THEN 1 ELSE -1 END) ASC, ");
        sql.append("x ASC, z ASC, ");
        sql.append(
            "(CASE WHEN affected_block_name IN " +
            "('pointed_dripstone', 'cave_vines_plant', 'weeping_vines_plant', 'vine') THEN y END) DESC, "
        );
        sql.append(
            "(CASE WHEN affected_block_name NOT IN " +
            "('pointed_dripstone', 'cave_vines_plant', 'weeping_vines_plant', 'vine') THEN y END) ASC, "
        );
        sql.append("activity_id ASC");
    }

    /**
     * A pull-based activity stream that holds only the matching activity ids in memory, re-fetching
     * full modification rows per batch. Mirrors the normalized adapter's batched stream so preview →
     * apply replay and progress reporting behave identically.
     */
    private final class ClickhouseBatchedActivityStream implements ActivityStream {

        /**
         * The activity ids to stream, in modification ordering.
         */
        private final List<Long> pks;

        /**
         * The original modification query, reused for per-batch row mapping.
         */
        private final ActivityQuery query;

        /**
         * The total number of activities the stream will yield.
         */
        private final int total;

        /**
         * The index of the next activity id to yield.
         */
        private int cursor;

        /**
         * Whether the stream has been closed.
         */
        private boolean closed;

        /**
         * Construct a new batched stream.
         *
         * @param pks The activity ids in modification ordering
         * @param query The original modification query
         */
        ClickhouseBatchedActivityStream(List<Long> pks, ActivityQuery query) {
            this.pks = pks;
            this.query = query;
            this.total = pks.size();
        }

        @Override
        public List<Activity> next(int limit) {
            List<Long> batchPks;
            synchronized (this) {
                if (closed || cursor >= pks.size() || limit <= 0) {
                    return List.of();
                }

                int end = Math.min(cursor + limit, pks.size());
                batchPks = List.copyOf(pks.subList(cursor, end));
                cursor = end;
            }

            return queryActivitiesByPks(batchPks, query);
        }

        @Override
        public synchronized void close() {
            closed = true;
            pks.clear();
        }

        @Override
        public synchronized void reopen() {
            if (closed) {
                throw new IllegalStateException("Cannot reopen a closed ActivityStream");
            }

            cursor = 0;
        }

        @Override
        public int total() {
            return total;
        }
    }

    /**
     * Query the distinct worlds present in the fact table.
     *
     * <p>The denormalized table has no world primary key, so a synthetic sequential id is assigned
     * for display purposes only.</p>
     *
     * @return The list of worlds
     */
    public List<World> worlds() {
        List<World> worlds = new ArrayList<>();

        String sql = String.format(
            "SELECT DISTINCT world, world_uuid FROM %s WHERE world_uuid != '' ORDER BY world",
            tableName()
        );

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            try (ResultSet resultSet = statement.executeQuery()) {
                int id = 1;
                while (resultSet.next()) {
                    worlds.add(new World(id++, resultSet.getString("world"), resultSet.getString("world_uuid")));
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return worlds;
    }

    /**
     * Get the min/max activity id bounds for a purge query.
     *
     * @param query The activity query
     * @return The min/max activity id pair
     */
    public Pair<Integer, Integer> getActivitiesPkBounds(ActivityQuery query) {
        List<Object> parameters = new ArrayList<>();
        List<String> clauses = collectConditions(query, parameters);

        StringBuilder sql = new StringBuilder("SELECT min(activity_id) AS mn, max(activity_id) AS mx FROM ");
        sql.append(tableName());
        if (!clauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", clauses));
        }

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            bindParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Pair<>((int) resultSet.getLong("mn"), (int) resultSet.getLong("mx"));
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return new Pair<>(0, 0);
    }

    /**
     * Delete activities matching a query within an activity id window using a ClickHouse lightweight
     * delete.
     *
     * @param query The activity query
     * @param minPrimaryKey The minimum activity id for this cycle
     * @param maxPrimaryKey The maximum activity id for this cycle
     * @return The number of deleted rows reported by the driver
     */
    public int deleteActivities(ActivityQuery query, int minPrimaryKey, int maxPrimaryKey) {
        List<Object> parameters = new ArrayList<>();
        List<String> clauses = collectConditions(query, parameters);
        clauses.add("activity_id BETWEEN ? AND ?");
        parameters.add(minPrimaryKey);
        parameters.add(maxPrimaryKey);

        String where = " WHERE " + String.join(" AND ", clauses);

        try (Connection connection = dataSource.getConnection()) {
            // ClickHouse lightweight deletes do not report an affected-row count, so count the
            // matching rows first. The window is stable (new activities get higher ids and already
            // deleted rows are excluded from subsequent counts), so this stays accurate.
            int deleted = 0;
            try (
                PreparedStatement countStatement = connection.prepareStatement(
                    String.format("SELECT count() AS total FROM %s%s", tableName(), where)
                )
            ) {
                bindParameters(countStatement, parameters);

                try (ResultSet resultSet = countStatement.executeQuery()) {
                    if (resultSet.next()) {
                        deleted = (int) resultSet.getLong("total");
                    }
                }
            }

            try (
                PreparedStatement deleteStatement = connection.prepareStatement(
                    String.format("DELETE FROM %s%s", tableName(), where)
                )
            ) {
                bindParameters(deleteStatement, parameters);
                deleteStatement.executeUpdate();
            }

            return deleted;
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return 0;
    }

    /**
     * Set the reversed flag for a list of activities using ClickHouse lightweight updates.
     *
     * @param activityIds The activity ids to flag
     * @param reversed Whether the activities are reversed
     */
    public void markReversed(List<Long> activityIds, boolean reversed) {
        if (activityIds == null || activityIds.isEmpty()) {
            return;
        }

        for (int start = 0; start < activityIds.size(); start += MARK_REVERSED_CHUNK_SIZE) {
            List<Long> chunk = activityIds.subList(
                start,
                Math.min(start + MARK_REVERSED_CHUNK_SIZE, activityIds.size())
            );

            String sql = String.format(
                "UPDATE %s SET reversed = ? WHERE activity_id IN (%s)",
                tableName(),
                placeholders(chunk.size())
            );

            try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
            ) {
                statement.setInt(1, reversed ? 1 : 0);
                for (int i = 0; i < chunk.size(); i++) {
                    statement.setObject(i + 2, chunk.get(i));
                }

                statement.executeUpdate();
            } catch (SQLException e) {
                loggingService.handleException(e);
            }
        }
    }

    /**
     * Whether an airtag row exists in the flat airtags table.
     *
     * @param airtag The airtag id
     * @return True if a row with this airtag id exists
     */
    public boolean airtagExists(String airtag) {
        String sql = String.format("SELECT count() AS total FROM %s FINAL WHERE airtag = ?", airtagsTableName());

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, airtag);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("total") > 0;
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return false;
    }

    /**
     * Insert a new airtag row owned by a player, with an empty latest-item pointer.
     *
     * @param airtag The airtag id
     * @param playerUuid The owning player's UUID
     * @param playerName The owning player's name
     * @return 1 if a row was inserted, 0 if the airtag already existed
     * @throws SQLException On error
     */
    public int createAirtag(String airtag, UUID playerUuid, String playerName) throws SQLException {
        if (airtagExists(airtag)) {
            return 0;
        }

        String sql = String.format(
            "INSERT INTO %s (airtag, player_uuid, player_name, created_at, " +
            "latest_item_material, latest_item_data, latest_item_timestamp) VALUES (?, ?, ?, ?, '', '', 0)",
            airtagsTableName()
        );

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, airtag);
            statement.setString(2, playerUuid.toString());
            statement.setString(3, playerName != null ? playerName : "");
            statement.setLong(4, Instant.now().getEpochSecond());
            statement.executeUpdate();
        }

        return 1;
    }

    /**
     * Delete the airtag row(s) matching an airtag id, optionally scoped to an owning player.
     *
     * @param airtag The airtag id
     * @param playerUuid When non-null, only rows owned by this player are deleted; when null, all
     *     rows for the airtag are deleted
     * @return The number of rows deleted
     */
    public int deleteAirtag(String airtag, UUID playerUuid) {
        List<Object> parameters = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE airtag = ?");
        parameters.add(airtag);

        if (playerUuid != null) {
            where.append(" AND player_uuid = ?");
            parameters.add(playerUuid.toString());
        }

        try (Connection connection = dataSource.getConnection()) {
            int deleted = 0;
            try (
                PreparedStatement countStatement = connection.prepareStatement(
                    String.format("SELECT count() AS total FROM %s FINAL%s", airtagsTableName(), where)
                )
            ) {
                bindParameters(countStatement, parameters);

                try (ResultSet resultSet = countStatement.executeQuery()) {
                    if (resultSet.next()) {
                        deleted = (int) resultSet.getLong("total");
                    }
                }
            }

            if (deleted == 0) {
                return 0;
            }

            try (
                PreparedStatement clearStatement = connection.prepareStatement(
                    String.format("UPDATE %s SET affected_item_airtag = '' WHERE affected_item_airtag = ?", tableName())
                )
            ) {
                clearStatement.setString(1, airtag);
                clearStatement.executeUpdate();
            }

            try (
                PreparedStatement deleteStatement = connection.prepareStatement(
                    String.format("DELETE FROM %s%s", airtagsTableName(), where)
                )
            ) {
                bindParameters(deleteStatement, parameters);
                deleteStatement.executeUpdate();
            }

            return deleted;
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return 0;
    }

    /**
     * List the airtags owned by a player, newest first.
     *
     * @param playerUuid The owning player's UUID
     * @param limit The maximum number of results to return
     * @return The airtag summaries
     */
    public List<AirtagSummary> queryAirtagsForPlayer(UUID playerUuid, int limit) {
        List<AirtagSummary> summaries = new ArrayList<>();

        String sql = String.format(
            "SELECT airtag, latest_item_material, latest_item_data, created_at FROM %s " +
            "FINAL WHERE player_uuid = ? ORDER BY created_at DESC LIMIT ?",
            airtagsTableName()
        );

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, playerUuid.toString());
            statement.setInt(2, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    summaries.add(
                        new AirtagSummary(
                            resultSet.getString("airtag"),
                            resultSet.getString("latest_item_material"),
                            resultSet.getString("latest_item_data"),
                            resultSet.getLong("created_at")
                        )
                    );
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return summaries;
    }

    /**
     * Count the airtags owned by a player.
     *
     * @param playerUuid The owning player's UUID
     * @return The number of airtags owned
     */
    public int countAirtagsForPlayer(UUID playerUuid) {
        String sql = String.format("SELECT count() AS total FROM %s FINAL WHERE player_uuid = ?", airtagsTableName());

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, playerUuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return (int) resultSet.getLong("total");
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return 0;
    }

    /**
     * Execute an activity query and map each result row to an activity.
     *
     * @param query The activity query
     * @param totalResultsOut A single-element array populated with the {@code totalrows} value, or null
     * @return The mapped activities
     */
    private List<AbstractActivity> execute(ActivityQuery query, long[] totalResultsOut) {
        List<Object> parameters = new ArrayList<>();
        String sql = query.grouped() ? buildGroupedSql(query, parameters) : buildUngroupedSql(query, parameters);

        List<AbstractActivity> activities = new ArrayList<>();

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            bindParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                boolean first = true;
                while (resultSet.next()) {
                    if (first) {
                        if (totalResultsOut != null) {
                            totalResultsOut[0] = resultSet.getLong("totalrows");
                        }

                        first = false;
                    }

                    AbstractActivity activity = mapRow(resultSet, query);
                    if (activity != null) {
                        activities.add(activity);
                    }
                }
            }
        } catch (SQLException e) {
            loggingService.handleException(e);
        }

        return activities;
    }

    /**
     * Build the grouped lookup SQL, appending bound parameters in order.
     *
     * @param query The activity query
     * @param parameters The ordered parameter list to populate
     * @return The SQL string
     */
    private String buildGroupedSql(ActivityQuery query, List<Object> parameters) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(BASE_COLUMNS);
        sql.append(", count() OVER () AS totalrows");
        sql.append(", avg(toUnixTimestamp(`timestamp`)) AS avgtime");
        sql.append(", count() AS groupcount");
        sql.append(" FROM ").append(tableName());
        appendWhere(query, sql, parameters);
        sql.append(" GROUP BY ").append(BASE_COLUMNS);
        sql.append(" ORDER BY avgtime ").append(direction(query));
        appendLimitOffset(query, sql);

        return sql.toString();
    }

    /**
     * Build the ungrouped lookup SQL, appending bound parameters in order.
     *
     * @param query The activity query
     * @param parameters The ordered parameter list to populate
     * @return The SQL string
     */
    private String buildUngroupedSql(ActivityQuery query, List<Object> parameters) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(BASE_COLUMNS);
        sql.append(", count() OVER () AS totalrows");
        sql.append(", activity_id, toUnixTimestamp(`timestamp`) AS ts, x, y, z");
        sql.append(" FROM ").append(tableName());
        appendWhere(query, sql, parameters);
        sql.append(" ORDER BY ts ").append(direction(query)).append(", activity_id ").append(direction(query));
        appendLimitOffset(query, sql);

        return sql.toString();
    }

    /**
     * Append the {@code WHERE} clause for the query, populating the ordered parameter list.
     *
     * @param query The activity query
     * @param sql The SQL builder
     * @param parameters The ordered parameter list to populate
     */
    private void appendWhere(ActivityQuery query, StringBuilder sql, List<Object> parameters) {
        List<String> clauses = collectConditions(query, parameters);
        if (!clauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", clauses));
        }
    }

    /**
     * Collect the {@code WHERE} conditions for a query, populating the ordered parameter list.
     *
     * @param query The activity query
     * @param parameters The ordered parameter list to populate
     * @return The list of condition clause strings
     */
    private List<String> collectConditions(ActivityQuery query, List<Object> parameters) {
        List<String> clauses = new ArrayList<>();

        // Action type keys
        addInclusion(clauses, parameters, "action", query.allActionTypeKeys());
        addExclusion(clauses, parameters, "action", query.actionTypeKeysExcluded());

        // Activity ids
        if (query.activityIds() != null && !query.activityIds().isEmpty()) {
            addInclusion(clauses, parameters, "activity_id", query.activityIds());
        }

        // Materials
        addInclusion(clauses, parameters, "affected_material", query.affectedMaterials());
        addExclusion(clauses, parameters, "affected_material", query.affectedMaterialsExcluded());

        // Affected blocks
        addInclusion(clauses, parameters, "affected_block_name", query.affectedBlocks());
        addExclusion(clauses, parameters, "affected_block_name", query.affectedBlocksExcluded());

        // Cause blocks
        addInclusion(clauses, parameters, "cause_block_name", query.causeBlocks());
        addExclusion(clauses, parameters, "cause_block_name", query.causeBlocksExcluded());

        // Affected entity types
        addInclusion(clauses, parameters, "affected_entity_type", query.affectedEntityTypes());
        addExclusion(clauses, parameters, "affected_entity_type", query.affectedEntityTypesExcluded());

        // Cause entity types
        addInclusion(clauses, parameters, "cause_entity_type", query.causeEntityTypes());
        addExclusion(clauses, parameters, "cause_entity_type", query.causeEntityTypesExcluded());

        // Affected players
        addInclusion(clauses, parameters, "affected_player", query.affectedPlayerNames());
        addExclusion(clauses, parameters, "affected_player", query.affectedPlayerNamesExcluded());

        // Cause players
        addInclusion(clauses, parameters, "cause_player", query.causePlayerNames());
        addExclusion(clauses, parameters, "cause_player", query.causePlayerNamesExcluded());

        // Named cause
        if (query.namedCause() != null) {
            addInclusion(clauses, parameters, "cause", List.of(query.namedCause()));
        }
        if (query.namedCauseExcluded() != null) {
            addExclusion(clauses, parameters, "cause", List.of(query.namedCauseExcluded()));
        }

        // World uuid
        if (query.worldUuid() != null) {
            addInclusion(clauses, parameters, "world_uuid", List.of(query.worldUuid().toString()));
        }
        if (query.worldUuidExcluded() != null) {
            addExclusion(clauses, parameters, "world_uuid", List.of(query.worldUuidExcluded().toString()));
        }

        if (query.airtag() != null) {
            clauses.add("affected_item_airtag = ?");
            parameters.add(query.airtag());
        }

        // Descriptor. The raw value is escaped so LIKE metacharacters (% and _) in the search term
        // match literally rather than acting as wildcards (e.g. "oak_log" must not match "oakXlog").
        // The normalized SqlActivityQueryBuilder still binds the raw value here and has the same
        // unescaped-wildcard issue; that is a separate cross-backend fix.
        if (query.descriptor() != null) {
            clauses.add("descriptor ILIKE concat('%', ?, '%')");
            parameters.add(escapeLikePattern(query.descriptor()));
        }

        // Reversed
        if (query.reversed() != null) {
            clauses.add("reversed = ?");
            parameters.add(query.reversed() ? 1 : 0);
        }

        // Locations
        if (query.coordinate() != null) {
            clauses.add("x = ?");
            parameters.add(query.coordinate().intX());
            clauses.add("y = ?");
            parameters.add(query.coordinate().intY());
            clauses.add("z = ?");
            parameters.add(query.coordinate().intZ());
        } else if (query.minCoordinate() != null && query.maxCoordinate() != null) {
            clauses.add("x BETWEEN ? AND ?");
            parameters.add(query.minCoordinate().intX());
            parameters.add(query.maxCoordinate().intX());
            clauses.add("y BETWEEN ? AND ?");
            parameters.add(query.minCoordinate().intY());
            parameters.add(query.maxCoordinate().intY());
            clauses.add("z BETWEEN ? AND ?");
            parameters.add(query.minCoordinate().intZ());
            parameters.add(query.maxCoordinate().intZ());
        }

        // Y coordinate (above)
        if (query.above() != null) {
            clauses.add("y >= ?");
            parameters.add(query.above());
        }

        // Y coordinate (below)
        if (query.below() != null) {
            clauses.add("y <= ?");
            parameters.add(query.below());
        }

        // Timestamps
        if (query.after() != null && query.before() != null) {
            clauses.add("`timestamp` >= fromUnixTimestamp(?)");
            parameters.add(query.after());
            clauses.add("`timestamp` <= fromUnixTimestamp(?)");
            parameters.add(query.before());
        } else if (query.after() != null) {
            clauses.add("`timestamp` > fromUnixTimestamp(?)");
            parameters.add(query.after());
        } else if (query.before() != null) {
            clauses.add("`timestamp` < fromUnixTimestamp(?)");
            parameters.add(query.before());
        }

        return clauses;
    }

    /**
     * Add an inclusive {@code IN} clause for a column and append its bound values.
     *
     * @param clauses The clause list
     * @param parameters The ordered parameter list to populate
     * @param column The column name
     * @param values The values to require
     */
    private void addInclusion(List<String> clauses, List<Object> parameters, String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        clauses.add(column + " IN (" + placeholders(values.size()) + ")");
        parameters.addAll(values);
    }

    /**
     * Add an exclusive {@code NOT IN} clause for a column and append its bound values.
     *
     * <p>Absent values are stored as empty strings, so the empty-string case is OR-ed in to keep
     * rows that have no value for this column (mirroring the normalized {@code IS NULL OR NOT IN}).</p>
     *
     * @param clauses The clause list
     * @param parameters The ordered parameter list to populate
     * @param column The column name
     * @param values The values to reject
     */
    private void addExclusion(List<String> clauses, List<Object> parameters, String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        clauses.add("(" + column + " = '' OR " + column + " NOT IN (" + placeholders(values.size()) + "))");
        parameters.addAll(values);
    }

    /**
     * Build a comma-separated list of bind placeholders.
     *
     * @param count The number of placeholders
     * @return The placeholder string
     */
    private String placeholders(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append("?");
        }

        return builder.toString();
    }

    /**
     * Escape the LIKE metacharacters in a value so it matches as a literal substring.
     *
     * @param value The raw search value
     * @return The value with LIKE metacharacters escaped
     */
    private String escapeLikePattern(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * Append a {@code LIMIT}/{@code OFFSET} clause when the query defines a positive limit.
     *
     * @param query The activity query
     * @param sql The SQL builder
     */
    private void appendLimitOffset(ActivityQuery query, StringBuilder sql) {
        if (query.limit() > 0) {
            sql.append(" LIMIT ").append(query.limit()).append(" OFFSET ").append(query.offset());
        }
    }

    /**
     * Resolve the SQL sort direction for a query.
     *
     * @param query The activity query
     * @return {@code ASC} or {@code DESC}
     */
    private String direction(ActivityQuery query) {
        return query.sort().equals(ActivityQuery.Sort.ASCENDING) ? "ASC" : "DESC";
    }

    /**
     * Bind the ordered parameter list onto a prepared statement.
     *
     * @param statement The prepared statement
     * @param parameters The ordered parameter list
     * @throws SQLException On error
     */
    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
    }

    /**
     * Map a single result-set row to an activity, replicating the normalized adapter's mapping.
     *
     * @param resultSet The result set positioned on a row
     * @param query The original query
     * @return The mapped activity, or null if the action type is unknown or the row cannot be built
     * @throws SQLException On error reading the row
     */
    private AbstractActivity mapRow(ResultSet resultSet, ActivityQuery query) throws SQLException {
        String actionKey = resultSet.getString("action");
        var optionalActionType = actionRegistry.actionType(actionKey);
        if (optionalActionType.isEmpty()) {
            loggingService.warn("Failed to find action type: {0}", actionKey);
            return null;
        }

        var actionType = optionalActionType.get();

        String worldUuidValue = emptyToNull(resultSet.getString("world_uuid"));
        if (worldUuidValue == null) {
            loggingService.warn("Skipping activity with missing world uuid for action: {0}", actionKey);
            return null;
        }

        UUID worldUuid = UUID.fromString(worldUuidValue);
        var world = new Pair<>(worldUuid, resultSet.getString("world"));

        // Location
        Coordinate coordinate = null;
        if (!query.grouped()) {
            coordinate = new Coordinate(resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));
        }

        // Entity type
        String entityType = emptyToNull(resultSet.getString("affected_entity_type"));
        if (entityType != null) {
            entityType = entityType.toUpperCase(Locale.ENGLISH);
        }

        // Material
        String material = emptyToNull(resultSet.getString("affected_material"));
        if (material != null) {
            material = material.toUpperCase(Locale.ENGLISH);
        }

        String itemData = emptyToNull(resultSet.getString("affected_item_data"));

        // Item quantity
        short itemQuantity = resultSet.getShort("affected_item_quantity");
        if (resultSet.wasNull()) {
            itemQuantity = 0;
        }

        // Affected player
        String affectedPlayerName = emptyToNull(resultSet.getString("affected_player"));
        String affectedPlayerUuidValue = emptyToNull(resultSet.getString("affected_player_uuid"));
        UUID affectedPlayerUuid = affectedPlayerUuidValue != null ? UUID.fromString(affectedPlayerUuidValue) : null;

        // Cause
        Cause cause = buildCause(resultSet, query);

        String descriptor = query.lookup() ? emptyToNull(resultSet.getString("descriptor")) : null;
        String metadata = query.lookup() ? emptyToNull(resultSet.getString("metadata")) : null;
        boolean reversed = query.lookup() && resultSet.getInt("reversed") == 1;

        String blockNamespace = emptyToNull(resultSet.getString("affected_block_ns"));
        String blockName = emptyToNull(resultSet.getString("affected_block_name"));
        String translationKey = emptyToNull(resultSet.getString("affected_block_translation_key"));

        long timestamp = query.grouped() ? (long) resultSet.getDouble("avgtime") : resultSet.getLong("ts");

        // Build the action data
        ActionData actionData = new ActionData(
            material,
            itemQuantity,
            itemData,
            blockNamespace,
            blockName,
            null,
            null,
            null,
            null,
            entityType,
            null,
            descriptor,
            metadata,
            (short) 0,
            translationKey,
            null,
            affectedPlayerName,
            affectedPlayerUuid
        );

        if (query.grouped()) {
            int count = resultSet.getInt("groupcount");

            try {
                return new GroupedActivity(
                    actionType.createAction(actionData),
                    world,
                    cause,
                    timestamp,
                    count,
                    reversed
                );
            } catch (Exception e) {
                loggingService.handleException(e);
                return null;
            }
        }

        long activityId = resultSet.getLong("activity_id");

        try {
            return new Activity(
                activityId,
                actionType.createAction(actionData),
                world,
                coordinate,
                cause,
                timestamp,
                reversed
            );
        } catch (Exception e) {
            loggingService.handleException(e);
            return null;
        }
    }

    /**
     * Resolve the cause for a row, replicating the normalized adapter's cause chain.
     *
     * @param resultSet The result set positioned on a row
     * @param query The original query
     * @return The cause, or null if none applies
     * @throws SQLException On error reading the row
     */
    private Cause buildCause(ResultSet resultSet, ActivityQuery query) throws SQLException {
        String namedCause = emptyToNull(resultSet.getString("cause"));
        String causePlayer = emptyToNull(resultSet.getString("cause_player"));
        String causePlayerUuid = emptyToNull(resultSet.getString("cause_player_uuid"));
        String causeEntityTranslationKey = emptyToNull(resultSet.getString("cause_entity_type_translation_key"));
        String causeBlockTranslationKey = emptyToNull(resultSet.getString("cause_block_translation_key"));

        if (query.lookup() && namedCause != null) {
            return new Cause(new StringContainer(namedCause));
        } else if (causePlayerUuid != null) {
            return new Cause(new PlayerContainer(causePlayer, UUID.fromString(causePlayerUuid)));
        } else if (query.lookup() && causeEntityTranslationKey != null) {
            return new Cause(new TranslatableContainer(causeEntityTranslationKey));
        } else if (query.lookup() && causeBlockTranslationKey != null) {
            return new Cause(new TranslatableContainer(causeBlockTranslationKey));
        }

        return null;
    }

    /**
     * Normalize an empty string to null. The flat table stores absent string values as empty strings.
     *
     * @param value The value
     * @return The value, or null if it was null or empty
     */
    private String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     * Get the fully-qualified activities table name.
     *
     * @return The table name
     */
    private String tableName() {
        return prefix + "activities";
    }

    /**
     * Get the fully-qualified airtags table name.
     *
     * @return The airtags table name
     */
    private String airtagsTableName() {
        return prefix + "airtags";
    }
}
