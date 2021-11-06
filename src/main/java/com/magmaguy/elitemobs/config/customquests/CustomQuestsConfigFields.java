package com.magmaguy.elitemobs.config.customquests;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.quests.CustomQuestReward;
import com.magmaguy.elitemobs.quests.objectives.CustomKillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomQuestsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface, Serializable {

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
    private String permission = "";
    @Getter
    @Setter
    private String questName = "";
    @Getter
    @Setter
    private String questLore = "";
    @Getter
    @Setter
    private String questAcceptMessage;
    @Getter
    @Setter
    private String questCompleteMessage;


    public CustomQuestsConfigFields(String filename,
                                    boolean isEnabled,
                                    List<String> customQuestObjective,
                                    List<String> customQuestReward,
                                    int questLevel,
                                    String questName,
                                    String questLore) {
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

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.customObjectivesList = processStringList("customObjectives", customObjectivesList, new ArrayList<>(), true);
        this.customRewardsList = processStringList("customRewards", customRewardsList, new ArrayList<>(), true);
        this.permission = processString("permission", permission, null, false);
        this.questName = processString("name", questName, filename, true);
        this.questLore = processString("questLore", questLore, "", false);
        this.questAcceptMessage = processString("acceptMessage", questAcceptMessage, null, false);
        this.questCompleteMessage = processString("questCompleteMessage", questCompleteMessage, null, false);
    }




}
