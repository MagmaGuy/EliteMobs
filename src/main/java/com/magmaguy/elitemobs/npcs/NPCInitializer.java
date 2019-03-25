package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.config.ConfigValues;

public class NPCInitializer {

    /**
     * Initializes all NPCs in the NPCs.yml config file
     */
    public NPCInitializer() {

        for (String npcKey : ConfigValues.npcConfig.getKeys(false))
            new NPCEntity(npcKey);

        new NPCWorkingHours();

    }

}
