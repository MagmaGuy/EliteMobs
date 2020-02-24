package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

public class NPCInitializer {

    /**
     * Initializes all NPCs in the NPCs.yml config file
     */
    public NPCInitializer() {
        for (NPCsConfigFields npCsConfigFields : NPCsConfig.getNPCsList().values())
            new NPCEntity(npCsConfigFields);
        new NPCWorkingHours();
    }

}
