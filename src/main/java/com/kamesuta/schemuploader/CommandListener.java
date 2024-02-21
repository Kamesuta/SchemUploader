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
 * Class for registering commands.
 */
public class CommandListener {
    /**
     * Register the commands.
     */
    public static void register() {
        // Register upload schem command
        if (PluginConfig.uploadEnabled) {
            UploadSchemCommand executor = new UploadSchemCommand();
            PluginCommand command = Objects.requireNonNull(plugin.getCommand("schem_upload"));
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
        // Register download schem command
        if (PluginConfig.downloadEnabled) {
            DownloadSchemCommand executor = new DownloadSchemCommand();
            PluginCommand command = Objects.requireNonNull(plugin.getCommand("schem_download"));
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    /**
     * Command for uploading schem files.
     */
    public static class UploadSchemCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length < 1 || args.length > 2) {
                return false;
            }

            String schemName = args[0];
            String message = args.length == 2 ? args[1] : null;

            // Get the path of the schem file
            String schemFileName = schemName.endsWith(".schem") ? schemName : schemName + ".schem";
            File schemFile = new File(plugin.schematicFolder, schemFileName);
            // Check if the file is included in the directory (to prevent ../ and other path traversal attacks)
            if (!schemFile.getParentFile().equals(plugin.schematicFolder)) {
                sender.spigot().sendMessage(plugin.messages.error("error_invalid_folder"));
                return true;
            }

            // Check if the file exists
            if (!schemFile.exists()) {
                sender.spigot().sendMessage(plugin.messages.error("error_not_found", schemFileName));
                return true;
            }

            // Get the name and UUID of the sender
            String senderName = sender.getName();
            UUID senderUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

            // Send a message if it is expected to take some time
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                sender.spigot().sendMessage(plugin.messages.info("upload_progress"));
            }, 20);

            // Upload the schem file asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Upload the schem file
                DiscordUploader.Result result = DiscordUploader.upload(senderName, senderUUID, schemFile, message);

                // Cancel the progress message task
                task.cancel();

                // Send an error message if the upload fails
                if (!result.success) {
                    sender.spigot().sendMessage(plugin.messages.error("upload_failed", result.error));

                    // Add record to statistics
                    plugin.statistics.actionCounter.increment(Statistics.ActionCounter.ActionType.UPLOAD_FAILURE);

                    return;
                }

                // Send a message when the upload is complete
                sender.spigot().sendMessage(new ComponentBuilder()
                        .append(plugin.messages.success("upload_done", schemFileName))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.messages.getMessage("upload_done_open_url"))))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, result.url))
                        .create()
                );

                // Add record to statistics
                plugin.statistics.actionCounter.increment(Statistics.ActionCounter.ActionType.UPLOAD_SUCCESS);
            });

            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            // Tab completion for the first argument
            if (args.length == 1) {
                return Collections.singletonList("schem_name");
            }
            // Tab completion for the second argument
            if (args.length == 2) {
                return Collections.singletonList("message");
            }
            return null;
        }
    }

    /**
     * Command for downloading schem files.
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

            // Get the path of the schem file
            File schemFile = new File(plugin.schematicFolder, schemName + ".schem");

            // Check if the file is included in the directory (to prevent ../ and other path traversal attacks)
            if (!schemFile.getParentFile().equals(plugin.schematicFolder)) {
                sender.spigot().sendMessage(plugin.messages.error("error_invalid_folder"));
                return true;
            }
            // If the file exists and force flag is not set, send an error message
            if (!force && schemFile.exists()) {
                sender.spigot().sendMessage(plugin.messages.error("error_already_exists", schemName + ".schem"));
                return true;
            }

            // Check the URL prefix if URL restriction is enabled
            if (PluginConfig.downloadUrlRestrictionEnabled && !url.startsWith(PluginConfig.downloadUrlPrefix)) {
                sender.spigot().sendMessage(plugin.messages.error("error_url_prefix", PluginConfig.downloadUrlName));
                return true;
            }

            // Send a message if it is expected to take some time
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                sender.spigot().sendMessage(plugin.messages.info("download_progress"));
            }, 20);

            // Download the schem file asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Download the schem file
                FileDownloader.Result result = FileDownloader.download(schemFile, url, PluginConfig.downloadMaxSize);

                // Cancel the progress message task
                task.cancel();

                // Send an error message if the download fails
                if (!result.success) {
                    if (result.exceededSize) {
                        sender.spigot().sendMessage(plugin.messages.error("error_file_size_exceeded", PluginConfig.downloadMaxSize));
                    } else {
                        sender.spigot().sendMessage(plugin.messages.error("download_failed", result.error));
                    }

                    // Add record to statistics
                    plugin.statistics.actionCounter.increment(Statistics.ActionCounter.ActionType.DOWNLOAD_FAILURE);

                    return;
                }

                // Send a message when the download is complete
                sender.spigot().sendMessage(new ComponentBuilder()
                        .append(plugin.messages.success("download_done", schemName + ".schem"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.messages.getMessage("download_done_open_folder"))))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                        .create()
                );

                // Add record to statistics
                plugin.statistics.actionCounter.increment(Statistics.ActionCounter.ActionType.DOWNLOAD_SUCCESS);
            });

            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            // Tab completion for the first argument
            if (args.length == 1) {
                return Collections.singletonList("schem_name");
            }
            // Tab completion for the second argument
            if (args.length == 2) {
                return Collections.singletonList("url");
            }
            // Tab completion for the third argument
            if (args.length == 3) {
                return Collections.singletonList("-f");
            }
            return null;
        }
    }
}
