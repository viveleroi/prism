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

package network.darkhelmet.prism;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.nio.file.Path;

import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.core.services.configuration.ConfigurationService;
import network.darkhelmet.prism.core.utils.VersionUtils;
import network.darkhelmet.prism.injection.PrismModule;
import network.darkhelmet.prism.listeners.ChangeBlockListener;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("prism")
public class PrismSponge {
    /**
     * The plugin container.
     */
    private final PluginContainer container;

    /**
     * The logger.
     */
    private final Logger logger;

    /**
     * The config path.
     */
    private final Path configPath;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The injector.
     */
    private final Injector injector;

    /**
     * The storage adapter.
     */
    private IStorageAdapter storageAdapter;

    /**
     * Sets a numeric version we can use to handle differences between serialization formats.
     */
    protected short serializerVersion;

    /**
     * Constructor.
     *
     * @param container The plugin container
     * @param logger The logger
     */
    @Inject
    PrismSponge(final PluginContainer container, final Logger logger, @ConfigDir(sharedRoot = false) Path configPath) {
        this.container = container;
        this.logger = logger;
        this.configPath = configPath;
        this.configurationService = new ConfigurationService(configPath);

        String versionMsg = String.format("Java version: %s", System.getProperty("java.version"));
        logger.info(versionMsg);

        this.injector = Guice.createInjector(new PrismModule(
            configPath, container.metadata().version().toString(), logger, configurationService, (short) 0));

        // Choose and initialize the datasource
        storageAdapter = injector.getInstance(IStorageAdapter.class);
        if (!storageAdapter.ready()) {
            logger.warn("Prism failed to connect to the database and will be unusable!");
        }
    }

    /**
     * On plugin construction.
     *
     * @param event The event
     */
    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        logger.info("Initializing {} {} by viveleroi",
            container.metadata().name().get(), container.metadata().version());

        // Register event listeners
        Sponge.eventManager().registerListeners(container, injector.getInstance(ChangeBlockListener.class));

        Short serializerVer = VersionUtils.minecraftVersion(Sponge.platform().minecraftVersion().name());
        serializerVersion = serializerVer != null ? serializerVer : -1;
        logger.info("Serializer version: {}", serializerVersion);
    }
}