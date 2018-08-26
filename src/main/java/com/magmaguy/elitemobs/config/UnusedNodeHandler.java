package com.magmaguy.elitemobs.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

public class UnusedNodeHandler {

    public static Configuration clearNodes(Configuration configuration) {

        for (String actual : configuration.getKeys(true)) {

            boolean keyExists = false;

            for (String defaults : configuration.getDefaults().getKeys(true)) {

                if (actual.equals(defaults)) {

                    if (actual.equals("test"))
                        Bukkit.getLogger().warning("Oh shit");

                    keyExists = true;

                }

            }

            if (!keyExists) {

                configuration.set(actual, null);
                Bukkit.getLogger().info("[EliteMobs] Deleting unused config values.");

            }

        }

        return configuration;

    }

}
