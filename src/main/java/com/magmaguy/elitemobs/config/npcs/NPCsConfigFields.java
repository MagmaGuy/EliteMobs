package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolRoute;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.VersionChecker;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class NPCsConfigFields extends CustomConfigFields {

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
    private double nameplateScale = 1;
    @Getter
    private double nameplateLineGap = 0.1;
    @Setter
    private Villager.Profession profession = null;
    @Getter
    private String spawnLocation;
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
    private String classRoot;
    @Getter
    @Setter
    private List<String> locations = new ArrayList<>();
    @Getter
    @Setter
    private String command;
    @Getter
    @Setter
    private boolean instanced = false;
    @Getter
    @Setter
    private double scale = 1;
    @Getter
    @Setter
    private boolean syncMovement = true;
    @Getter
    @Setter
    private List<String> scripts = new ArrayList<>();
    @Getter
    private List<String> transportRoutes = new ArrayList<>();
    @Getter
    private PatrolRoute patrolRoute;
    /** Zero disables proximity pauses. Independent of chatter and manual patrol holds. */
    @Getter
    @Setter
    private double patrolPauseNearPlayersRadius;

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
        this.spawnLocation = location;
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

    public boolean addSpawnLocation(String spawnLocation) {
        if (spawnLocation == null || spawnLocation.isEmpty()) return false;
        if (this.locations == null) this.locations = new ArrayList<>();
        if (this.locations.contains(spawnLocation)) return false;
        this.locations.add(spawnLocation);
        saveSpawnLocations();
        return true;
    }

    public void setSpawnLocation(String spawnLocation) {
        addSpawnLocation(spawnLocation);
    }

    private void saveSpawnLocations() {
        try {
            this.getWritableFileConfiguration().set("spawnLocations", locations);
            ConfigurationEngine.fileSaverCustomValues(getWritableFileConfiguration(), this.file);
        } catch (Exception ex) {
            Logger.warn("Attempted to update the location status for an NPC with no config file! Did you delete it during runtime?");
        }
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.name = translatable(filename, "name", processString("name", name, "", true));
        this.role = translatable(filename, "role", processString("role", role, "", true));
        nameplateScale = processDouble("nameplateScale", nameplateScale, 1D, true);
        nameplateLineGap = processDouble("nameplateLineGap", nameplateLineGap, 0.1D, true);
        if (!Double.isFinite(nameplateScale) || nameplateScale <= 0 || nameplateScale > Float.MAX_VALUE)
            nameplateScale = 1;
        if (!Double.isFinite(nameplateLineGap) || nameplateLineGap < 0) nameplateLineGap = 0.1;
        try {
            if (Bukkit.getServer() == null) {
                processString("profession", professionConfigName(), "nitwit", false);
            } else if (VersionChecker.serverVersionOlderThan(21, 4))
                this.profession = Villager.Profession.valueOf(processString("profession", professionConfigName(), "NITWIT", false).toUpperCase(Locale.ROOT));
            else {
                String professionString = processString("profession", professionConfigName(), "nitwit", false);
                for (Villager.Profession value : Villager.Profession.values()) {
                    if (value.getKey().getKey().toLowerCase(Locale.ROOT).equals(professionString.toLowerCase(Locale.ROOT))) {
                        this.profession = value;
                        break;
                    }
                }
            }
        } catch (IncompatibleClassChangeError e) {
            //The early 1.21 API still used the profession enum, which was later dropped. This works for later releases, but not the early ones.
        } catch (Exception e) {
            Logger.warn("NPC in configuration file " + filename + " has an invalid profession!");
            e.printStackTrace();
        }
        this.spawnLocation = processString("spawnLocation", spawnLocation, null, true);
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
        this.classRoot = processString("classRoot", classRoot, null, false);
        this.command = processString("command", command, null, false);
        this.instanced = processBoolean("instanced", instanced, false, false);
        this.scale = processDouble("scale", scale, 1, false);
        this.syncMovement = processBoolean("syncMovement", syncMovement, false, true);
        this.scripts = processStringList("scripts", scripts, new ArrayList<>(), false);
        this.transportRoutes = processStringList("transportRoutes", transportRoutes, new ArrayList<>(), false);
        patrolPauseNearPlayersRadius = processDouble("patrol.pauseNearPlayersRadius", patrolPauseNearPlayersRadius, 0D, false);
        if (!Double.isFinite(patrolPauseNearPlayersRadius) || patrolPauseNearPlayersRadius < 0D) {
            Logger.warn("Invalid patrol.pauseNearPlayersRadius in " + filename + ": expected a finite, non-negative radius. Disabling proximity pauses.");
            patrolPauseNearPlayersRadius = 0D;
        }
        try {
            this.patrolRoute = PatrolRoute.parse(fileConfiguration);
        } catch (IllegalArgumentException exception) {
            this.patrolRoute = null;
            Logger.warn("Invalid patrol in " + filename + ": " + exception.getMessage());
        }
    }

    public Villager.Profession getProfession() {
        if (profession == null) return Villager.Profession.NITWIT;
        return profession;
    }

    private String professionConfigName() {
        if (profession == null) return "nitwit";
        return ((Keyed) profession).getKey().getKey();
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        this.getWritableFileConfiguration().set("isEnabled", enabled);
        try {
            ConfigurationEngine.fileSaverCustomValues(this.getWritableFileConfiguration(), this.file);
        } catch (Exception e) {
            Logger.warn("Attempted to update the enabled status for an NPC with no config file! Did you delete it during runtime?");
        }
    }

    public void removeNPC(String locationString) {
        if (locations == null) return;
        locations.removeIf(entry -> Objects.equals(entry, locationString));
        saveSpawnLocations();
    }

    public boolean reloadPatrolRoute() {
        try {
            patrolRoute = PatrolRoute.parse(getWritableFileConfiguration());
            return patrolRoute != null;
        } catch (IllegalArgumentException exception) {
            patrolRoute = null;
            Logger.warn("Invalid patrol in " + filename + ": " + exception.getMessage());
            return false;
        }
    }

}
