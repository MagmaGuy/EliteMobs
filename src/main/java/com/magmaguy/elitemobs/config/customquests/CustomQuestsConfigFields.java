package com.magmaguy.elitemobs.config.customquests;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    @Getter
    @Setter
    private List<String> customObjectivesList = new ArrayList<>();
    @Getter
    @Setter
    private List<String> customRewardsList = new ArrayList<>();
    @Getter
    @Setter
    //Required permission to accept quest
    private String questAcceptPermission = "";
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
    private boolean customQuestFormat = false;
    @Getter
    @Setter
    private boolean trackable = true;

    public CustomQuestsConfigFields(String filename,
                                    boolean isEnabled,
                                    List<String> customQuestObjective,
                                    List<String> customQuestReward,
                                    int questLevel,
                                    String questName,
                                    List<String> questLore) {
        super(filename, isEnabled);
        this.customObjectivesList = customQuestObjective;
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
        this.customObjectivesList = processStringList("customObjectives", customObjectivesList, new ArrayList<>(), true);
        this.customRewardsList = processStringList("customRewards", customRewardsList, new ArrayList<>(), true);
        this.questAcceptPermission = processString("questAcceptPermission", questAcceptPermission, null, false);
        this.questLockoutPermission = processString("questLockoutPermission", questLockoutPermission, null, false);
        this.questLockoutMinutes = processInt("questLockoutMinutes", questLockoutMinutes, -1, false);
        this.questName = processString("name", questName, filename, true);
        this.questLore = processStringList("questLore", questLore, new ArrayList<>(), false);
        this.temporaryPermissions = processStringList("temporaryPermissions", temporaryPermissions, new ArrayList<>(), false);
        this.questAcceptDialog = processStringList("questAcceptDialog", questAcceptDialog, new ArrayList<>(), false);
        this.questCompleteDialog = processStringList("questCompleteMessage", questCompleteDialog, new ArrayList<>(), false);
        this.questCompleteCommands = processStringList("questCompleteCommands", questCompleteCommands, new ArrayList<>(), false);
        this.turnInNPC = processString("turnInNPC", turnInNPC, "", false);
        this.customQuestFormat = processBoolean("customQuestFormat", customQuestFormat, false, false);
        this.trackable = processBoolean("trackable", trackable, true, false);
    }


}
