package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCInitializer {

    /**
     * Initializes all NPCs in the NPCs.yml config file
     */
    public NPCInitializer(World world) {
        for (NPCsConfigFields npCsConfigFields : NPCsConfig.getNPCsList().values()) {
            Location location = ConfigurationLocation.deserialize(npCsConfigFields.getLocation());
            if (location == null) continue;
            if (!location.getWorld().equals(world)) continue;
            new DeveloperMessage("Spawning NPC entity " + npCsConfigFields.getFileName() + " based on world load!");
            new NPCEntity(npCsConfigFields);
        }


        for (NPCEntity npcEntity : EntityTracker.getNPCEntities().values())
            npcEntity.worldLoad(world);
    }

    public NPCInitializer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPCsConfigFields npCsConfigFields : NPCsConfig.getNPCsList().values()) {
                    Location location = ConfigurationLocation.deserialize(npCsConfigFields.getLocation());
                    if (location == null) continue;
                    new DeveloperMessage("Spawning NPC entity " + npCsConfigFields.getFileName() + " based on npc initialization!");
                    new NPCEntity(npCsConfigFields);
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 10);
    }

}
