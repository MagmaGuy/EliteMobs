package com.magmaguy.elitemobs.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;

public class UnusedNodeHandler {

    public static Configuration clearNodes(Configuration configuration) {

        for (String actual : configuration.getKeys(false)) {

            boolean keyExists = false;

            for (String defaults : configuration.getDefaults().getKeys(true)) {

                if (actual.equals(defaults)) {

                    keyExists = true;

                }

            }

            if (!keyExists) {

                configuration.set(actual, null);
                Bukkit.getLogger().warning(actual);
                Bukkit.getLogger().info("[EliteMobs] Deleting unused config values.");

            }

        }

        return configuration;

    }

    public static Configuration clearNodes(FileConfiguration configuration) {

        for (String actual : configuration.getKeys(false)) {
            boolean keyExists = false;
            for (String defaults : configuration.getDefaults().getKeys(true))
                if (actual.equals(defaults))
                    keyExists = true;

            if (!keyExists) {
                configuration.set(actual, null);
                Bukkit.getLogger().warning(actual);
                Bukkit.getLogger().info("[EliteMobs] Deleting unused config values.");
            }
        }
        return configuration;
    }

}
