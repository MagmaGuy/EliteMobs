package com.magmaguy.elitemobs.thirdparty.custommodels.modelengine;

import org.bukkit.Bukkit;

public class ModelEngineChecker {
    private static Boolean isInstalled = null;

    private ModelEngineChecker() {
    }

    public static boolean modelEngineIsInstalled() {
        if (isInstalled == null) {
            if (!Bukkit.getPluginManager().isPluginEnabled("ModelEngine"))
                return isInstalled = false;
            if (!Bukkit.getPluginManager().getPlugin("ModelEngine").getDescription().getVersion().contains("R3"))
                return isInstalled = false;
        }
        return isInstalled;
    }
}
