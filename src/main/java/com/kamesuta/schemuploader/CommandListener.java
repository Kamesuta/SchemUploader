package com.kamesuta.schemuploader;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.kamesuta.schemuploader.SchemUploader.plugin;

/**
 * コマンドを登録するクラス
 */
public class CommandListener {
    /**
     * コマンドを登録する
     */
    public static void register() {
        // コマンドを登録する処理
        if (PluginConfig.uploadEnabled) {
            UploadSchemCommand executor = new UploadSchemCommand();
            PluginCommand command = Objects.requireNonNull(plugin.getCommand("schem_upload"));
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
        if (PluginConfig.downloadEnabled) {
            DownloadSchemCommand executor = new DownloadSchemCommand();
            PluginCommand command = Objects.requireNonNull(plugin.getCommand("schem_download"));
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    /**
     * schemファイルをアップロードするコマンド
     */
    public static class UploadSchemCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length < 1 || args.length > 2) {
                return false;
            }

            String schemName = args[0];
            String message = args.length == 2 ? args[1] : null;

            // schemファイルのパスを取得
            String schemFileName = schemName.endsWith(".schem") ? schemName : schemName + ".schem";
            File schemFile = new File(plugin.schematicFolder, schemFileName);
            // ファイルがディレクトリに含まれるかどうかを判定 (../などのパスを指定されたとき対策)
            if (!schemFile.getParentFile().equals(plugin.schematicFolder)) {
                sender.sendMessage(plugin.messages.error("error_invalid_folder"));
                return true;
            }
            // 名前とUUIDを取得
            String senderName = sender.getName();
            UUID senderUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

            // 時間がかかりそうならメッセージを送信
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                sender.sendMessage(plugin.messages.info("upload_progress"));
            }, 20);

            // schemファイルをアップロードする処理
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // schemファイルをアップロードする処理
                DiscordUploader.Result result = DiscordUploader.upload(senderName, senderUUID, schemFile, message);

                // 進行中メッセージ送信タスクをキャンセル
                task.cancel();

                // アップロードに失敗したらメッセージを送信
                if (!result.success) {
                    sender.sendMessage(plugin.messages.error("upload_failed", result.error));
                    return;
                }

                // アップロードが完了したらメッセージを送信
                sender.sendMessage(new ComponentBuilder()
                        .append(plugin.messages.success("upload_done", schemFileName))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.messages.getMessage("upload_done_open_url"))))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, result.url))
                        .create()
                );
            });

            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            // 1つ目の引数の補完
            if (args.length == 1) {
                return Collections.singletonList("schem_name");
            }
            // 2つ目の引数の補完
            if (args.length == 2) {
                return Collections.singletonList("message");
            }
            return null;
        }
    }

    /**
     * schemファイルをダウンロードするコマンド
     */
    public static class DownloadSchemCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length < 2 || args.length > 3) {
                return false;
            }

            String schemName = args[0];
            String url = args[1];
            boolean force = args.length == 3 && args[2].equals("-f");

            // schemファイルのパスを取得
            File schemFile = new File(plugin.schematicFolder, schemName + ".schem");

            // ファイルがディレクトリに含まれるかどうかを判定 (../などのパスを指定されたとき対策)
            if (!schemFile.getParentFile().equals(plugin.schematicFolder)) {
                sender.sendMessage(plugin.messages.error("error_invalid_folder"));
                return true;
            }
            // ファイルが存在して、上書きしない場合はエラー
            if (!force && schemFile.exists()) {
                sender.sendMessage(plugin.messages.error("error_already_exists", schemName + ".schem"));
                return true;
            }

            // URLのプレフィックスを確認
            if (PluginConfig.downloadUrlRestrictionEnabled && !url.startsWith(PluginConfig.downloadUrlPrefix)) {
                sender.sendMessage(plugin.messages.error("error_url_prefix"));
                return true;
            }

            // 時間がかかりそうならメッセージを送信
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                sender.sendMessage(plugin.messages.info("download_progress"));
            }, 20);

            // schemファイルをダウンロードする処理
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // schemファイルをダウンロードする処理
                FileDownloader.Result result = FileDownloader.download(schemFile, url, PluginConfig.downloadMaxSize);

                // 進行中メッセージ送信タスクをキャンセル
                task.cancel();

                // ダウンロードに失敗したらメッセージを送信
                if (!result.success) {
                    if (result.exceededSize) {
                        sender.sendMessage(plugin.messages.error("error_file_size_exceeded", PluginConfig.downloadMaxSize));
                    } else {
                        sender.sendMessage(plugin.messages.error("download_failed", result.error));
                    }
                    return;
                }

                // ダウンロードが完了したらメッセージを送信
                sender.sendMessage(new ComponentBuilder()
                        .append(plugin.messages.success("download_done", schemName + ".schem"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.messages.getMessage("download_done_open_folder"))))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                        .create()
                );
            });

            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            // 1つ目の引数の補完
            if (args.length == 1) {
                return Collections.singletonList("schem_name");
            }
            // 2つ目の引数の補完
            if (args.length == 2) {
                return Collections.singletonList("url");
            }
            // 3つ目の引数の補完
            if (args.length == 3) {
                return Collections.singletonList("-f");
            }
            return null;
        }
    }
}
