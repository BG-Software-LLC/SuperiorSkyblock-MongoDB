package com.bgsoftware.superiorskyblock.module.mongodb;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.module.mongodb.bridge.MongoDatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.module.mongodb.threading.DatabaseExecutor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

public final class MongoDBModule extends PluginModule {

    public MongoDBModule() {
        super("SSB-MongoDB", "Ome_R");
    }

    @Override
    public void onEnable(SuperiorSkyblock plugin) {
        YamlConfiguration config;

        try {
            config = loadConfigFile();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create config.yml file:", ex);
        }

        String url = config.getString("url");
        String database = config.getString("database");

        try {
            MongoDBClient.connect(url, database);
        } catch (Exception ex) {
            getLogger().info("Failed to log in to MongoDB, shutting down server...");
            Bukkit.shutdown();
            throw ex;
        }

        MongoDBInitializer.initDatabase(plugin);

        plugin.getFactory().registerDatabaseBridgeFactory(new MongoDatabaseBridgeFactory());
    }

    @Override
    public void onReload(SuperiorSkyblock superiorSkyblock) {

    }

    @Override
    public void onDisable(SuperiorSkyblock superiorSkyblock) {
        MongoDBClient.close();
        DatabaseExecutor.shutdown();
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblock superiorSkyblock) {
        return new Listener[0];
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock superiorSkyblock) {
        return new SuperiorCommand[0];
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock superiorSkyblock) {
        return new SuperiorCommand[0];
    }

    private YamlConfiguration loadConfigFile() throws IOException {
        File configFile = new File(getModuleFolder(), "config.yml");

        if (!configFile.exists()) {
            saveResource("config.yml");
        }

        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(configFile);
        config.syncWithConfig(configFile, getResource("config.yml"));

        return config;
    }

}
