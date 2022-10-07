package com.magmaguy.elitemobs.config.customquests;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class CustomQuestsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    @Getter
    @Setter
    List<String> temporaryPermissions = new ArrayList<>();
    @Getter
    @Setter
    List<String> questAcceptDialog = new ArrayList<>();
    @Getter
    @Setter
    private int questLevel = 0;
    //@Getter
    //@Setter
    //private List<String> customObjectivesList = new ArrayList<>();
    @Getter
    @Setter
    protected Map<String, Map<String, Object>> customObjectives = new HashMap();
    @Getter
    @Setter
    private List<String> customRewardsList = new ArrayList<>();
    @Getter
    @Setter
    //Required permission to accept quest
    private String questAcceptPermission = "";
    @Getter
    @Setter
    private List<String> questAcceptPermissions = null;
    @Getter
    //Permission which locks players out of a quest
    private String questLockoutPermission = "";
    @Getter
    @Setter
    private int questLockoutMinutes = -1;
    @Getter
    @Setter
    private String questName = "";
    @Getter
    @Setter
    private List<String> questLore = new ArrayList<>();
    @Getter
    @Setter
    private List<String> questCompleteDialog = new ArrayList<>();
    @Getter
    @Setter
    //placeholders: $player, $getX, $getY, $getZ
    private List<String> questCompleteCommands = new ArrayList<>();
    @Getter
    @Setter
    private String turnInNPC = "";
    @Getter
    @Setter
    private boolean trackable = true;

    public CustomQuestsConfigFields(String filename,
                                    boolean isEnabled,
                                    Map<String, Map<String, Object>> customQuestObjective,
                                    List<String> customQuestReward,
                                    int questLevel,
                                    String questName,
                                    List<String> questLore) {
        super(filename, isEnabled);
        this.customObjectives = customQuestObjective;
        this.customRewardsList = customQuestReward;
        this.questLevel = questLevel;
        this.questName = questName;
        this.questLore = questLore;
    }

    /**
     * Used by plugin-generated files (defaults)
     *
     * @param filename
     * @param isEnabled
     */
    public CustomQuestsConfigFields(String filename,
                                    boolean isEnabled) {
        super(filename, isEnabled);
    }

    public void setQuestLockoutPermission() {
        this.questLockoutPermission = "elitequest." + getFilename();
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        //this.customObjectivesList = translatable(filename, "customObjectives", processStringList("customObjectives", customObjectivesList, new ArrayList<>(), true));
        //todo update format
        if (fileConfiguration.contains("customObjectives") && fileConfiguration.get("customObjectives") instanceof List)
            updateOldStringFormat(fileConfiguration.getStringList("customObjectives"));
        this.customObjectives = processQuestObjectives();
        this.customRewardsList = processStringList("customRewards", customRewardsList, new ArrayList<>(), true);
        this.questAcceptPermission = processString("questAcceptPermission", questAcceptPermission, null, false);
        this.questAcceptPermissions = processStringList("questAcceptPermissions", questAcceptPermissions, null, false);
        this.questLockoutPermission = processString("questLockoutPermission", questLockoutPermission, null, false);
        this.questLockoutMinutes = processInt("questLockoutMinutes", questLockoutMinutes, -1, false);
        this.questName = translatable(filename, "name", processString("name", questName, filename, true));
        this.questLore = translatable(filename, "questLore", processStringList("questLore", questLore, new ArrayList<>(), false));
        this.temporaryPermissions = processStringList("temporaryPermissions", temporaryPermissions, new ArrayList<>(), false);
        this.questAcceptDialog = translatable(filename, "questAcceptDialog", processStringList("questAcceptDialog", questAcceptDialog, new ArrayList<>(), false));
        this.questCompleteDialog = translatable(filename, "questCompleteMessage", processStringList("questCompleteMessage", questCompleteDialog, new ArrayList<>(), false));
        this.questCompleteCommands = processStringList("questCompleteCommands", questCompleteCommands, new ArrayList<>(), false);
        this.turnInNPC = processString("turnInNPC", turnInNPC, "", false);
        this.trackable = processBoolean("trackable", trackable, true, false);
        this.questLevel = processInt("questLevel", questLevel, 0, false);
    }

    private void updateOldStringFormat(List<String> oldList) {
        Map<String, Map<String, Object>> parsedObjectives = new HashMap<>();
        int counter = 0;
        for (String questObjectiveEntry : oldList) {
            counter++;
            Map<String, Object> parsedEntry = new HashMap<>();
            String[] individualEntries = questObjectiveEntry.split(":");
            for (String keyAndValue : individualEntries) {
                String[] keyAndValueArray = keyAndValue.split("=");
                String key = keyAndValueArray[0];
                Object value = null;
                if (keyAndValueArray.length > 1)
                    value = keyAndValueArray[1];
                if (value == null)
                    switch (key.toUpperCase()) {
                        case "KILL_CUSTOM", "FETCH_ITEM", "DIALOG", "ARENA" -> {
                            value = key;
                            key = "objectiveType";
                        }
                        default ->
                                new WarningMessage("Failed to correctly parse key " + key + " in " + filename + " while updating the old quest configuration format!");
                    }
                if (key.toLowerCase().equals("dialog")) {
                    value = Arrays.stream(((String) value).split("\\n")).toList();
                }
                parsedEntry.put(key, value);
                new InfoMessage("Converted quest old entry to " + key + ": " + value);
            }
            parsedObjectives.put("Objective" + counter, parsedEntry);
        }
        fileConfiguration.set("customObjectives", parsedObjectives);
        customObjectives = parsedObjectives;
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            new WarningMessage("Failed to save new custom objective format!");
        }
    }

    private Map<String, Map<String, Object>> processQuestObjectives() {
        Map<String, Object> rawMap;

        //write defaults
        if (fileConfiguration.getConfigurationSection("customObjectives") == null && !customObjectives.isEmpty()) {
            fileConfiguration.set("customObjectives", customObjectives);
            rawMap = new HashMap<>(customObjectives);

            Map<String, Map<String, Object>> parsedObjectives = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> maps : customObjectives.entrySet()) {
                Map<String, Object> parsedMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : maps.getValue().entrySet())
                    if (entry.getKey().equals("dialog"))
                        parsedMap.put(entry.getKey(), translatable(filename, "customObjectives." + maps.getKey() + "." + (String) entry.getKey(), (List<String>) entry.getValue()));
                    else
                        parsedMap.put(entry.getKey(), entry.getValue());
                parsedObjectives.put(maps.getKey(), parsedMap);
            }
            return parsedObjectives;

        } else
            rawMap = fileConfiguration.getConfigurationSection("customObjectives").getValues(false);

        if (rawMap == null) {
            new WarningMessage("Failed to parse custom objectives for " + filename);
            return new HashMap<>();
        }
        //Parse for the specific translatable elements
        Map<String, Map<String, Object>> parsedObjectives = new HashMap<>();
        for (Map.Entry<String, Object> maps : rawMap.entrySet()) {
            Map<String, Object> parsedMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((ConfigurationSection) maps.getValue()).getValues(false).entrySet())
                if (entry.getKey().equals("dialog"))
                    parsedMap.put((String) entry.getKey(), translatable(filename, "customObjectives." + maps.getKey() + "." + (String) entry.getKey(), (List<String>) entry.getValue()));
                else
                    parsedMap.put((String) entry.getKey(), entry.getValue());
            parsedObjectives.put(maps.getKey(), parsedMap);
        }
        return parsedObjectives;
    }

}
