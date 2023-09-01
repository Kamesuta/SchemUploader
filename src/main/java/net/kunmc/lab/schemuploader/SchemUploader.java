package net.kunmc.lab.schemuploader;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * プラグインのメインクラス
 */
public final class SchemUploader extends JavaPlugin {
    /**
     * プラグインのインスタンス
     */
    public static SchemUploader instance;

    /**
     * WorldEditのschematicフォルダのパス
     */
    public static final String SCHEMATIC_FOLDER = "plugins/WorldEdit/schematics";

    /**
     * WorldEditのschematicフォルダ
     */
    public File schematicFolder;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // 設定ファイルを読み込む
        saveDefaultConfig();
        PluginConfig.loadConfig(getConfig());
        if (!PluginConfig.isValidConfig()) {
            getLogger().warning("設定ファイルの読み込みに失敗しました。");
            getLogger().warning("URLが設定されていない、または初期値である可能性があります。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
