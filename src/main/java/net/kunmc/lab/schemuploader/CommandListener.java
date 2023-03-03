package net.kunmc.lab.schemuploader;

import net.kunmc.lab.commandlib.Command;
import net.kunmc.lab.commandlib.CommandContext;
import net.kunmc.lab.commandlib.CommandLib;
import net.kunmc.lab.commandlib.argument.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.UUID;

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
            CommandLib.register(SchemUploader.instance, new UploadSchemCommand());
        }
        if (PluginConfig.downloadEnabled) {
            CommandLib.register(SchemUploader.instance, new DownloadSchemCommand());
        }
    }

    /**
     * schemファイルをアップロードするコマンド
     */
    public static class UploadSchemCommand extends Command {
        public UploadSchemCommand() {
            super("schem_upload");

            argument(new StringArgument("schem_name", StringArgument.Type.WORD), (schemName, ctx) -> {
                onCommand(schemName, null, ctx);
            });

            argument(new StringArgument("schem_name", StringArgument.Type.WORD), new StringArgument("message", StringArgument.Type.PHRASE), this::onCommand);
        }

        /**
         * コマンドが実行されたときの処理
         *
         * @param schemName schemファイルの名前
         * @param message   メッセージ
         * @param ctx       コマンドのコンテキスト
         */
        private void onCommand(String schemName, String message, CommandContext ctx) {
            // Sender
            CommandSender sender = ctx.getSender();
            // schemファイルのパスを取得
            String schemFileName = schemName.endsWith(".schem") ? schemName : schemName + ".schem";
            File schemFile = new File(SchemUploader.instance.schematicFolder, schemFileName);
            // ファイルがディレクトリに含まれるかどうかを判定 (../などのパスを指定されたとき対策)
            if (!schemFile.getParentFile().equals(SchemUploader.instance.schematicFolder)) {
                sender.sendMessage(Component.text("WorldEditのschematicフォルダー以外の場所を指定することはできません").color(NamedTextColor.RED));
                return;
            }
            // 名前とUUIDを取得
            String senderName = sender.getName();
            UUID senderUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

            // 時間がかかりそうならメッセージを送信
            BukkitTask task = Bukkit.getScheduler().runTaskLater(SchemUploader.instance, () -> {
                sender.sendMessage("アップロード中...");
            }, 20);

            // schemファイルをアップロードする処理
            Bukkit.getScheduler().runTaskAsynchronously(SchemUploader.instance, () -> {
                // schemファイルをアップロードする処理
                DiscordUploader.Result result = DiscordUploader.upload(senderName, senderUUID, schemFile, message);

                // 進行中メッセージ送信タスクをキャンセル
                task.cancel();

                // アップロードに失敗したらメッセージを送信
                if (!result.success) {
                    sender.sendMessage(Component.text("アップロードに失敗しました: " + result.error).color(NamedTextColor.RED));
                    return;
                }

                // アップロードが完了したらメッセージを送信
                sender.sendMessage(Component.text()
                        .append(Component.text(schemFileName)
                                .color(NamedTextColor.GREEN)
                                .hoverEvent(Component.text("クリックでURLを開く").color(NamedTextColor.GREEN))
                                .clickEvent(ClickEvent.openUrl(result.url))
                        )
                        .append(Component.text(" をアップロードしました"))
                );
            });
        }
    }

    /**
     * schemファイルをダウンロードするコマンド
     */
    public static class DownloadSchemCommand extends Command {
        public DownloadSchemCommand() {
            super("schem_download");

            argument(new StringArgument("schem_name", StringArgument.Type.WORD), new StringArgument("url", StringArgument.Type.PHRASE), (schemName, url, ctx) -> {
                onCommand(schemName, url, false, ctx);
            });

            argument(new StringArgument("schem_name", StringArgument.Type.WORD), new StringArgument("-f", StringArgument.Type.WORD), new StringArgument("url", StringArgument.Type.PHRASE), (schemName, force, url, ctx) -> {
                if (!force.equals("-f")) {
                    ctx.getSender().sendMessage(Component.text("オプションが不正です。ファイルを上書きする場合は -f を指定してください").color(NamedTextColor.RED));
                    return;
                }
                onCommand(schemName, url, true, ctx);
            });
        }

        /**
         * コマンドが実行されたときの処理
         *
         * @param schemName schemファイルの名前
         * @param url       URL
         * @param force     上書きするかどうか
         * @param ctx       コマンドのコンテキスト
         */
        private static void onCommand(String schemName, String url, boolean force, CommandContext ctx) {
            // Sender
            CommandSender sender = ctx.getSender();
            // schemファイルのパスを取得
            File schemFile = new File(SchemUploader.instance.schematicFolder, schemName + ".schem");

            // ファイルがディレクトリに含まれるかどうかを判定 (../などのパスを指定されたとき対策)
            if (!schemFile.getParentFile().equals(SchemUploader.instance.schematicFolder)) {
                sender.sendMessage(Component.text("WorldEditのschematicフォルダー以外の場所を指定することはできません").color(NamedTextColor.RED));
                return;
            }
            // ファイルが存在して、上書きしない場合はエラー
            if (!force && schemFile.exists()) {
                sender.sendMessage(Component.text(schemName + ".schem は既に存在します。上書きする場合は -f を指定してください").color(NamedTextColor.RED));
                return;
            }

            // URLのプレフィックスを確認
            if (PluginConfig.downloadUrlRestrictionEnabled && !url.startsWith(PluginConfig.downloadUrlPrefix)) {
                sender.sendMessage(Component.text(PluginConfig.downloadUrlErrorMessage).color(NamedTextColor.RED));
                return;
            }

            // 時間がかかりそうならメッセージを送信
            BukkitTask task = Bukkit.getScheduler().runTaskLater(SchemUploader.instance, () -> {
                sender.sendMessage("ダウンロード中...");
            }, 20);

            // schemファイルをダウンロードする処理
            Bukkit.getScheduler().runTaskAsynchronously(SchemUploader.instance, () -> {
                // schemファイルをダウンロードする処理
                FileDownloader.Result result = FileDownloader.download(schemFile, url, PluginConfig.downloadMaxSize);

                // 進行中メッセージ送信タスクをキャンセル
                task.cancel();

                // ダウンロードに失敗したらメッセージを送信
                if (!result.success) {
                    if (result.exceededSize) {
                        sender.sendMessage(Component.text("ファイルサイズが大きすぎます (最大 " + PluginConfig.downloadMaxSize + " Bytes)").color(NamedTextColor.RED));
                    } else {
                        sender.sendMessage(Component.text("ダウンロードに失敗しました: " + result.error).color(NamedTextColor.RED));
                    }
                    return;
                }

                // ダウンロードが完了したらメッセージを送信
                sender.sendMessage(Component.text()
                        .append(Component.text(schemName + ".schem")
                                .color(NamedTextColor.GREEN)
                                .hoverEvent(Component.text("クリックでURLを開く").color(NamedTextColor.GREEN))
                                .clickEvent(ClickEvent.openUrl(url))
                        )
                        .append(Component.text(" をダウンロードしました"))
                );
            });
        }
    }
}
