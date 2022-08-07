package com.magmaguy.elitemobs.thirdparty.bstats;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class CustomCharts {
    public CustomCharts() {
        Metrics metrics = EliteMobs.metrics;
        metrics.addCustomChart(new AdvancedPie("minidungeons", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
                valueMap.put(
                        ChatColor.stripColor(emPackage.getDungeonPackagerConfigFields().getFilename()),
                        emPackage.isInstalled() ? 1 : 0);
            }
            return valueMap;
        }));
    }
}
