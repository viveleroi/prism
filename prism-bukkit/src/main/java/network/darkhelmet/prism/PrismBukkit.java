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

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.argument.named.Argument;
import dev.triumphteam.cmd.core.argument.named.ArgumentKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.IPrism;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.actions.types.IActionTypeRegistry;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.commands.AboutCommand;
import network.darkhelmet.prism.commands.LookupCommand;
import network.darkhelmet.prism.commands.NearCommand;
import network.darkhelmet.prism.commands.PageCommand;
import network.darkhelmet.prism.commands.PreviewCommand;
import network.darkhelmet.prism.commands.ReloadCommand;
import network.darkhelmet.prism.commands.RestoreCommand;
import network.darkhelmet.prism.commands.RollbackCommand;
import network.darkhelmet.prism.commands.WandCommand;
import network.darkhelmet.prism.core.services.configuration.ConfigurationService;
import network.darkhelmet.prism.core.utils.VersionUtils;
import network.darkhelmet.prism.injection.PrismModule;
import network.darkhelmet.prism.listeners.BlockBreakListener;
import network.darkhelmet.prism.listeners.BlockExplodeListener;
import network.darkhelmet.prism.listeners.BlockPlaceListener;
import network.darkhelmet.prism.listeners.EntityDeathListener;
import network.darkhelmet.prism.listeners.EntityExplodeListener;
import network.darkhelmet.prism.listeners.EntitySpawnListener;
import network.darkhelmet.prism.listeners.HangingBreakListener;
import network.darkhelmet.prism.listeners.PlayerDropItemListener;
import network.darkhelmet.prism.listeners.PlayerInteractListener;
import network.darkhelmet.prism.listeners.PlayerJoinListener;
import network.darkhelmet.prism.listeners.PlayerQuitListener;
import network.darkhelmet.prism.listeners.VehicleCreateListener;
import network.darkhelmet.prism.listeners.VehicleEnterListener;
import network.darkhelmet.prism.listeners.VehicleExitListener;
import network.darkhelmet.prism.services.recording.RecordingService;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrismBukkit extends JavaPlugin implements IPrism {
    /**
     * Cache static instance.
     */
    private static PrismBukkit instance;

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger("Prism");

    /**
     * The injector.
     */
    @Getter
    private Injector injector;

    /**
     * Sets a numeric version we can use to handle differences between serialization formats.
     */
    @Getter
    protected short serializerVersion;

    /**
     * The task chain factory.
     */
    private static TaskChainFactory taskChainFactory;

    /**
     * The configuration service.
     */
    private ConfigurationService configurationService;

    /**
     * The storage adapter.
     */
    @Getter
    private IStorageAdapter storageAdapter;

    /**
     * The action type registry.
     */
    @Getter
    private IActionTypeRegistry actionTypeRegistry;

    /**
     * Get this instance.
     *
     * @return The plugin instance
     */
    public static PrismBukkit getInstance() {
        return instance;
    }

    /**
     * Constructor.
     */
    public PrismBukkit() {
        instance = this;
    }

    @Override
    public void onLoad() {
        Short serializerVer = VersionUtils.minecraftVersion(Bukkit.getVersion());
        serializerVersion = serializerVer != null ? serializerVer : -1;
        logger.info("Serializer version: {}", serializerVersion);

        this.injector = Guice.createInjector(new PrismModule(this, logger));

        // Load the configuration service (and files)
        configurationService = injector.getInstance(ConfigurationService.class);

        // Choose and initialize the datasource
        storageAdapter = injector.getInstance(IStorageAdapter.class);
        if (!storageAdapter.ready()) {
            disable();
        }

        actionTypeRegistry = injector.getInstance(IActionTypeRegistry.class);
    }

    /**
     * On enable.
     */
    @Override
    public void onEnable() {
        String pluginName = this.getDescription().getName();
        String pluginVersion = this.getDescription().getVersion();
        logger.info("Initializing {} {} by viveleroi", pluginName, pluginVersion);

        if (isEnabled()) {
            // Initialize some classes
            injector.getInstance(RecordingService.class);
            taskChainFactory = BukkitTaskChainFactory.create(this);

            // Register listeners
            getServer().getPluginManager().registerEvents(injector.getInstance(BlockBreakListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(BlockExplodeListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(BlockPlaceListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(EntityDeathListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(EntityExplodeListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(EntitySpawnListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(HangingBreakListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(PlayerDropItemListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(PlayerInteractListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(PlayerJoinListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(PlayerQuitListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(VehicleCreateListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(VehicleEnterListener.class), this);
            getServer().getPluginManager().registerEvents(injector.getInstance(VehicleExitListener.class), this);

            // Register commands
            BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);

            // Register action types auto-suggest
            commandManager.registerSuggestion(SuggestionKey.of("actions"), (sender, context) -> {
                List<String> actionFamilies = new ArrayList<>();
                for (IActionType actionType : injector.getInstance(ActionTypeRegistry.class).actionTypes()) {
                    actionFamilies.add(actionType.familyKey());
                }

                return actionFamilies;
            });

            // Register online player auto-suggest
            commandManager.registerSuggestion(SuggestionKey.of("players"), (sender, context) -> {
                List<String> players = new ArrayList<>();
                for (Player player : getServer().getOnlinePlayers()) {
                    players.add(player.getName());
                }

                return players;
            });

            // Register world auto-suggest
            commandManager.registerSuggestion(SuggestionKey.of("worlds"), (sender, context) -> {
                List<String> worlds = new ArrayList<>();
                for (World world : getServer().getWorlds()) {
                    worlds.add(world.getName());
                }

                return worlds;
            });

            // Register "in" parameter
            commandManager.registerSuggestion(SuggestionKey.of("ins"), (sender, context) ->
                Arrays.asList("chunk", "world"));

            commandManager.registerNamedArguments(
                ArgumentKey.of("params"),
                Argument.forInt().name("r").build(),
                Argument.forString().name("in").suggestion(SuggestionKey.of("ins")).build(),
                Argument.forString().name("since").build(),
                Argument.forString().name("before").build(),
                Argument.forString().name("cause").build(),
                Argument.forString().name("world").suggestion(SuggestionKey.of("worlds")).build(),
                Argument.listOf(String.class).name("a").suggestion(SuggestionKey.of("actions")).build(),
                Argument.listOf(Material.class).name("m").build(),
                Argument.listOf(EntityType.class).name("e").build(),
                Argument.listOf(String.class).name("p").suggestion(SuggestionKey.of("players")).build()
            );

            commandManager.registerCommand(injector.getInstance(AboutCommand.class));
            commandManager.registerCommand(injector.getInstance(LookupCommand.class));
            commandManager.registerCommand(injector.getInstance(NearCommand.class));
            commandManager.registerCommand(injector.getInstance(PageCommand.class));
            commandManager.registerCommand(injector.getInstance(PreviewCommand.class));
            commandManager.registerCommand(injector.getInstance(ReloadCommand.class));
            commandManager.registerCommand(injector.getInstance(RestoreCommand.class));
            commandManager.registerCommand(injector.getInstance(RollbackCommand.class));
            commandManager.registerCommand(injector.getInstance(WandCommand.class));
        }
    }

    /**
     * Disable the plugin.
     */
    protected void disable() {
        Bukkit.getPluginManager().disablePlugin(PrismBukkit.getInstance());

        logger.error("Prism has to disable due to a fatal error.");
    }

    @Override
    public void onDisable() {
        if (storageAdapter != null) {
            storageAdapter.close();
        }
    }

    /**
     * Create a new task chain.
     *
     * @param <T> The type
     * @return The chain
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    /**
     * Log a debug message to console.
     *
     * @param message String
     * @deprecated Use LoggingService
     */
    @Deprecated
    public void debug(String message) {
        if (configurationService.prismConfig().debug()) {
            logger.info(message);
        }
    }

    /**
     * Handle exceptions.
     *
     * @param e The exception
     * @deprecated Use LoggingService
     */
    @Deprecated
    public void handleException(Exception e) {
        e.printStackTrace();
    }
}
