package com.magmaguy.elitemobs.config.customevents;

import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomEventsConfigFields {

    public String getFilename() {
        return filename;
    }

    private String filename;

    public boolean isEnabled() {
        return enabled;
    }

    private boolean enabled = true;

    public CustomEvent.EventType getEventType() {
        return eventType;
    }

    CustomEvent.EventType eventType;

    public List<String> getBossFilenames() {
        return bossFilenames;
    }

    List<String> bossFilenames;

    /**
     * For Action Events
     *
     * @param filename
     * @param isEnabled
     * @param eventType
     * @param bossFilenames
     */
    public CustomEventsConfigFields(String filename,
                                    boolean isEnabled,
                                    CustomEvent.EventType eventType,
                                    List<String> bossFilenames) {
        this.filename = filename + ".yml";
        this.enabled = isEnabled;
        this.eventType = eventType;
        this.bossFilenames = bossFilenames;
    }

    public double getLocalCooldown() {
        return localCooldown;
    }

    private double localCooldown = 0;

    public double getGlobalCooldown() {
        return globalCooldown;
    }

    private double globalCooldown = 0;

    public double getWeight() {
        return weight;
    }

    private double weight = 0;

    /**
     * For Timed Events
     *
     * @param filename
     * @param isEnabled
     * @param eventType
     * @param bossFilenames
     */
    public CustomEventsConfigFields(String filename,
                                    boolean isEnabled,
                                    CustomEvent.EventType eventType,
                                    List<String> bossFilenames,
                                    double localCooldown,
                                    double globalCooldown,
                                    double weight,
                                    TimedEvent.SpawnType spawnType) {
        this.filename = filename + ".yml";
        this.enabled = isEnabled;
        this.eventType = eventType;
        this.bossFilenames = bossFilenames;
        this.localCooldown = localCooldown;
        this.globalCooldown = globalCooldown;
        this.weight = weight;
        this.spawnType = spawnType;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", enabled);
        fileConfiguration.addDefault("eventType", eventType.toString());
        fileConfiguration.addDefault("bossFilenames", bossFilenames);
        if (startMessage != null) fileConfiguration.addDefault("startMessage", startMessage);
        if (endMessage != null) fileConfiguration.addDefault("endMessage", endMessage);
        if (eventStartCommands != null) fileConfiguration.addDefault("eventStartCommands", eventStartCommands);
        if (eventEndCommands != null) fileConfiguration.addDefault("eventEndCommands", eventEndCommands);
        if (announcementPriority != 0) fileConfiguration.addDefault("announcementPriority", announcementPriority);
        if (chance != 0) fileConfiguration.addDefault("chance", chance);
        if (breakableMaterials != null) {
            List<String> convertedList = new ArrayList<>();
            breakableMaterials.forEach(material -> convertedList.add(material.toString()));
            fileConfiguration.addDefault("breakableMaterials", convertedList);
        }
        if (localCooldown != 0)
            fileConfiguration.addDefault("localCooldown", localCooldown);
        if (globalCooldown != 0)
            fileConfiguration.addDefault("globalCooldown", globalCooldown);
        if (weight != 0)
            fileConfiguration.addDefault("weight", weight);
        if (eventDuration != 0)
            fileConfiguration.addDefault("eventDuration", eventDuration);
        if (!endEventWithBossDeath)
            fileConfiguration.addDefault("endEventWithBossDeath", endEventWithBossDeath);
        if (spawnType != TimedEvent.SpawnType.INSTANT_SPAWN)
            fileConfiguration.addDefault("spawnType", spawnType.toString());

        if (minimumPlayerCount != 0)
            fileConfiguration.addDefault("minimumPlayerCount", minimumPlayerCount);
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    private String startMessage;

    public String getEndMessage() {
        return endMessage;
    }

    public void setEndMessage(String endMessage) {
        this.endMessage = endMessage;
    }

    private String endMessage;

    public List<String> getEventStartCommands() {
        return eventStartCommands;
    }

    public void setEventStartCommands(List<String> eventStartCommands) {
        this.eventStartCommands = eventStartCommands;
    }

    private List<String> eventStartCommands;

    public List<String> getEventEndCommands() {
        return eventEndCommands;
    }

    public void setEventEndCommands(List<String> eventEndCommands) {
        this.eventEndCommands = eventEndCommands;
    }

    private List<String> eventEndCommands;

    public int getAnnouncementPriority() {
        return announcementPriority;
    }

    public void setAnnouncementPriority(int announcementPriority) {
        this.announcementPriority = announcementPriority;
    }

    private int announcementPriority = 0;

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    private double chance = 0;

    public List<Material> getBreakableMaterials() {
        return breakableMaterials;
    }

    public void setBreakableMaterials(List<Material> breakableMaterials) {
        this.breakableMaterials = breakableMaterials;
    }

    private List<Material> breakableMaterials;

    public double getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(double eventDuration) {
        this.eventDuration = eventDuration;
    }

    private double eventDuration = 0;

    public boolean isEndEventWithBossDeath() {
        return endEventWithBossDeath;
    }

    public void setEndEventWithBossDeath(boolean endEventWithBossDeath) {
        this.endEventWithBossDeath = endEventWithBossDeath;
    }

    private boolean endEventWithBossDeath = true;

    public TimedEvent.SpawnType getSpawnType() {
        return spawnType;
    }

    public void setSpawnType(TimedEvent.SpawnType spawnType) {
        this.spawnType = spawnType;
    }

    TimedEvent.SpawnType spawnType = TimedEvent.SpawnType.INSTANT_SPAWN;


    public int getMinimumPlayerCount() {
        return minimumPlayerCount;
    }

    public void setMinimumPlayerCount(int minimumPlayerCount) {
        this.minimumPlayerCount = minimumPlayerCount;
    }

    private int minimumPlayerCount = 0;


    public void setFilename(String filename) {
        this.filename = filename;
    }


    private FileConfiguration fileConfiguration;

    public CustomEventsConfigFields(FileConfiguration fileConfiguration, File file) {
        this.filename = file.getName();
        this.fileConfiguration = fileConfiguration;
        this.enabled = processBoolean("isEnabled", enabled);
        if (configHas("eventType")) {
            try {
                this.eventType = CustomEvent.EventType.valueOf(fileConfiguration.getString("eventType"));
            } catch (Exception ex) {
                new WarningMessage("Tried to parse an eventType for custom event file " + filename + " which was not valid! Valid types:");
                for (CustomEvent.EventType eventTypes : CustomEvent.EventType.values())
                    new WarningMessage(eventTypes.toString());
                new WarningMessage("This event will not be registered.");
                return;
            }
        } else {
            new WarningMessage("Custom event file " + filename + " is missing a valid eventType entry! Fix it for the event to be registered!");
            return;
        }
        this.bossFilenames = processStringList("bossFilenames", bossFilenames);
        if (this.bossFilenames == null) {
            new WarningMessage("Custom event file " + filename + " is missing a valid bossFilenames entry! Fix it for the event to be registered!");
            return;
        }

        this.startMessage = processString("startMessage", startMessage);
        this.endMessage = processString("endMessage", endMessage);
        this.eventStartCommands = processStringList("eventStartCommands", eventStartCommands);
        this.eventEndCommands = processStringList("eventEndCommands", eventEndCommands);
        this.announcementPriority = processInt("announcementPriority", announcementPriority);
        this.chance = processDouble("chance", chance);
        this.breakableMaterials = processBreakables("breakableMaterials");
        this.localCooldown = processDouble("localCooldown", localCooldown);
        this.globalCooldown = processDouble("globalCooldown", globalCooldown);
        this.weight = processDouble("weight", weight);
        this.eventDuration = processDouble("eventDuration", eventDuration);
        this.endEventWithBossDeath = processBoolean("endEventWithBossDeath", endEventWithBossDeath);
        this.spawnType = processSpawnType("spawnType", spawnType);



        this.minimumPlayerCount = processInt("minimumPlayerCount", minimumPlayerCount);
    }

    private boolean configHas(String configKey) {
        return fileConfiguration.contains(configKey);
    }

    private String processString(String path, String pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getString(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return null;
    }

    private List<String> processStringList(String path, List<String> pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getStringList(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return null;
    }

    private int processInt(String path, int pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getInt(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return 0;
    }

    private long processLong(String path, long pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getInt(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return 0;
    }


    private double processDouble(String path, double pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getDouble(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return 0;
    }

    private boolean processBoolean(String path, boolean pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getBoolean(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return true;
    }

    private List<Material> processBreakables(String path) {
        List<String> unprocessedBreakables = processStringList(path, null);
        if (unprocessedBreakables == null && eventType.equals(CustomEvent.EventType.BREAK_BLOCK)) {
            new WarningMessage("Custom event file " + filename + " has an incorrect or missing entry for " + path + " which is required for BREAK_BLOCK event types!");
        }
        try {
            List<Material> processedBreakables = new ArrayList<>();
            for (String string : unprocessedBreakables)
                processedBreakables.add(Material.valueOf(string));
            return processedBreakables;
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an invalid material entry in " + path + " !");
            return null;
        }
    }

    private TimedEvent.SpawnType processSpawnType(String path, TimedEvent.SpawnType pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return TimedEvent.SpawnType.valueOf(fileConfiguration.getString(path));
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

}
