package com.magmaguy.elitemobs.thirdparty.bstats;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class CustomCharts {
    public CustomCharts() {
        Metrics metrics = EliteMobs.metrics;
        metrics.addCustomChart(new Metrics.AdvancedPie("minidungeons", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                for (Minidungeon minidungeon : Minidungeon.minidungeons.values()) {
                    valueMap.put(
                            ChatColor.stripColor(minidungeon.dungeonPackagerConfigFields.getName()),
                            minidungeon.isInstalled ? 1 : 0);
                }
                return valueMap;
            }
        }));
    }
}
