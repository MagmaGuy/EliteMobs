package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCsConfigFields {

    private final String fileName;
    private boolean isEnabled;
    private final String name;
    private final String role;
    private final String profession;
    private String location;
    private final List<String> greetings;
    private final List<String> dialog;
    private final List<String> farewell;
    private final boolean canMove;
    private final boolean canTalk;
    private final double activationRadius;
    private final boolean canSleep;
    private final String interactionType;
    private FileConfiguration fileConfiguration = null;
    private File file;
    private double timeout;
    public String noPreviousLocationMessage;
    private final HashMap<String, Object> additionalConfigOptions = new HashMap<>();

    public NPCsConfigFields(String fileName,
                            boolean isEnabled,
                            String name,
                            String role,
                            String profession,
                            String location,
                            List<String> greetings,
                            List<String> dialog,
                            List<String> farewell,
                            boolean canMove,
                            boolean canTalk,
                            double activationRadius,
                            boolean canSleep,
                            String interactionType) {
        this.fileName = fileName;
        this.isEnabled = isEnabled;
        this.name = name;
        this.role = role;
        this.profession = profession;
        this.location = location;
        this.greetings = greetings;
        this.dialog = dialog;
        this.farewell = farewell;
        this.canMove = canMove;
        this.canTalk = canTalk;
        this.activationRadius = activationRadius;
        this.canSleep = canSleep;
        this.interactionType = interactionType;

    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("role", role);
        fileConfiguration.addDefault("profession", profession);
        fileConfiguration.addDefault("spawnLocation", location);
        fileConfiguration.addDefault("greetings", greetings);
        fileConfiguration.addDefault("dialog", dialog);
        fileConfiguration.addDefault("farewell", farewell);
        fileConfiguration.addDefault("canMove", canMove);
        fileConfiguration.addDefault("canTalk", canTalk);
        fileConfiguration.addDefault("activationRadius", activationRadius);
        fileConfiguration.addDefault("canSleep", canSleep);
        fileConfiguration.addDefault("interactionType", interactionType);
        if (!additionalConfigOptions.isEmpty())
            fileConfiguration.addDefaults(additionalConfigOptions);
    }

    public NPCsConfigFields(FileConfiguration fileConfiguration, File file) {
        this.fileConfiguration = fileConfiguration;
        this.file = file;
        this.fileName = file.getName();
        this.isEnabled = fileConfiguration.getBoolean("isEnabled");
        this.name = fileConfiguration.getString("name");
        this.role = fileConfiguration.getString("role");
        this.profession = fileConfiguration.getString("profession");
        this.location = fileConfiguration.getString("spawnLocation");
        this.greetings = fileConfiguration.getStringList("greetings");
        this.dialog = fileConfiguration.getStringList("dialog");
        this.farewell = fileConfiguration.getStringList("farewell");
        this.canMove = fileConfiguration.getBoolean("canMove");
        this.canTalk = fileConfiguration.getBoolean("canTalk");
        this.activationRadius = fileConfiguration.getDouble("activationRadius");
        this.canSleep = fileConfiguration.getBoolean("canSleep");
        this.interactionType = fileConfiguration.getString("interactionType");
        if (fileConfiguration.getString("timeout") != null)
            this.timeout = fileConfiguration.getDouble("timeout");
        else
            this.timeout = 0;
        if (fileConfiguration.getString("noPreviousLocationMessage") != null)
            this.noPreviousLocationMessage = fileConfiguration.getString("noPreviousLocationMessage");
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getProfession() {
        return profession;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getGreetings() {
        return greetings;
    }

    public List<String> getDialog() {
        return dialog;
    }

    public List<String> getFarewell() {
        return farewell;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public boolean isCanTalk() {
        return canTalk;
    }

    public double getActivationRadius() {
        return activationRadius;
    }

    public boolean isCanSleep() {
        return canSleep;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public File getFile() {
        return file;
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

    public void setLocation(String location) {
        this.location = location;
        this.fileConfiguration.set("spawnLocation", location);
        try {
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, this.file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to update the location status for an NPC with no config file! Did you delete it during runtime?");
        }
    }

    public Map<String, Object> getAdditionalConfigOptions() {
        return additionalConfigOptions;
    }

    public double getTimeout() {
        return this.timeout;
    }

}
