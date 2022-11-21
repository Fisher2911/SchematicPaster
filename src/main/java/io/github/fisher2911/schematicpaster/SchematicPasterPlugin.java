package io.github.fisher2911.schematicpaster;

import io.github.fisher2911.fisherlib.FishPlugin;
import io.github.fisher2911.fisherlib.gui.AbstractGuiManager;
import io.github.fisher2911.fisherlib.listener.GlobalListener;
import io.github.fisher2911.fisherlib.message.MessageHandler;
import io.github.fisher2911.fisherlib.placeholder.Placeholders;
import io.github.fisher2911.schematicpaster.command.SchematicCommand;
import io.github.fisher2911.schematicpaster.config.SchematicSettings;
import io.github.fisher2911.schematicpaster.data.DataManager;
import io.github.fisher2911.schematicpaster.gui.SchematicGuiManager;
import io.github.fisher2911.schematicpaster.listener.ChunkListener;
import io.github.fisher2911.schematicpaster.listener.JoinListener;
import io.github.fisher2911.schematicpaster.listener.PlaceListener;
import io.github.fisher2911.schematicpaster.message.SchematicMessages;
import io.github.fisher2911.schematicpaster.placeholder.SchematicPlaceholders;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilderManager;
import io.github.fisher2911.schematicpaster.schematic.SchematicTaskManager;
import io.github.fisher2911.schematicpaster.user.SchematicUser;
import io.github.fisher2911.schematicpaster.user.UserManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;

public final class SchematicPasterPlugin extends FishPlugin<SchematicUser, SchematicPasterPlugin> {

    private GlobalListener globalListener;
    private Placeholders placeholders;
    private MessageHandler messageHandler;
    private DataManager dataManager;
    private UserManager userManager;
    private SchematicGuiManager schematicGuiManager;
    private SchematicSettings schematicSettings;
    private SchematicBuilderManager schematicBuilderManager;
    private SchematicTaskManager schematicTaskManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        this.globalListener = new GlobalListener(this);
        this.placeholders = new SchematicPlaceholders();
        this.messageHandler = MessageHandler.createInstance(this, this.placeholders);
        this.dataManager = new DataManager(this);
        this.messageHandler.load(SchematicMessages.values());
        this.userManager = new UserManager(this, new HashMap<>());
        this.schematicGuiManager = new SchematicGuiManager(this);
        this.schematicSettings = new SchematicSettings(this);
        this.schematicBuilderManager = new SchematicBuilderManager(this, new HashMap<>());
        this.schematicTaskManager = new SchematicTaskManager(new HashMap<>());

        final File folder = this.getDataFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }
        this.load();
        this.registerCommands();
        this.registerListeners();
        final int bStatsPluginId = 16904;
        Metrics metrics = new Metrics(this, bStatsPluginId);
    }

    public void registerListeners() {
        new PlaceListener(this).init();
        new JoinListener(this).init();
        final ChunkListener chunkListener = new ChunkListener(this);
        chunkListener.init();
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                chunkListener.loadChunk(chunk);
            }
        }
    }

    public void registerCommands() {
        final SchematicCommand schematicCommand = new SchematicCommand(this, new HashMap<>());
        this.getCommand("schematicpaster").setExecutor(schematicCommand);
    }

    public void load() {
        this.dataManager.load();
        this.schematicSettings.load();
        this.schematicBuilderManager.load();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.dataManager.saveSchematicTasks(this.schematicTaskManager.getAll());
    }

    @Override
    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    @Override
    public GlobalListener getGlobalListener() {
        return this.globalListener;
    }

    @Override
    public Placeholders getPlaceholders() {
        return this.placeholders;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    @Override
    public AbstractGuiManager<SchematicUser, SchematicPasterPlugin> getGuiManager() {
        return this.schematicGuiManager;
    }

    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }

    public SchematicSettings getSchematicSettings() {
        return schematicSettings;
    }

    public SchematicBuilderManager getSchematicBuilderManager() {
        return schematicBuilderManager;
    }

    public SchematicTaskManager getSchematicTaskManager() {
        return schematicTaskManager;
    }

}
