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

package network.darkhelmet.prism.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

import java.nio.file.Path;
import java.util.Map;

import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.types.IActionTypeRegistry;
import network.darkhelmet.prism.api.providers.IWorldIdentityProvider;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlActivityQueryBuilder;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlSchemaUpdater;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.loader.storage.StorageType;
import network.darkhelmet.prism.providers.WorldIdentityProvider;

import org.apache.logging.log4j.Logger;

public class PrismModule extends AbstractModule {
    /**
     * The logger.
     */
    private final Logger logger;

    /**
     * The data path.
     */
    private final Path configPath;

    /**
     * The version.
     */
    private final String version;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The serializer version.
     */
    private final short serializerVersion;

    /**
     * Construct the module.
     *
     * @param configPath The config path
     * @param version The version
     * @param logger The logger
     * @param configurationService The configuration service
     */
    public PrismModule(
        Path configPath,
        String version,
        Logger logger,
        ConfigurationService configurationService,
        short serializerVersion) {
        this.logger = logger;
        this.configPath = configPath;
        this.version = version;
        this.configurationService = configurationService;
        this.serializerVersion = serializerVersion;
    }

    @Provides
    @Named("version")
    String getVersion() {
        return version;
    }

    @Provides
    @Named("serializerVersion")
    short serializerVersion() {
        return serializerVersion;
    }

    /**
     * Get the configured storage adapter.
     *
     * @param configurationService The configuration service
     * @param storageMap The storage binding map
     * @return The storage adapter
     */
    @Provides
    public IStorageAdapter getStorageAdapter(
            ConfigurationService configurationService,
            Map<StorageType, Provider<IStorageAdapter>> storageMap) {
        StorageType datasource = configurationService.storageConfig().primaryStorageType();
        return storageMap.get(datasource).get();
    }

    @Override
    public void configure() {
        // Base
        bind(Logger.class).toInstance(this.logger);
        bind(Path.class).toInstance(configPath);

        // Actions
        bind(IActionTypeRegistry.class).to(ActionTypeRegistry.class).in(Singleton.class);

        // Providers
        bind(IWorldIdentityProvider.class).to(WorldIdentityProvider.class).in(Singleton.class);

        // Service - Configuration
        bind(ConfigurationService.class).toInstance(configurationService);

        // Service = Logging
        bind(LoggingService.class).in(Singleton.class);

        // Storage
        bind(SqlActivityQueryBuilder.class);
        bind(SqlSchemaUpdater.class).in(Singleton.class);

        MapBinder<StorageType, IStorageAdapter> storageBinder = MapBinder.newMapBinder(
            binder(), StorageType.class, IStorageAdapter.class);
        storageBinder.addBinding(StorageType.MYSQL).to(AbstractSqlStorageAdapter.class).in(Singleton.class);
    }
}
