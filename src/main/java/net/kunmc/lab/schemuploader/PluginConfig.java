package net.kunmc.lab.schemuploader;

import org.bukkit.configuration.Configuration;

/**
 * プラグインの設定を管理するクラス
 */
public class PluginConfig {
    /**
     * アップロード機能の有効化
     */
    public static boolean uploadEnabled;

    /**
     * アップロード用のDiscord Webhook URL
     */
    public static String uploadWebhookUrl;

    /**
     * ダウンロード機能の有効化
     */
    public static boolean downloadEnabled;

    /**
     * ダウンロードファイルの最大サイズ (バイト) (-1で無制限)
     */
    public static long downloadMaxSize;

    /**
     * ダウンロード元制限機能の有効化
     */
    public static boolean downloadUrlRestrictionEnabled;

    /**
     * ダウンロード用のURL制限のプレフィックス
     */
    public static String downloadUrlPrefix;

    /**
     * ダウンロード用のURL制限場所の名前
     */
    public static String downloadUrlName;

    /**
     * プラグインの設定を読み込む
     *
     * @param config 設定ファイル
     * @return 読み込みに成功したかどうか
     */
    public static void loadConfig(Configuration config) {
        // 設定ファイルを読み込む
        uploadEnabled = config.getBoolean("upload.enabled", true);
        uploadWebhookUrl = config.getString("upload.webhook-url");
        downloadEnabled = config.getBoolean("download.enabled", true);
        downloadMaxSize = config.getLong("download.max-size", -1);
        downloadUrlRestrictionEnabled = config.getBoolean("download.url-restriction.enabled", true);
        downloadUrlPrefix = config.getString("download.url-restriction.prefix");
        downloadUrlName = config.getString("download.url-restriction.name", "指定された場所");
    }

    /**
     * 設定ファイルが正しく設定されているかどうかを返す
     *
     * @return 設定ファイルが正しく設定されているかどうか
     */
    public static boolean isValidConfig() {
        // アップロード機能が有効化されている場合はWebhook URLが設定されている必要がある
        if (uploadEnabled && (uploadWebhookUrl == null || uploadWebhookUrl.contains("xxxx"))) {
            return false;
        }
        // ダウンロード制限機能が有効化されている場合はURL制限が設定されている必要がある
        if (downloadUrlRestrictionEnabled && (downloadUrlPrefix == null || downloadUrlPrefix.contains("xxxx"))) {
            return false;
        }
        // 設定ファイルが正しく設定されている
        return true;
    }
}
