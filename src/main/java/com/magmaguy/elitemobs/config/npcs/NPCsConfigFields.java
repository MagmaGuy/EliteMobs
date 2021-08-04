package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;

import java.util.List;

public class NPCsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    public String noPreviousLocationMessage;
    private String name;
    private String role;
    private Villager.Profession profession = Villager.Profession.NITWIT;
    private String location;
    private List<String> greetings;
    private List<String> dialog;
    private List<String> farewell;
    private boolean canTalk;
    private double activationRadius;
    private NPCInteractions.NPCInteractionType interactionType;
    private double timeout;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    public void setProfession(Villager.Profession profession) {
        this.profession = profession;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.fileConfiguration.set("spawnLocation", location);
        try {
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, this.file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to update the location status for an NPC with no config file! Did you delete it during runtime?");
        }
    }

    public List<String> getGreetings() {
        return greetings;
    }

    public void setGreetings(List<String> greetings) {
        this.greetings = greetings;
    }

    public List<String> getDialog() {
        return dialog;
    }

    public void setDialog(List<String> dialog) {
        this.dialog = dialog;
    }

    public List<String> getFarewell() {
        return farewell;
    }

    public void setFarewell(List<String> farewell) {
        this.farewell = farewell;
    }

    public boolean isCanTalk() {
        return canTalk;
    }

    public void setCanTalk(boolean canTalk) {
        this.canTalk = canTalk;
    }

    public double getActivationRadius() {
        return activationRadius;
    }

    public void setActivationRadius(double activationRadius) {
        this.activationRadius = activationRadius;
    }

    public NPCInteractions.NPCInteractionType getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(NPCInteractions.NPCInteractionType interactionType) {
        this.interactionType = interactionType;
    }

    public double getTimeout() {
        return timeout;
    }

    public void setTimeout(double timeout) {
        this.timeout = timeout;
    }

    public String getNoPreviousLocationMessage() {
        return noPreviousLocationMessage;
    }

    public void setNoPreviousLocationMessage(String noPreviousLocationMessage) {
        this.noPreviousLocationMessage = noPreviousLocationMessage;
    }

    @Override
    public void generateConfigDefaults() {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("role", role);
        fileConfiguration.addDefault("profession", profession.toString());
        fileConfiguration.addDefault("spawnLocation", location);
        fileConfiguration.addDefault("greetings", greetings);
        fileConfiguration.addDefault("dialog", dialog);
        fileConfiguration.addDefault("farewell", farewell);
        fileConfiguration.addDefault("canTalk", canTalk);
        fileConfiguration.addDefault("activationRadius", activationRadius);
        fileConfiguration.addDefault("interactionType", interactionType.toString());
    }

    @Override
    public void processConfigFields() {
        this.name = processString("name", name);
        this.role = processString("role", role);
        this.profession = processEnum("profession", profession);
        this.location = processString("spawnLocation", location);
        this.greetings = processStringList("greetings", greetings);
        this.dialog = processStringList("dialog", dialog);
        this.farewell = processStringList("farewell", farewell);
        this.canTalk = processBoolean("canTalk", canTalk);
        this.activationRadius = processDouble("activationRadius", activationRadius);
        this.interactionType = processEnum("interactionType", interactionType);
        this.timeout = processDouble("timeout", timeout);
        this.noPreviousLocationMessage = processString("noPreviousLocationMessage", noPreviousLocationMessage);
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

}
