package com.kamesuta.schemuploader;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * Main class of the plugin
 */
public final class SchemUploader extends JavaPlugin {
    /**
     * Logger
     */
    public static Logger logger;
    /**
     * Plugin instance
     */
    public static SchemUploader plugin;

    /**
     * Fallback Translations
     */
    public Messages fallbackMessages;
    /**
     * Translations
     */
    public Messages messages;

    /**
     * WorldEdit schematic folder
     */
    public File schematicFolder;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        logger = getLogger();

        // Load the configuration file
        saveDefaultConfig();
        PluginConfig.loadConfig(getConfig());
        if (!PluginConfig.isValidConfig()) {
            logger.warning("Failed to load configuration file.");
            logger.warning("The URL may not be set or it may be the default value.");
            PluginConfig.validateConfig();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load messages.yml
        fallbackMessages = Messages.loadFromResource("en", null);
        // Try to load messages.yml from resource
        // This way, you only need to define the translation of the differences from the default language
        Messages resourceMessages;
        try {
            resourceMessages = Messages.loadFromResource(PluginConfig.language, fallbackMessages);
        } catch (Exception e) {
            // This is used when you add a language to the config that the plugin does not support by default
            resourceMessages = fallbackMessages;
        }
        // Load messages.yml
        messages = Messages.load(PluginConfig.language, resourceMessages);

        // Schematic folder
        schematicFolder = new File(PluginConfig.fileFolderPath);

        // Register commands
        CommandListener.register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
