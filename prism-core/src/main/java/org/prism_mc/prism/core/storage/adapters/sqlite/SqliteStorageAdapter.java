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

package org.prism_mc.prism.core.storage.adapters.sqlite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.File;
import java.nio.file.Path;

import org.prism_mc.prism.api.actions.types.ActionTypeRegistry;
import org.prism_mc.prism.core.injection.factories.FileSqlActivityQueryBuilderFactory;
import org.prism_mc.prism.core.services.cache.CacheService;
import org.prism_mc.prism.core.storage.HikariConfigFactory;
import org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter;
import org.prism_mc.prism.core.storage.adapters.sql.SqlSchemaUpdater;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;
import org.prism_mc.prism.loader.services.logging.LoggingService;

import org.jooq.SQLDialect;

@Singleton
public class SqliteStorageAdapter extends AbstractSqlStorageAdapter {
    /**
     * Constructor.
     *
     * @param loggingService The logging service
     * @param configurationService The configuration service
     * @param actionRegistry The action type registry
     * @param schemaUpdater The schema updater
     * @param queryBuilderFactory The query builder factory
     * @param serializerVersion The serializer version
     */
    @Inject
    public SqliteStorageAdapter(
            LoggingService loggingService,
            ConfigurationService configurationService,
            ActionTypeRegistry actionRegistry,
            SqlSchemaUpdater schemaUpdater,
            FileSqlActivityQueryBuilderFactory queryBuilderFactory,
            CacheService cacheService,
            @Named("serializerVersion") short serializerVersion,
            Path dataPath) {
        super(
                loggingService,
                configurationService,
                actionRegistry,
                schemaUpdater,
                null,
                cacheService,
                serializerVersion);

        try {
            File prismSqliteFile = new File(dataPath.toFile(),
                configurationService.storageConfig().sqlite().database() + ".db");

            if (connect(HikariConfigFactory.sqlite(
                    configurationService.storageConfig(), prismSqliteFile), SQLDialect.SQLITE)) {
                this.queryBuilder = queryBuilderFactory.create(create);

                prepareSchema();

                ready = true;
            }
        } catch (Exception e) {
            loggingService.handleException(e);
        }
    }
}