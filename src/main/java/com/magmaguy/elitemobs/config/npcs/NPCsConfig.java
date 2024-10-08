package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;

public class NPCsConfig extends CustomConfig {

    public static HashMap<String, NPCsConfigFields> npcEntities;

    public NPCsConfig() {
        super("npcs", "com.magmaguy.elitemobs.config.npcs.premade", NPCsConfigFields.class);
        npcEntities = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            npcEntities.put(key, (NPCsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
            //Initializes NPC entities
            NPCsConfigFields npCsConfigFields = (NPCsConfigFields) super.getCustomConfigFieldsHashMap().get(key);
            if (npCsConfigFields.isEnabled())
                NPCEntity.initializeNPCs(npCsConfigFields);
        }
    }

    public static HashMap<String, NPCsConfigFields> getNpcEntities() {
        return npcEntities;
    }

}
