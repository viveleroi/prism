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

package network.darkhelmet.prism.core.storage.adapters.h2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.File;
import java.nio.file.Path;

import network.darkhelmet.prism.api.actions.types.IActionTypeRegistry;
import network.darkhelmet.prism.core.injection.factories.ISqlActivityQueryBuilderFactory;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.core.storage.HikariConfigFactory;
import network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlSchemaUpdater;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.jooq.SQLDialect;

@Singleton
public class H2StorageAdapter extends AbstractSqlStorageAdapter {
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
    public H2StorageAdapter(
            LoggingService loggingService,
            ConfigurationService configurationService,
            IActionTypeRegistry actionRegistry,
            SqlSchemaUpdater schemaUpdater,
            ISqlActivityQueryBuilderFactory queryBuilderFactory,
            CacheService cacheService,
            @Named("serializerVersion") short serializerVersion,
            Path dataPath) {
        super(
            loggingService,
            configurationService,
            actionRegistry,
            schemaUpdater,
            queryBuilderFactory,
            cacheService,
            serializerVersion);

        try {
            File prismH2File = new File(dataPath.toFile(), configurationService.storageConfig().h2().database());

            if (connect(HikariConfigFactory.h2(configurationService.storageConfig(), prismH2File), SQLDialect.H2)) {
                prepareSchema();

                ready = true;
            }
        } catch (Exception e) {
            loggingService.handleException(e);
        }
    }

    @Override
    protected void prepareSchema() throws Exception {
        create.createSchemaIfNotExists(configurationService.storageConfig().h2().database()).execute();

        super.prepareSchema();
    }
}