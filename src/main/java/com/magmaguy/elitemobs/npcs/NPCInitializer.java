package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import org.bukkit.Bukkit;

public class NPCInitializer {

    /**
     * Initializes all NPCs in the NPCs.yml config file
     */
    public NPCInitializer() {

        for (NPCsConfigFields npCsConfigFields : NPCsConfig.getNPCsList().values())
            new NPCEntity(npCsConfigFields);

        Bukkit.getPluginManager().registerEvents(new NPCChunkLoad(), MetadataHandler.PLUGIN);

        new NPCWorkingHours();

    }

}
