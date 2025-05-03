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

package org.prism_mc.prism.loader.services.configuration;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.prism_mc.prism.loader.services.configuration.serializers.LocaleSerializerConfigurate;
import org.prism_mc.prism.loader.services.configuration.storage.StorageConfiguration;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

public class ConfigurationService {

    /**
     * The plugin data path.
     */
    private final Path dataPath;

    /**
     * The logger.
     */
    private final Logger logger;

    /**
     * The primary plugin configuration.
     */
    private PrismConfiguration prismConfiguration = new PrismConfiguration();

    /**
     * The storage configuration.
     */
    private StorageConfiguration storageConfiguration = new StorageConfiguration();

    /**
     * Construct the configuration service.
     *
     * @param dataPath The plugin datapath
     */
    public ConfigurationService(Path dataPath, Logger logger) {
        this.dataPath = dataPath;
        this.logger = logger;

        loadConfigurations();
    }

    /**
     * Get the prism configuration.
     *
     * @return The prism configuration
     */
    public PrismConfiguration prismConfig() {
        return prismConfiguration;
    }

    /**
     * Get the storage configuration.
     *
     * @return The storage configuration
     */
    public StorageConfiguration storageConfig() {
        return storageConfiguration;
    }

    /**
     * Load the configurations.
     */
    public void loadConfigurations() {
        // Load the main config
        File prismConfigFile = new File(dataPath.toFile(), "prism.conf");
        prismConfiguration = getOrWriteConfiguration(
            PrismConfiguration.class,
            prismConfigFile,
            new PrismConfiguration()
        );

        File storageConfigFile = new File(dataPath.toFile(), "storage.conf");
        storageConfiguration = getOrWriteConfiguration(
            StorageConfiguration.class,
            storageConfigFile,
            new StorageConfiguration()
        );
    }

    /**
     * Build a hocon configuration loader with locale support.
     *
     * @param file The config file
     * @return The config loader
     */
    public ConfigurationLoader<?> configurationLoader(final Path file) {
        HoconConfigurationLoader.Builder builder = HoconConfigurationLoader.builder();
        builder.prettyPrinting(true);
        builder.defaultOptions(opts ->
            opts
                .shouldCopyDefaults(true)
                .implicitInitialization(false)
                .serializers(serializerBuilder ->
                    serializerBuilder.register(Locale.class, new LocaleSerializerConfigurate())
                )
        );
        builder.path(file);
        return builder.build();
    }

    /**
     * Get or create a configuration file.
     *
     * @param clz The configuration class
     * @param file The file path we'll read/write to
     * @param defaultConfig The default config object to write
     * @param <T> The configuration class type
     * @return The configuration class instance
     */
    public <T> T getOrWriteConfiguration(Class<T> clz, File file, T defaultConfig) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        final ConfigurationLoader<?> loader = configurationLoader(file.toPath());

        try {
            final ConfigurationNode root = loader.load();

            T config = root.get(clz);

            // If config is not present, default
            if (config == null) {
                config = defaultConfig;
            }

            root.set(clz, config);
            loader.save(root);

            return config;
        } catch (final ConfigurateException e) {
            if (e.getCause() != null) {
                logger.log(Level.SEVERE, "An exception occurred", e);
            }
        }

        return null;
    }
}
