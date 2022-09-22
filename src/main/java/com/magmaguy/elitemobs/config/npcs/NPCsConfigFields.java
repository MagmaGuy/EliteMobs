package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class NPCsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    @Getter
    @Setter
    public String noPreviousLocationMessage;
    @Getter
    @Setter
    private String name = "";
    @Getter
    @Setter
    private String role = "";
    @Getter
    @Setter
    private Villager.Profession profession = Villager.Profession.NITWIT;
    @Getter
    private String location;
    @Getter
    @Setter
    private List<String> greetings = new ArrayList<>();
    @Getter
    @Setter
    private List<String> dialog = new ArrayList<>();
    @Getter
    @Setter
    private List<String> farewell = new ArrayList<>();
    @Getter
    @Setter
    private boolean canTalk = true;
    @Getter
    @Setter
    private double activationRadius = 3;
    @Getter
    @Setter
    private NPCInteractions.NPCInteractionType interactionType = NPCInteractions.NPCInteractionType.NONE;
    @Getter
    @Setter
    private double timeout = 0;
    @Getter
    @Setter
    private List<String> questFilenames = null;
    /**
     * Integration with LibsDisguises. Only used if that plugin is loaded.
     */
    @Getter
    @Setter
    private String disguise = null;
    @Getter
    @Setter
    private String customDisguiseData = null;
    @Getter
    @Setter
    private String customModel = null;
    @Getter
    @Setter
    private String arenaFilename;
    @Getter
    @Setter
    private List<String> locations = new ArrayList<>();
    @Getter
    @Setter
    private String command;

    public NPCsConfigFields(String fileName,
                            boolean isEnabled,
                            String name,
                            String role,
                            Villager.Profession profession,
                            String location,
                            List<String> greetings,
                            List<String> dialog,
                            List<String> farewell,
                            boolean canTalk,
                            double activationRadius,
                            NPCInteractions.NPCInteractionType interactionType) {
        super(fileName, isEnabled);
        this.name = name;
        this.role = role;
        this.profession = profession;
        this.location = location;
        this.greetings = greetings;
        this.dialog = dialog;
        this.farewell = farewell;
        this.canTalk = canTalk;
        this.activationRadius = activationRadius;
        this.interactionType = interactionType;
    }

    public NPCsConfigFields(String filename,
                            boolean isEnabled) {
        super(filename, isEnabled);
    }

    public void setLocation(String location) {
        this.locations.add(location);
        this.fileConfiguration.set("spawnLocations", locations);
        try {
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, this.file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to update the location status for an NPC with no config file! Did you delete it during runtime?");
        }
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.name = translatable(filename, "name", processString("name", name, "", true));
        this.role = translatable(filename, "role", processString("role", role, "", true));
        this.profession = processEnum("profession", profession, Villager.Profession.NITWIT, Villager.Profession.class, true);
        this.location = processString("spawnLocation", location, null, true);
        this.locations = processStringList("spawnLocations", locations, null, false);
        this.greetings = translatable(filename, "greetings", processStringList("greetings", greetings, new ArrayList<>(), true));
        this.dialog = translatable(filename, "dialog", processStringList("dialog", dialog, new ArrayList<>(), true));
        this.farewell = translatable(filename, "farewell", processStringList("farewell", farewell, new ArrayList<>(), true));
        this.canTalk = processBoolean("canTalk", canTalk, true, true);
        this.activationRadius = processDouble("activationRadius", activationRadius, 3, true);
        this.interactionType = processEnum("interactionType", interactionType, NPCInteractions.NPCInteractionType.NONE, NPCInteractions.NPCInteractionType.class, true);
        this.timeout = processDouble("timeout", timeout, 0, false);
        this.noPreviousLocationMessage = translatable(filename, "noPreviousLocationMessage", processString("noPreviousLocationMessage", noPreviousLocationMessage, "", false));
        this.questFilenames = processStringList("questFileName", questFilenames, new ArrayList<>(), false);
        this.disguise = processString("disguise", disguise, null, false);
        this.customDisguiseData = processString("customDisguiseData", customDisguiseData, null, false);
        this.customModel = processString("customModel", customModel, null, false);
        this.arenaFilename = processString("arena", arenaFilename, null, false);
        this.command = processString("command", command, null, false);
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        this.fileConfiguration.set("isEnabled", enabled);
        try {
            ConfigurationEngine.fileSaverCustomValues(this.fileConfiguration, this.file);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to update the enabled status for an NPC with no config file! Did you delete it during runtime?");
        }
    }

    public void removeNPC(String locationString) {
        if (locations == null) return;
        locations.removeIf(entry -> entry.equals(locationString));
        this.fileConfiguration.set("spawnLocations", locations);
        try {
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, this.file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to update the location status for an NPC with no config file! Did you delete it during runtime?");
        }
    }

}
