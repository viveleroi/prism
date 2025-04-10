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

package network.darkhelmet.prism;

import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.dependencies.loader.JarInJarClassLoader;
import network.darkhelmet.prism.loader.services.dependencies.loader.PluginLoader;
import network.darkhelmet.prism.loader.services.dependencies.loader.PrismBootstrap;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

import org.apache.logging.log4j.LogManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PrismBukkitPluginLoader extends JavaPlugin implements PluginLoader {
    /**
     * The file name of the "JarInJar".
     */
    private static final String JAR_NAME = "prism-bukkit.jarinjar";

    /**
     * The qualified name of the bootstrapper.
     */
    private static final String BOOTSTRAP_CLASS = "network.darkhelmet.prism.PrismBukkitBootstrap";

    /**
     * The configuration service.
     */
    private ConfigurationService configurationService;

    /**
     * The logging service.
     */
    private LoggingService loggingService;

    /**
     * The prism bootstrapper.
     */
    private PrismBootstrap prismBootstrap;

    @Override
    public ConfigurationService configurationService() {
        return configurationService;
    }

    @Override
    public LoggingService loggingService() {
        return loggingService;
    }

    @Override
    public void onEnable() {
        // Instantiate the loader
        JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);

        // Instantiate the bootstrapper (which then instantiates the actual Prism plugin)
        this.prismBootstrap = loader.instantiatePlugin(BOOTSTRAP_CLASS, PluginLoader.class, this);

        // Initialize the config service and load config files
        configurationService = new ConfigurationService(getDataFolder().toPath());

        // Initialize the logger and logging service
        loggingService = new LoggingService(configurationService, LogManager.getLogger("prism"));

        // Call onEnable in the bootstrapper
        this.prismBootstrap.onEnable();
    }

    @Override
    public void onDisable() {
        this.prismBootstrap.onDisable();
    }
}