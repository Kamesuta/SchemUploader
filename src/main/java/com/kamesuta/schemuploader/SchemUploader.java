package com.kamesuta.schemuploader;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * プラグインのメインクラス
 */
public final class SchemUploader extends JavaPlugin {
    /**
     * ロガー
     */
    public static Logger logger;
    /**
     * プラグインのインスタンス
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
     * WorldEditのschematicフォルダ
     */
    public File schematicFolder;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        logger = getLogger();

        // 設定ファイルを読み込む
        saveDefaultConfig();
        PluginConfig.loadConfig(getConfig());
        if (!PluginConfig.isValidConfig()) {
            logger.warning("設定ファイルの読み込みに失敗しました。");
            logger.warning("URLが設定されていない、または初期値である可能性があります。");
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

        // schematicフォルダ
        schematicFolder = new File(PluginConfig.fileFolderPath);

        // コマンドを登録する
        CommandListener.register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
