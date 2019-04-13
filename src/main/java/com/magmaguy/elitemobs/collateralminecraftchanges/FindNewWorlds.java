package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.npcs.NPCInitializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class FindNewWorlds implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {

        ValidWorldsConfig validWorldsConfig = new ValidWorldsConfig();
        validWorldsConfig.initializeConfig();
        ConfigValues.validWorldsConfig = validWorldsConfig.configuration;

        if (event.getWorld().getName().equals(ConfigValues.adventurersGuildConfig.getString(AdventurersGuildConfig.GUILD_WORLD_NAME))) {
            NPCInitializer npcInitializer = new NPCInitializer();
        }

    }

}
