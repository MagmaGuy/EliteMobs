package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.config.EliteMobsConfigInheritance;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.magmaguy.magmacore.util.Logger;
import java.io.File;

public class NPCsConfig extends CustomConfig {

    public static HashMap<String, NPCsConfigFields> npcEntities;
    private static NPCsConfig instance;
    private final Set<String> reportedDisguiseConflicts = new HashSet<>();

    public NPCsConfig() {
        super("npcs", "com.magmaguy.elitemobs.config.npcs.premade",
                NPCsConfigFields.class, EliteMobsConfigInheritance.POLICY);
        instance = this;
        npcEntities = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            npcEntities.put(key, (NPCsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        }
        reportDisguiseConflicts();
    }

    private void reportDisguiseConflicts() {
        var definitions = new HashMap<String, NPCsConfigFields>();
        for (NPCsConfigFields fields : npcEntities.values()) {
            String disguise = fields.getDisguise();
            if (!fields.isEnabled() || disguise == null || !disguise.startsWith("custom:")
                    || fields.getCustomDisguiseData() == null) continue;
            NPCsConfigFields previous = definitions.putIfAbsent(disguise, fields);
            if (previous != null && !previous.getCustomDisguiseData().equals(fields.getCustomDisguiseData())
                    && reportedDisguiseConflicts.add(disguise))
                Logger.warn("NPCs " + previous.getFilename() + " and " + fields.getFilename()
                        + " use different skin data under " + disguise
                        + ". Give them distinct custom disguise names. Existing files and LibsDisguises aliases were preserved.");
        }
    }

    public static void initializeNPCs() {
        for (NPCsConfigFields npCsConfigFields : npcEntities.values()) {
            if (npCsConfigFields.isEnabled())
                NPCEntity.initializeNPCs(npCsConfigFields);
        }
        NPCEntity.startNameplates();
    }

    public static HashMap<String, NPCsConfigFields> getNpcEntities() {
        return npcEntities;
    }

    public static NPCsConfigFields registerRuntimeFile(File file) {
        if (instance == null) throw new IllegalStateException("NPC configuration is not initialized");
        for (var loaded : instance.getCustomConfigFieldsHashMap().values()) {
            if (loaded.getFilename().equalsIgnoreCase(file.getName()) && !sameFile(loaded.getFile(), file))
                throw new IllegalArgumentException("Another configuration already owns filename " + file.getName());
        }
        NPCsConfigFields fields = (NPCsConfigFields) instance.registerFile(file);
        if (fields != null) {
            npcEntities.put(fields.getFilename(), fields);
            instance.reportDisguiseConflicts();
        }
        return fields;
    }

    public static void unregisterRuntimeFile(File file) {
        if (instance == null) return;
        instance.getCustomConfigFieldsHashMap().entrySet().removeIf(entry -> sameFile(entry.getValue().getFile(), file));
        npcEntities.entrySet().removeIf(entry -> sameFile(entry.getValue().getFile(), file));
    }

    private static boolean sameFile(File left, File right) {
        return left != null && left.toPath().toAbsolutePath().normalize()
                .equals(right.toPath().toAbsolutePath().normalize());
    }

}
