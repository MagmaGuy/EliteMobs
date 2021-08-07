package com.magmaguy.elitemobs.config.customevents;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CustomEventsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    private CustomEvent.EventType eventType = CustomEvent.EventType.DEFAULT;
    private List<String> bossFilenames = new ArrayList<>();
    private double localCooldown = 0;
    private double globalCooldown = 0;
    private double weight = 0;
    private String startMessage;
    private String endMessage;
    private List<String> eventStartCommands;
    private List<String> eventEndCommands;
    private int announcementPriority = 0;
    private double chance = 0;
    private List<Material> breakableMaterials;
    private double eventDuration = 0;
    private int eventEndTime = -1;
    private boolean endEventWithBossDeath = true;
    private String customSpawn = "";
    private int minimumPlayerCount = 1;

    /**
     * For Action Events
     *
     * @param filename
     * @param isEnabled
     */
    public CustomEventsConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    public CustomEvent.EventType getEventType() {
        return eventType;
    }

    public void setEventType(CustomEvent.EventType eventType) {
        this.eventType = eventType;
    }

    public List<String> getBossFilenames() {
        return bossFilenames;
    }

    public void setBossFilenames(List<String> bossFilenames) {
        this.bossFilenames = bossFilenames;
    }

    public double getLocalCooldown() {
        return localCooldown;
    }

    public void setLocalCooldown(double localCooldown) {
        this.localCooldown = localCooldown;
    }

    public double getGlobalCooldown() {
        return globalCooldown;
    }

    public void setGlobalCooldown(double globalCooldown) {
        this.globalCooldown = globalCooldown;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public void generateConfigDefaults() {
        fileConfiguration.addDefault("isEnabled", isEnabled);
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
        if (localCooldown != 0) fileConfiguration.addDefault("localCooldown", localCooldown);
        if (globalCooldown != 0) fileConfiguration.addDefault("globalCooldown", globalCooldown);
        if (weight != 0) fileConfiguration.addDefault("weight", weight);
        if (eventDuration != 0) fileConfiguration.addDefault("eventDuration", eventDuration);
        if (eventEndTime != -1) fileConfiguration.addDefault("eventEndTime", eventEndTime);
        if (!endEventWithBossDeath) fileConfiguration.addDefault("endEventWithBossDeath", endEventWithBossDeath);
        if (!customSpawn.isEmpty()) fileConfiguration.addDefault("spawnType", customSpawn);
        if (minimumPlayerCount != 0) fileConfiguration.addDefault("minimumPlayerCount", minimumPlayerCount);
    }

    @Override
    public void processConfigFields() {
        this.eventType = processEnum("eventType", eventType);
        if (eventType == CustomEvent.EventType.DEFAULT) {
            new WarningMessage("Failed to determine a valid event type for " + filename + " ! This event will not be registered.");
            return;
        }
        this.bossFilenames = processStringList("bossFilenames", bossFilenames);
        if (bossFilenames == null) return;
        this.startMessage = processString("startMessage", startMessage);
        this.endMessage = processString("endMessage", endMessage);
        this.eventStartCommands = processStringList("eventStartCommands", eventStartCommands);
        this.eventEndCommands = processStringList("eventEndCommands", eventEndCommands);
        this.announcementPriority = processInt("announcementPriority", announcementPriority);
        this.chance = processDouble("chance", chance);
        this.breakableMaterials = processEnumList("breakableMaterials", breakableMaterials, Material.class);
        this.localCooldown = processDouble("localCooldown", localCooldown);
        this.globalCooldown = processDouble("globalCooldown", globalCooldown);
        this.weight = processDouble("weight", weight);
        this.eventDuration = processDouble("eventDuration", eventDuration);
        this.eventEndTime = processInt("eventEndTime", eventEndTime);
        this.endEventWithBossDeath = processBoolean("endEventWithBossDeath", endEventWithBossDeath);
        this.customSpawn = processString("spawnType", customSpawn);
        this.minimumPlayerCount = processInt("minimumPlayerCount", minimumPlayerCount);
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    public String getEndMessage() {
        return endMessage;
    }

    public void setEndMessage(String endMessage) {
        this.endMessage = endMessage;
    }

    public List<String> getEventStartCommands() {
        return eventStartCommands;
    }

    public void setEventStartCommands(List<String> eventStartCommands) {
        this.eventStartCommands = eventStartCommands;
    }

    public List<String> getEventEndCommands() {
        return eventEndCommands;
    }

    public void setEventEndCommands(List<String> eventEndCommands) {
        this.eventEndCommands = eventEndCommands;
    }

    public int getAnnouncementPriority() {
        return announcementPriority;
    }

    public void setAnnouncementPriority(int announcementPriority) {
        this.announcementPriority = announcementPriority;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public List<Material> getBreakableMaterials() {
        return breakableMaterials;
    }

    public void setBreakableMaterials(List<Material> breakableMaterials) {
        this.breakableMaterials = breakableMaterials;
    }

    public double getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(double eventDuration) {
        this.eventDuration = eventDuration;
    }

    public double getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(int eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public boolean isEndEventWithBossDeath() {
        return endEventWithBossDeath;
    }

    public void setEndEventWithBossDeath(boolean endEventWithBossDeath) {
        this.endEventWithBossDeath = endEventWithBossDeath;
    }

    public String getCustomSpawn() {
        return customSpawn;
    }

    public void setCustomSpawn(String customSpawn) {
        this.customSpawn = customSpawn;
    }

    public int getMinimumPlayerCount() {
        return minimumPlayerCount;
    }

    public void setMinimumPlayerCount(int minimumPlayerCount) {
        this.minimumPlayerCount = minimumPlayerCount;
    }

}
