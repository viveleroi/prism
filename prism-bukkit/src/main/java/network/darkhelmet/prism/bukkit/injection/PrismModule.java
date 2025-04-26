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

package network.darkhelmet.prism.bukkit.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

import io.leangen.geantyref.TypeToken;

import java.nio.file.Path;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.moonshine.Moonshine;
import net.kyori.moonshine.exception.scan.UnscannableMethodException;
import net.kyori.moonshine.strategy.StandardPlaceholderResolverStrategy;
import net.kyori.moonshine.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy;

import network.darkhelmet.prism.api.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.activities.AbstractActivity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueResult;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueService;
import network.darkhelmet.prism.api.services.modifications.Restore;
import network.darkhelmet.prism.api.services.modifications.Rollback;
import network.darkhelmet.prism.api.services.purges.PurgeCycleResult;
import network.darkhelmet.prism.api.services.purges.PurgeQueue;
import network.darkhelmet.prism.api.services.recording.RecordingService;
import network.darkhelmet.prism.api.services.wands.Wand;
import network.darkhelmet.prism.api.services.wands.WandMode;
import network.darkhelmet.prism.api.storage.StorageAdapter;
import network.darkhelmet.prism.bukkit.PrismBukkit;
import network.darkhelmet.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import network.darkhelmet.prism.bukkit.integrations.worldedit.WorldEditIntegration;
import network.darkhelmet.prism.bukkit.providers.TaskChainProvider;
import network.darkhelmet.prism.bukkit.services.alerts.BlockBreakAlertData;
import network.darkhelmet.prism.bukkit.services.alerts.BukkitAlertService;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.filters.BukkitFilterService;
import network.darkhelmet.prism.bukkit.services.lookup.LookupService;
import network.darkhelmet.prism.bukkit.services.messages.MessageRenderer;
import network.darkhelmet.prism.bukkit.services.messages.MessageSender;
import network.darkhelmet.prism.bukkit.services.messages.MessageService;
import network.darkhelmet.prism.bukkit.services.messages.ReceiverResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.ActivityPlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.BlockBreakAlertDataPlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.IntegerPlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.LongPlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.ModificationQueueResultPlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.PaginatedResultsPlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.PurgeCycleResultPlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.StringPlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.messages.resolvers.WandModePlaceholderResolver;
import network.darkhelmet.prism.bukkit.services.modifications.BukkitModificationQueueService;
import network.darkhelmet.prism.bukkit.services.modifications.BukkitRestore;
import network.darkhelmet.prism.bukkit.services.modifications.BukkitRollback;
import network.darkhelmet.prism.bukkit.services.nbt.NbtService;
import network.darkhelmet.prism.bukkit.services.purge.BukkitPurgeQueue;
import network.darkhelmet.prism.bukkit.services.purge.PurgeService;
import network.darkhelmet.prism.bukkit.services.recording.BukkitRecordingService;
import network.darkhelmet.prism.bukkit.services.scheduling.SchedulingService;
import network.darkhelmet.prism.bukkit.services.translation.BukkitTranslationService;
import network.darkhelmet.prism.bukkit.services.wands.InspectionWand;
import network.darkhelmet.prism.bukkit.services.wands.RestoreWand;
import network.darkhelmet.prism.bukkit.services.wands.RollbackWand;
import network.darkhelmet.prism.bukkit.services.wands.WandService;
import network.darkhelmet.prism.core.injection.factories.FileSqlActivityQueryBuilderFactory;
import network.darkhelmet.prism.core.injection.factories.PurgeQueueFactory;
import network.darkhelmet.prism.core.injection.factories.RestoreFactory;
import network.darkhelmet.prism.core.injection.factories.RollbackFactory;
import network.darkhelmet.prism.core.injection.factories.SqlActivityQueryBuilderFactory;
import network.darkhelmet.prism.core.services.cache.CacheService;
import network.darkhelmet.prism.core.storage.adapters.h2.H2StorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.mariadb.MariaDbStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.mysql.MysqlStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.postgres.PostgresStorageAdapter;
import network.darkhelmet.prism.core.storage.adapters.sql.FileSqlActivityQueryBuilder;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlActivityQueryBuilder;
import network.darkhelmet.prism.core.storage.adapters.sql.SqlSchemaUpdater;
import network.darkhelmet.prism.core.storage.adapters.sqlite.SqliteStorageAdapter;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;
import network.darkhelmet.prism.loader.storage.StorageType;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

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
    private final short serializerVersion;

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
    short serializerVersion() {
        return serializerVersion;
    }

    /**
     * Get the bukkit audiences.
     *
     * @return The bukkit audiences
     */
    @Provides
    @Singleton
    public BukkitAudiences getAudience() {
        if (prism.loaderPlugin().isEnabled()) {
            return BukkitAudiences.create(prism.loaderPlugin());
        }

        return null;
    }

    /**
     * Get the world edit integration.
     *
     * @return The world edit integration
     */
    @Provides
    @Singleton
    public WorldEditIntegration getWorldEditIntegeration() {
        final Plugin worldEdit = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit != null) {
            return new WorldEditIntegration(loggingService, worldEdit);
        }

        return null;
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
            BukkitTranslationService translationService,
            MessageRenderer messageRenderer,
            MessageSender messageSender,
            ActivityPlaceholderResolver activityPlaceholderResolver,
            ModificationQueueResultPlaceholderResolver modificationQueueResultPlaceholderResolver,
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
                .weightedPlaceholderResolver(Long.class, new LongPlaceholderResolver(), 0)
                .weightedPlaceholderResolver(String.class, new StringPlaceholderResolver(), 0)
                .weightedPlaceholderResolver(PurgeCycleResult.class, new PurgeCycleResultPlaceholderResolver(), 0)
                .weightedPlaceholderResolver(AbstractActivity.class, activityPlaceholderResolver, 0)
                .weightedPlaceholderResolver(WandMode.class, wandModePlaceholderResolver, 0)
                .weightedPlaceholderResolver(
                    ModificationQueueResult.class, modificationQueueResultPlaceholderResolver, 0)
                .weightedPlaceholderResolver(new TypeToken<>(){}, new PaginatedResultsPlaceholderResolver(), 0)
                .weightedPlaceholderResolver(
                    BlockBreakAlertData.class, new BlockBreakAlertDataPlaceholderResolver(), 0)
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
        bind(ActionTypeRegistry.class).to(BukkitActionTypeRegistry.class).in(Singleton.class);

        // Service - Alerts
        bind(BukkitAlertService.class).in(Singleton.class);

        // Service - Cache
        bind(CacheService.class).in(Singleton.class);

        // Service - Configuration
        bind(ConfigurationService.class).toInstance(prism.loader().configurationService());

        // Service - Expectations
        bind(ExpectationService.class).in(Singleton.class);

        // Service - Filters
        bind(BukkitFilterService.class).in(Singleton.class);

        // Service = Logging
        bind(LoggingService.class).toInstance(loggingService);

        // Service - Lookup
        bind(LookupService.class).in(Singleton.class);

        // Service - Modifications
        bind(ModificationQueueService.class).to(BukkitModificationQueueService.class).in(Singleton.class);

        // Service - Nbt
        bind(NbtService.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
            .implement(Restore.class, BukkitRestore.class)
            .build(RestoreFactory.class));

        install(new FactoryModuleBuilder()
            .implement(Rollback.class, BukkitRollback.class)
            .build(RollbackFactory.class));

        // Service - Purge
        bind(PurgeService.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
            .implement(PurgeQueue.class, BukkitPurgeQueue.class)
            .build(PurgeQueueFactory.class));

        // Service - Recording
        bind(RecordingService.class).to(BukkitRecordingService.class).in(Singleton.class);

        // Service - Scheduling
        bind(SchedulingService.class).in(Singleton.class);

        // Service - Messages
        bind(MessageRenderer.class).in(Singleton.class);
        bind(MessageSender.class).in(Singleton.class);
        bind(ActivityPlaceholderResolver.class).in(Singleton.class);

        // Service - Translation
        bind(BukkitTranslationService.class).in(Singleton.class);

        // Service - Wands
        bind(WandService.class).in(Singleton.class);
        MapBinder<WandMode, Wand> wandBinder = MapBinder.newMapBinder(binder(), WandMode.class, Wand.class);
        wandBinder.addBinding(WandMode.INSPECT).to(InspectionWand.class);
        wandBinder.addBinding(WandMode.ROLLBACK).to(RollbackWand.class);
        wandBinder.addBinding(WandMode.RESTORE).to(RestoreWand.class);

        // Storage
        bind(SqlSchemaUpdater.class).in(Singleton.class);

        StorageType storageType = prism.loader().configurationService().storageConfig().primaryStorageType();

        // Install the correct query builder
        if (storageType.equals(StorageType.SQLITE) || storageType.equals(StorageType.H2)) {
            install(new FactoryModuleBuilder()
                .implement(SqlActivityQueryBuilder.class, FileSqlActivityQueryBuilder.class)
                .build(FileSqlActivityQueryBuilderFactory.class));
        } else {
            install(new FactoryModuleBuilder()
                .implement(SqlActivityQueryBuilder.class, SqlActivityQueryBuilder.class)
                .build(SqlActivityQueryBuilderFactory.class));
        }

        // Bind the correct storage adapter
        switch (storageType) {
            case H2 -> bind(StorageAdapter.class).to(H2StorageAdapter.class).in(Singleton.class);
            case MARIADB -> bind(StorageAdapter.class).to(MariaDbStorageAdapter.class).in(Singleton.class);
            case MYSQL -> bind(StorageAdapter.class).to(MysqlStorageAdapter.class).in(Singleton.class);
            case POSTGRES -> bind(StorageAdapter.class).to(PostgresStorageAdapter.class).in(Singleton.class);
            case SQLITE -> bind(StorageAdapter.class).to(SqliteStorageAdapter.class).in(Singleton.class);
            default -> {
                // ignored
            }
        }
    }
}
