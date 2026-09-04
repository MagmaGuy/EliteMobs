package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.config.EliteMobsConfigInheritance;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;
import java.io.File;

public class NPCsConfig extends CustomConfig {

    public static HashMap<String, NPCsConfigFields> npcEntities;
    private static NPCsConfig instance;

    public NPCsConfig() {
        super("npcs", "com.magmaguy.elitemobs.config.npcs.premade",
                NPCsConfigFields.class, EliteMobsConfigInheritance.POLICY);
        instance = this;
        npcEntities = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            npcEntities.put(key, (NPCsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        }
    }

    public static void initializeNPCs() {
        for (NPCsConfigFields npCsConfigFields : npcEntities.values()) {
            if (npCsConfigFields.isEnabled())
                NPCEntity.initializeNPCs(npCsConfigFields);
        }
    }

    public static HashMap<String, NPCsConfigFields> getNpcEntities() {
        return npcEntities;
    }

    public static NPCsConfigFields registerRuntimeFile(File file) {
        if (instance == null) throw new IllegalStateException("NPC configuration is not initialized");
        NPCsConfigFields fields = (NPCsConfigFields) instance.registerFile(file);
        if (fields != null) npcEntities.put(fields.getFilename(), fields);
        return fields;
    }

}
