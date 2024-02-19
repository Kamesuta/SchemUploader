package com.kamesuta.schemuploader;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import static com.kamesuta.schemuploader.SchemUploader.logger;
import static com.kamesuta.schemuploader.SchemUploader.plugin;

/**
 * Plugin Translations
 */
public class Messages {
    private final Configuration messages;
    private final Messages parent;

    /**
     * Create a new Messages
     *
     * @param messages Messages configuration
     * @param parent   Parent messages
     */
    private Messages(Configuration messages, Messages parent) {
        this.messages = messages;
        this.parent = parent;
    }

    /**
     * Load messages.yml
     *
     * @param language Language
     *                 If the language is not found, the parent messages will be used
     * @param parent   Parent messages
     * @return Messages
     */
    public static Messages load(String language, Messages parent) {
        try {
            // Create the data folder if it does not exist
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdir();
            }

            // Create messages.yml if it does not exist
            File file = copyFileToDataFolder("messages_" + language + ".yml");

            // Load messages.yml
            Configuration messages = YamlConfiguration.loadConfiguration(file);

            return new Messages(messages, parent);
        } catch (IOException e) {
            logger.severe("Failed to create/load messages.yml");
            throw new RuntimeException(e);
        }
    }

    /**
     * Load messages.yml from resource
     *
     * @param language Language
     *                 If the language is not found, the parent messages will be used
     * @param parent   Parent messages
     * @return Messages
     */
    public static Messages loadFromResource(String language, Messages parent) {
        // Load messages.yml
        try (InputStream in = plugin.getResource("messages_" + language + ".yml")) {
            // If the language is not found, throw an exception
            if (in == null) {
                throw new IOException("messages_" + language + ".yml not found");
            }

            // Load messages.yml
            Configuration messages = YamlConfiguration.loadConfiguration(new InputStreamReader(in));

            return new Messages(messages, parent);
        } catch (IOException e) {
            logger.severe("Failed to load fallback messages_" + language + ".yml");
            throw new RuntimeException(e);
        }
    }

    /**
     * Copy a file from the plugin's resources to the data folder
     *
     * @param fileName The file name in the data folder
     * @return The file
     * @throws IOException If an I/O error occurs
     */
    public static File copyFileToDataFolder(String fileName) throws IOException {
        // Create config.yml if it does not exist
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            try (InputStream in = plugin.getResource(fileName)) {
                // If the file is not found, throw an exception
                if (in == null) {
                    throw new IOException(fileName + " not found");
                }

                Files.copy(in, file.toPath());
            }
        }
        return file;
    }

    /**
     * Get translated message
     *
     * @param key  Message key
     * @param args Message arguments
     * @return Translated message
     */
    public String getMessage(String key, Object... args) {
        String rawMessage = this.messages.getString(key, null);
        if (rawMessage != null) {
            return String.format(rawMessage, args);
        } else if (parent != null) {
            return parent.getMessage(key, args);
        } else {
            return "Message key not found: " + key;
        }
    }

    public ComponentBuilder prefix() {
        return new ComponentBuilder(getMessage("prefix")).color(ChatColor.LIGHT_PURPLE);
    }

    public BaseComponent[] success(String key, Object... args) {
        return prefix().append(new ComponentBuilder(getMessage(key, args)).color(ChatColor.GREEN).create()).create();
    }

    public BaseComponent[] error(String key, Object... args) {
        return prefix().append(new ComponentBuilder(getMessage(key, args)).color(ChatColor.RED).create()).create();
    }

    public BaseComponent[] warning(String key, Object... args) {
        return prefix().append(new ComponentBuilder(getMessage(key, args)).color(ChatColor.YELLOW).create()).create();
    }

    public BaseComponent[] info(String key, Object... args) {
        return prefix().append(new ComponentBuilder(getMessage(key, args)).color(ChatColor.LIGHT_PURPLE).create()).create();
    }
}