package com.kamesuta.schemuploader;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kamesuta.schemuploader.SchemUploader.plugin;

/**
 * Statistics of the plugin
 */
public class Statistics {
    public final ActionCounter actionCounter = new ActionCounter();

    /**
     * Register bStats
     */
    public void register() {
        // Enable bStats
        Metrics metrics = new Metrics(plugin, 21061);

        // Config charts
        metrics.addCustomChart(new SimplePie("uploadEnabled", () -> PluginConfig.uploadEnabled ? "enabled" : "disabled"));
        metrics.addCustomChart(new SimplePie("downloadEnabled", () -> PluginConfig.downloadEnabled ? "enabled" : "disabled"));
        metrics.addCustomChart(new SimplePie("downloadUrlRestrictionEnabled", () -> PluginConfig.downloadUrlRestrictionEnabled ? "enabled" : "disabled"));
        metrics.addCustomChart(new SimplePie("language", () -> PluginConfig.language));

        // The number of power actions performed
        for (ActionCounter.ActionType actionType : ActionCounter.ActionType.values()) {
            metrics.addCustomChart(new SingleLineChart(actionType.name, () -> actionCounter.collect(actionType)));
        }
    }

    /**
     * The counter for each action
     */
    public static class ActionCounter {
        private final Map<ActionType, AtomicInteger> countMap = new EnumMap<>(ActionType.class);

        /**
         * Increment the counter
         *
         * @param actionType type of action to get statistics for
         */
        public void increment(ActionType actionType) {
            getOrCreate(actionType).incrementAndGet();
        }

        /**
         * Get the collected value and reset the counter
         *
         * @param actionType type of action to get statistics for
         * @return The collected value
         */
        public int collect(ActionType actionType) {
            return getOrCreate(actionType).getAndSet(0);
        }

        /**
         * Get or create the counter
         *
         * @param actionType type of action to get statistics for
         * @return The counter
         */
        private AtomicInteger getOrCreate(ActionType actionType) {
            return countMap.computeIfAbsent(actionType, (k) -> new AtomicInteger());
        }

        /**
         * The service to collect statistics
         */
        public enum ActionType {
            UPLOAD_SUCCESS("upload_success"),
            DOWNLOAD_SUCCESS("download_success"),
            UPLOAD_FAILURE("upload_failure"),
            DOWNLOAD_FAILURE("download_failure"),
            ;

            public final String name;

            ActionType(String name) {
                this.name = name;
            }
        }
    }
}
