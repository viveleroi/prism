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

package network.darkhelmet.prism.core.storage.adapters.postgres;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;

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
public class PostgresStorageAdapter extends AbstractSqlStorageAdapter {
    /**
     * Constructor.
     *
     * @param loggingService The logging service
     * @param configurationService The configuration service
     * @param actionRegistry The action type registry
     * @param schemaUpdater The schema updater
     * @param cacheService The cache service
     * @param queryBuilderFactory The query builder factory
     * @param serializerVersion The serializer version
     * @param dataPath The plugin file path
     */
    @Inject
    public PostgresStorageAdapter(
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
            // First, try to use any hikari.properties
            File hikariPropertiesFile = new File(dataPath.toFile(), "hikari.properties");
            if (hikariPropertiesFile.exists()) {
                loggingService.logger().info("Using hikari.properties over storage.conf");

                if (connect(new HikariConfig(hikariPropertiesFile.getPath()), SQLDialect.POSTGRES)) {
                    describeDatabase(true);
                    prepareSchema();
                    prepareCache();

                    ready = true;
                }
            } else {
                loggingService.logger().info("Reading storage.conf. There is no hikari.properties file.");

                if (connect(HikariConfigFactory.postgres(configurationService.storageConfig()), SQLDialect.POSTGRES)) {
                    describeDatabase(false);
                    prepareSchema();
                    prepareCache();
                }

                ready = true;
            }
        } catch (Exception e) {
            loggingService.handleException(e);
        }
    }
}
