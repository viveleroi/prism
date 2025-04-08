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

package network.darkhelmet.prism.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

import io.leangen.geantyref.TypeToken;

import java.nio.file.Path;

import net.kyori.moonshine.Moonshine;
import net.kyori.moonshine.exception.scan.UnscannableMethodException;
import net.kyori.moonshine.strategy.StandardPlaceholderResolverStrategy;
import net.kyori.moonshine.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy;

import network.darkhelmet.prism.PrismBukkit;
import network.darkhelmet.prism.actions.ActionFactory;
import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.types.IActionTypeRegistry;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.providers.IWorldIdentityProvider;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.IRestore;
import network.darkhelmet.prism.api.services.modifications.IRollback;
import network.darkhelmet.prism.api.services.purges.IPurgeQueue;
import network.darkhelmet.prism.api.services.purges.PurgeCycleResult;
import network.darkhelmet.prism.api.services.recording.IRecordingService;
import network.darkhelmet.prism.api.services.wands.IWand;
import network.darkhelmet.prism.api.services.wands.WandMode;
import network.darkhelmet.prism.api.storage.ISqlActivityQueryBuilder;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.core.injection.factories.IH2ActivityQueryBuilderFactory;
import network.darkhelmet.prism.core.injection.factories.IPurgeQueueFactory;
import network.darkhelmet.prism.core.injection.factories.IRestoreFactory;
import network.darkhelmet.prism.core.injection.factories.IRollbackFactory;
import network.darkhelmet.prism.core.injection.factories.ISqlActivityQueryBuilderFactory;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.core.storage.adapters.h2.H2ActivityQueryBuilder;
import network.darkhelmet.prism.core.storage.adapters.h2.H2StorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.h2.IH2ActivityQueryBuilder;
import network.darkhelmet.prism.core.storage.adapters.mariadb.MariaDbStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.mysql.MysqlStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.postgres.PostgresStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlActivityQueryBuilder;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlSchemaUpdater;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.loader.storage.StorageType;
import network.darkhelmet.prism.providers.TaskChainProvider;
import network.darkhelmet.prism.providers.WorldIdentityProvider;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.filters.FilterService;
import network.darkhelmet.prism.services.lookup.LookupService;
import network.darkhelmet.prism.services.messages.MessageRenderer;
import network.darkhelmet.prism.services.messages.MessageSender;
import network.darkhelmet.prism.services.messages.MessageService;
import network.darkhelmet.prism.services.messages.ReceiverResolver;
import network.darkhelmet.prism.services.messages.resolvers.ActivityPlaceholderResolver;
import network.darkhelmet.prism.services.messages.resolvers.IntegerPlaceholderResolver;
import network.darkhelmet.prism.services.messages.resolvers.PaginatedResultsPlaceholderResolver;
import network.darkhelmet.prism.services.messages.resolvers.PurgeCycleResultPlaceholderResolver;
import network.darkhelmet.prism.services.messages.resolvers.StringPlaceholderResolver;
import network.darkhelmet.prism.services.messages.resolvers.WandModePlaceholderResolver;
import network.darkhelmet.prism.services.modifications.ModificationQueueService;
import network.darkhelmet.prism.services.modifications.Restore;
import network.darkhelmet.prism.services.modifications.Rollback;
import network.darkhelmet.prism.services.purge.PurgeQueue;
import network.darkhelmet.prism.services.purge.PurgeService;
import network.darkhelmet.prism.services.recording.RecordingService;
import network.darkhelmet.prism.services.scheduling.SchedulingService;
import network.darkhelmet.prism.services.translation.TranslationService;
import network.darkhelmet.prism.services.wands.InspectionWand;
import network.darkhelmet.prism.services.wands.RestoreWand;
import network.darkhelmet.prism.services.wands.RollbackWand;
import network.darkhelmet.prism.services.wands.WandService;

import org.bukkit.command.CommandSender;

public class PrismModule extends AbstractModule {
    /**
     * The plugin.
     */
    private final PrismBukkit prism;

    /**
     * The logging service.
     */
    private final LoggingService loggingService;

    /**
     * The data path.
     */
    private final Path dataPath;

    /**
     * The version.
     */
    private final String version;

    /**
     * The serializer version.
     */
    private final int serializerVersion;

    /**
     * Construct the module.
     *
     * @param prism Prism
     * @param loggingService The logging service
     */
    public PrismModule(PrismBukkit prism, LoggingService loggingService) {
        this.prism = prism;
        this.loggingService = loggingService;
        this.dataPath = prism.loaderPlugin().getDataFolder().toPath();
        this.version = prism.loaderPlugin().getDescription().getVersion();
        this.serializerVersion = prism.serializerVersion();
    }

    @Provides
    @Named("version")
    String getVersion() {
        return version;
    }

    @Provides
    @Named("serializerVersion")
    int serializerVersion() {
        return serializerVersion;
    }

    /**
     * Get the message service.
     *
     * @param translationService The translation service
     * @param messageRenderer The message renderer
     * @param messageSender The message sender
     * @param activityPlaceholderResolver The activity placeholder resolver
     * @param wandModePlaceholderResolver The wand mode resolver
     * @return The message service
     */
    @Provides
    @Singleton
    @Inject
    public MessageService getMessageService(
            TranslationService translationService,
            MessageRenderer messageRenderer,
            MessageSender messageSender,
            ActivityPlaceholderResolver activityPlaceholderResolver,
            WandModePlaceholderResolver wandModePlaceholderResolver) {
        try {
            return Moonshine.<MessageService, CommandSender>builder(
                    TypeToken.get(MessageService.class))
                .receiverLocatorResolver(new ReceiverResolver(), 0)
                .sourced(translationService)
                .rendered(messageRenderer)
                .sent(messageSender)
                .resolvingWithStrategy(new StandardPlaceholderResolverStrategy<>(
                    new StandardSupertypeThenInterfaceSupertypeStrategy(false)))
                .weightedPlaceholderResolver(Integer.class, new IntegerPlaceholderResolver(), 0)
                .weightedPlaceholderResolver(String.class, new StringPlaceholderResolver(), 0)
                .weightedPlaceholderResolver(PurgeCycleResult.class, new PurgeCycleResultPlaceholderResolver(), 0)
                .weightedPlaceholderResolver(IActivity.class, activityPlaceholderResolver, 0)
                .weightedPlaceholderResolver(WandMode.class, wandModePlaceholderResolver, 0)
                .weightedPlaceholderResolver(new TypeToken<>(){}, new PaginatedResultsPlaceholderResolver(), 0)
                .create(this.getClass().getClassLoader());
        } catch (UnscannableMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void configure() {
        // Base
        bind(Path.class).toInstance(dataPath);

        // Taskchain
        bind(TaskChainProvider.class).toInstance(new TaskChainProvider(prism.loader()));

        // Actions
        bind(ActionFactory.class).in(Singleton.class);
        bind(IActionTypeRegistry.class).to(ActionTypeRegistry.class).in(Singleton.class);

        // Providers
        bind(IWorldIdentityProvider.class).to(WorldIdentityProvider.class).in(Singleton.class);

        // Service - Cache
        bind(CacheService.class).in(Singleton.class);

        // Service - Configuration
        bind(ConfigurationService.class).toInstance(prism.loader().configurationService());

        // Service - Expectations
        bind(ExpectationService.class).in(Singleton.class);

        // Service - Filters
        bind(FilterService.class).in(Singleton.class);

        // Service = Logging
        bind(LoggingService.class).toInstance(loggingService);

        // Service - Lookup
        bind(LookupService.class).in(Singleton.class);

        // Service - Modifications
        bind(IModificationQueueService.class).to(ModificationQueueService.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
            .implement(IRestore.class, Restore.class)
            .build(IRestoreFactory.class));

        install(new FactoryModuleBuilder()
            .implement(IRollback.class, Rollback.class)
            .build(IRollbackFactory.class));

        // Service - Purge
        bind(PurgeService.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
            .implement(IPurgeQueue.class, PurgeQueue.class)
            .build(IPurgeQueueFactory.class));

        // Service - Recording
        bind(IRecordingService.class).to(RecordingService.class).in(Singleton.class);

        // Service - Scheduling
        bind(SchedulingService.class).in(Singleton.class);

        // Service - Messages
        bind(MessageRenderer.class).in(Singleton.class);
        bind(MessageSender.class).in(Singleton.class);
        bind(ActivityPlaceholderResolver.class).in(Singleton.class);

        // Service - Translation
        bind(TranslationService.class).in(Singleton.class);

        // Service - Wands
        bind(WandService.class).in(Singleton.class);
        MapBinder<WandMode, IWand> wandBinder = MapBinder.newMapBinder(binder(), WandMode.class, IWand.class);
        wandBinder.addBinding(WandMode.INSPECT).to(InspectionWand.class);
        wandBinder.addBinding(WandMode.ROLLBACK).to(RollbackWand.class);
        wandBinder.addBinding(WandMode.RESTORE).to(RestoreWand.class);

        // Storage
        bind(SqlSchemaUpdater.class).in(Singleton.class);

        StorageType storageType = prism.loader().configurationService().storageConfig().primaryStorageType();

        // Install the correct query builder
        if (storageType.equals(StorageType.H2)) {
            install(new FactoryModuleBuilder()
                .implement(IH2ActivityQueryBuilder.class, H2ActivityQueryBuilder.class)
                .build(IH2ActivityQueryBuilderFactory.class));
        } else {
            install(new FactoryModuleBuilder()
                .implement(ISqlActivityQueryBuilder.class, SqlActivityQueryBuilder.class)
                .build(ISqlActivityQueryBuilderFactory.class));
        }

        // Bind the correct storage adapter
        switch (storageType) {
            case H2 -> bind(IStorageAdapter.class).to(H2StorageAdapter.class).in(Singleton.class);
            case MARIADB -> bind(IStorageAdapter.class).to(MariaDbStorageAdapter.class).in(Singleton.class);
            case MYSQL -> bind(IStorageAdapter.class).to(MysqlStorageAdapter.class).in(Singleton.class);
            case POSTGRES -> bind(IStorageAdapter.class).to(PostgresStorageAdapter.class).in(Singleton.class);
            default -> {
                // ignored
            }
        }
    }
}
