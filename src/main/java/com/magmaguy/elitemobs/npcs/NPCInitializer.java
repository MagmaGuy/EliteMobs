package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import org.bukkit.Location;
import org.bukkit.World;

public class NPCInitializer {

    /**
     * Initializes all NPCs in the NPCs.yml config file
     */
    public NPCInitializer(World world) {
        for (NPCsConfigFields npCsConfigFields : NPCsConfig.getNPCsList().values()) {
            if (npCsConfigFields.getLocation() == null) continue;
            Location location = ConfigurationLocation.deserialize(npCsConfigFields.getLocation());
            if (location != null)
                if (location.getWorld().equals(world))
                    new NPCEntity(npCsConfigFields);
            new NPCWorkingHours();
        }
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
