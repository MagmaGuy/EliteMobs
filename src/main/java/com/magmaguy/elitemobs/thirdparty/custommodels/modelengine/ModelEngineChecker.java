package com.magmaguy.elitemobs.thirdparty.custommodels.modelengine;

import org.bukkit.Bukkit;

public class ModelEngineChecker {
    private ModelEngineChecker() {
    }

    public static boolean modelEngineIsInstalled() {
        var plugin = Bukkit.getPluginManager().getPlugin("ModelEngine");
        return plugin != null && plugin.isEnabled() && plugin.getDescription().getVersion().contains("R3");
    }
}
