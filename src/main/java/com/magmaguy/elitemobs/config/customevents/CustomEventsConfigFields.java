package com.magmaguy.elitemobs.config.customevents;

import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Material;
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

    public CustomEventsConfigFields(String filename, boolean isEnabled, CustomEvent.EventType eventType, List<String> bossFilenames) {
        this.filename = filename + ".yml";
        this.enabled = isEnabled;
        this.eventType = eventType;
        this.bossFilenames = bossFilenames;
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

    private FileConfiguration fileConfiguration;

    public CustomEventsConfigFields(FileConfiguration fileConfiguration, File file) {
        this.filename = file.getName();
        this.fileConfiguration = fileConfiguration;
        if (configHas("isEnabled"))
            this.enabled = fileConfiguration.getBoolean("isEnabled");
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
        if (configHas("bossFilenames")) {
            try {
                this.bossFilenames = fileConfiguration.getStringList("bossFilenames");
            } catch (Exception ex) {
                new WarningMessage("Custom event file " + filename + " has an invalid custom bosses list! Fix it for the event to work!");
                return;
            }
        } else {
            new WarningMessage("Custom event file " + filename + " is missing a valid bossFilenames entry! Fix it for the event to be registered!");
            return;
        }

        this.startMessage = processString("startMessage");
        this.endMessage = processString("endMessage");
        this.eventStartCommands = processStringList("eventStartCommands");
        this.eventEndCommands = processStringList("eventEndCommands");
        this.announcementPriority = processInt("announcementPriority");
        this.chance = processDouble("chance");
        this.breakableMaterials = processBreakables("breakableMaterials");
    }

    private boolean configHas(String configKey) {
        return fileConfiguration.contains(configKey);
    }

    private String processString(String path) {
        try {
            return fileConfiguration.getString(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return null;
    }

    private List<String> processStringList(String path) {
        try {
            return fileConfiguration.getStringList(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return null;
    }

    private int processInt(String path) {
        try {
            return fileConfiguration.getInt(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return 0;
    }

    private double processDouble(String path) {
        try {
            return fileConfiguration.getDouble(path);
        } catch (Exception ex) {
            new WarningMessage("Custom event file " + filename + " has an incorrect entry for " + path);
        }
        return 0;
    }

    private List<Material> processBreakables(String path) {
        List<String> unprocessedBreakables = processStringList(path);
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

}
