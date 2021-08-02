package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.config.CustomConfig;
import com.magmaguy.elitemobs.npcs.NPCEntity;

import java.util.HashMap;

public class NPCsConfig extends CustomConfig {

    public static HashMap<String, NPCsConfigFields> npcEntities;

    public static HashMap<String, NPCsConfigFields> getNpcEntities() {
        return npcEntities;
    }

    public NPCsConfig(){
        super("npcs", "com.magmaguy.elitemobs.config.npcs.premade", "NPCsConfigFields");
        npcEntities = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()){
            npcEntities.put(key, (NPCsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
            //Initializes NPC entities
            new NPCEntity((NPCsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        }
    }

}
