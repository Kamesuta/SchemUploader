package com.kamesuta.schemuploader;

import org.bukkit.configuration.Configuration;

import static com.kamesuta.schemuploader.SchemUploader.logger;

/**
 * Class for managing plugin configuration.
 */
public class PluginConfig {
    /**
     * Language
     */
    public static String language;

    /**
     * Enable upload feature
     */
    public static boolean uploadEnabled;

    /**
     * Discord Webhook URL for uploading
     */
    public static String uploadWebhookUrl;

    /**
     * Enable download feature
     */
    public static boolean downloadEnabled;

    /**
     * Maximum size of downloaded files (in bytes) (-1 for unlimited)
     */
    public static long downloadMaxSize;

    /**
     * Enable download URL restriction feature
     */
    public static boolean downloadUrlRestrictionEnabled;

    /**
     * Prefix for download URL restriction
     */
    public static String downloadUrlPrefix;

    /**
     * Path to the schematics folder
     */
    public static String fileFolderPath;

    /**
     * Load the plugin configuration
     *
     * @param config Configuration file
     */
    public static void loadConfig(Configuration config) {
        // Load the configuration file
        language = config.getString("language", "en");
        uploadEnabled = config.getBoolean("upload.enabled", true);
        uploadWebhookUrl = config.getString("upload.webhook-url");
        downloadEnabled = config.getBoolean("download.enabled", true);
        downloadMaxSize = config.getLong("download.max-size", -1);
        downloadUrlRestrictionEnabled = config.getBoolean("download.url-restriction.enabled", true);
        downloadUrlPrefix = config.getString("download.url-restriction.prefix");
        fileFolderPath = config.getString("file.folder-path", "plugins/WorldEdit/schematics");
    }

    /**
     * Check if the configuration is valid
     *
     * @return True if the configuration is valid, false otherwise
     */
    public static boolean isValidConfig() {
        // If upload feature is enabled, webhook URL must be set
        if (uploadEnabled && (uploadWebhookUrl == null || uploadWebhookUrl.contains("xxxx"))) {
            return false;
        }
        // If download restriction feature is enabled, URL prefix must be set
        if (downloadUrlRestrictionEnabled && (downloadUrlPrefix == null || downloadUrlPrefix.contains("xxxx"))) {
            return false;
        }
        // Configuration is valid
        return true;
    }

    /**
     * Validate the configuration
     */
    public static void validateConfig() {
        // If upload feature is enabled, webhook URL must be set
        if (uploadEnabled && (uploadWebhookUrl == null || uploadWebhookUrl.contains("xxxx"))) {
            logger.warning("uploadEnabled is true, but uploadWebhookUrl is not set. uploadWebhookUrl is required when uploadEnabled is true.");
        }
        // If download restriction feature is enabled, URL prefix must be set
        if (downloadUrlRestrictionEnabled && (downloadUrlPrefix == null || downloadUrlPrefix.contains("xxxx"))) {
            logger.warning("downloadUrlRestrictionEnabled is true, but downloadUrlPrefix is not set. downloadUrlPrefix is required when downloadUrlRestrictionEnabled is true.");
        }
    }
}
