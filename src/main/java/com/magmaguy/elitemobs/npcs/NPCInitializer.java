package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import org.bukkit.Location;
import org.bukkit.World;

public class NPCInitializer {

    /**
     * Initializes all NPCs in the NPCs.yml config file
     */
    public NPCInitializer(World world) {
        for (NPCEntity npcEntity : EntityTracker.getNPCEntities().values())
            npcEntity.worldLoad(world);
    }

    public NPCInitializer() {
        for (NPCsConfigFields npCsConfigFields : NPCsConfig.getNPCsList().values()) {
            Location location = ConfigurationLocation.deserialize(npCsConfigFields.getLocation());
            if (location != null)
                new NPCEntity(npCsConfigFields);
            new NPCWorkingHours();
        }
    }

}
