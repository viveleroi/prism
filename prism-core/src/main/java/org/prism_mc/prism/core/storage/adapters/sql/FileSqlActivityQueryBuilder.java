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

package org.prism_mc.prism.core.storage.adapters.sql;

import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.AFFECTED_PLAYERS;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.CAUSE_BLOCKS;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.CAUSE_ENTITY_TYPES;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIONS;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_BLOCKS;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_CAUSES;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ENTITY_TYPES;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ITEMS;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_PLAYERS;
import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_WORLDS;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.types.UInteger;
import org.prism_mc.prism.api.activities.ActivityQuery;
import org.prism_mc.prism.core.storage.dbo.records.PrismActivitiesRecord;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;

public class FileSqlActivityQueryBuilder extends SqlActivityQueryBuilder {

    /**
     * Construct a new query builder.
     *
     * @param configurationService The configuration service
     * @param create The DSL context
     */
    @Inject
    public FileSqlActivityQueryBuilder(ConfigurationService configurationService, @Assisted DSLContext create) {
        super(configurationService, create);
    }

    /**
     * Delete records from the activities table.
     *
     * <p>Neither sqlite nor h2 support the DELETE USING clause so we use the WHERE EXISTS
     * with subqueries because those are more performant than WHERE IN subqueries.</p>
     *
     * @param query The query
     * @param cycleMinPrimaryKey The min primary key
     * @param cycleMaxPrimaryKey The max primary key
     * @return The number of rows deleted
     */
    @Override
    public int deleteActivities(ActivityQuery query, int cycleMinPrimaryKey, int cycleMaxPrimaryKey) {
        DeleteQuery<PrismActivitiesRecord> queryBuilder = dslContext.deleteQuery(PRISM_ACTIVITIES);

        // Action Types + Keys (combined)
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.ACTION_ID,
            PRISM_ACTIONS.ACTION_ID,
            PRISM_ACTIONS.ACTION,
            PRISM_ACTIONS,
            query.allActionTypeKeys(),
            query.actionTypeKeysExcluded()
        );

        // Materials
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.AFFECTED_ITEM_ID,
            PRISM_ITEMS.ITEM_ID,
            PRISM_ITEMS.MATERIAL,
            PRISM_ITEMS,
            query.affectedMaterials(),
            query.affectedMaterialsExcluded()
        );

        // Affected Blocks
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.AFFECTED_BLOCK_ID,
            PRISM_BLOCKS.BLOCK_ID,
            PRISM_BLOCKS.NAME,
            PRISM_BLOCKS,
            query.affectedBlocks(),
            query.affectedBlocksExcluded()
        );

        // Cause Blocks
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.CAUSE_BLOCK_ID,
            CAUSE_BLOCKS.BLOCK_ID,
            CAUSE_BLOCKS.NAME,
            CAUSE_BLOCKS,
            query.causeBlocks(),
            query.causeBlocksExcluded()
        );

        // Affected Entity Types
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.AFFECTED_ENTITY_TYPE_ID,
            PRISM_ENTITY_TYPES.ENTITY_TYPE_ID,
            PRISM_ENTITY_TYPES.ENTITY_TYPE,
            PRISM_ENTITY_TYPES,
            query.affectedEntityTypes(),
            query.affectedEntityTypesExcluded()
        );

        // Cause Entity Types
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.CAUSE_ENTITY_TYPE_ID,
            CAUSE_ENTITY_TYPES.ENTITY_TYPE_ID,
            CAUSE_ENTITY_TYPES.ENTITY_TYPE,
            CAUSE_ENTITY_TYPES,
            query.causeEntityTypes(),
            query.causeEntityTypesExcluded()
        );

        // Affected Players
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.AFFECTED_PLAYER_ID,
            AFFECTED_PLAYERS.PLAYER_ID,
            AFFECTED_PLAYERS.PLAYER,
            AFFECTED_PLAYERS,
            query.affectedPlayerNames(),
            query.affectedPlayerNamesExcluded()
        );

        // Cause Players
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.CAUSE_PLAYER_ID,
            PRISM_PLAYERS.PLAYER_ID,
            PRISM_PLAYERS.PLAYER,
            PRISM_PLAYERS,
            query.causePlayerNames(),
            query.causePlayerNamesExcluded()
        );

        // Named Cause
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.CAUSE_ID,
            PRISM_CAUSES.CAUSE_ID,
            PRISM_CAUSES.CAUSE,
            PRISM_CAUSES,
            query.namedCause() != null ? List.of(query.namedCause()) : List.of(),
            query.namedCauseExcluded() != null ? List.of(query.namedCauseExcluded()) : List.of()
        );

        // Locations
        if (query.coordinate() != null) {
            queryBuilder.addConditions(
                PRISM_ACTIVITIES.X.equal(query.coordinate().intX())
                    .and(PRISM_ACTIVITIES.Y.equal(query.coordinate().intY()))
                    .and(PRISM_ACTIVITIES.Z.equal(query.coordinate().intZ()))
            );
        } else if (query.minCoordinate() != null && query.maxCoordinate() != null) {
            queryBuilder.addConditions(
                PRISM_ACTIVITIES.X.between(query.minCoordinate().intX(), query.maxCoordinate().intX())
                    .and(PRISM_ACTIVITIES.Y.between(query.minCoordinate().intY(), query.maxCoordinate().intY()))
                    .and(PRISM_ACTIVITIES.Z.between(query.minCoordinate().intZ(), query.maxCoordinate().intZ()))
            );
        }

        // Reversed
        if (query.reversed() != null) {
            queryBuilder.addConditions(PRISM_ACTIVITIES.REVERSED.eq(query.reversed()));
        }

        // Timestamps
        if (query.after() != null && query.before() != null) {
            queryBuilder.addConditions(
                PRISM_ACTIVITIES.TIMESTAMP.between(UInteger.valueOf(query.after()), UInteger.valueOf(query.before()))
            );
        } else if (query.after() != null) {
            queryBuilder.addConditions(PRISM_ACTIVITIES.TIMESTAMP.greaterThan(UInteger.valueOf(query.after())));
        } else if (query.before() != null) {
            queryBuilder.addConditions(PRISM_ACTIVITIES.TIMESTAMP.lessThan(UInteger.valueOf(query.before())));
        }

        // World
        addSubqueryLookupConditions(
            queryBuilder,
            PRISM_ACTIVITIES.WORLD_ID,
            PRISM_WORLDS.WORLD_ID,
            PRISM_WORLDS.WORLD_UUID,
            PRISM_WORLDS,
            query.worldUuid() != null ? List.of(query.worldUuid().toString()) : List.of(),
            query.worldUuidExcluded() != null ? List.of(query.worldUuidExcluded().toString()) : List.of()
        );

        // Limit
        queryBuilder.addConditions(
            PRISM_ACTIVITIES.ACTIVITY_ID.between(
                UInteger.valueOf(cycleMinPrimaryKey),
                UInteger.valueOf(cycleMaxPrimaryKey)
            )
        );

        return queryBuilder.execute();
    }
}
