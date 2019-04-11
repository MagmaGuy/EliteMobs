package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class FindNewWorlds implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {

        ValidWorldsConfig validWorldsConfig = new ValidWorldsConfig();
        validWorldsConfig.initializeConfig();
        ConfigValues.validWorldsConfig = validWorldsConfig.configuration;

    }

}
