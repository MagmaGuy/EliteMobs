package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.Bukkit;

public class NPCInitializer {

    /**
     * Initializes all NPCs in the NPCs.yml config file
     */
    public NPCInitializer() {

        for (String npcKey : ConfigValues.npcConfig.getKeys(false))
            new NPCEntity(npcKey);

        //Event gets registered here to avoid reloading these entities right after they get initialized
        Bukkit.getPluginManager().registerEvents(new NPCChunkLoad(), MetadataHandler.PLUGIN);

        new NPCWorkingHours();

    }

}
