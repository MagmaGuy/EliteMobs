package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestObjectivesCompletedEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.CustomQuestReward;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomQuestObjectives implements Serializable {

    @Getter
    @Setter
    protected CustomQuestReward customQuestReward;
    @Getter
    @Setter
    protected List<Objective> objectives;
    //The CustomQuest this objective belongs to
    @Getter
    private CustomQuest customQuest;
    private boolean isOver = false;

    public CustomQuestObjectives(CustomQuest customQuest, CustomQuestReward customQuestReward) {
        this.customQuest = customQuest;
        this.customQuestReward = customQuestReward;
        this.objectives = processCustomObjectives();
    }

    /**
     * Returns whether all the objectives have been cleared, meaning that the quest is over
     *
     * @return
     */
    public boolean isOver() {
        boolean over = true;
        for (Objective customQuestObjective : objectives)
            if (!customQuestObjective.isObjectiveCompleted()) {
                over = false;
                break;
            }
        return over;
    }

    public void updateQuestStatus(UUID playerUUID) {
        if (isOver) return;
        if (!isOver()) return;
        isOver = true;
        QuestObjectivesCompletedEvent questObjectivesCompletedEvent = new QuestObjectivesCompletedEvent(Bukkit.getPlayer(playerUUID), customQuest);
        new EventCaller(questObjectivesCompletedEvent);
    }

    /**
     * \
     * Processes the custom objectives as set in the configuration file. These objectives must follow one or more of these formats:
     * KILL_CUSTOM:filename=X.yml:amount=Y
     * FETCH_ITEM:filename=X.yml:amount=Y
     */
    private List<Objective> processCustomObjectives() {
        for (String string : customQuest.getCustomQuestsConfigFields().getCustomObjectivesList()) {
            String[] rawStrings = string.split(":");
            switch (rawStrings[0]) {
                case "KILL_CUSTOM":
                    return processObjectiveType(rawStrings, ObjectiveType.KILL_CUSTOM);
                case "FETCH_ITEM":
                    return processObjectiveType(rawStrings, ObjectiveType.FETCH_ITEM);
                default:
                    new WarningMessage("Entry " + string + " for quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " is not valid! Check the documentation on how to create valid quest objectives on the wiki!");
                    return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    private List<Objective> processObjectiveType(String[] rawStrings, ObjectiveType objectiveType) {
        List<Objective> parsedCustomQuestObjectivesBuffer = new ArrayList<>();
        String filename = null;
        int amount = 1;
        for (String rawString : rawStrings) {
            String[] processedStrings = rawString.split("=");
            switch (processedStrings[0]) {
                case "filename":
                    filename = processedStrings[1];
                    break;
                case "amount":
                    try {
                        amount = Integer.parseInt(processedStrings[1]);
                    } catch (Exception ex) {
                        new WarningMessage("Invalid amount " + amount + " in entry " + rawString + " for Custom Quest " + filename + " . Defaulting to 1.");
                    }
                    break;
            }
        }
        if (filename == null) {
            new WarningMessage("Invalid filename for entry " + rawStrings.toString() + " in Custom Quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " . This objective will not be registered.");
            return new ArrayList<>();
        }
        try {
            if (objectiveType.equals(ObjectiveType.KILL_CUSTOM))
                parsedCustomQuestObjectivesBuffer.add(new CustomKillObjective(filename, amount, customQuest.getQuestLevel()));
            else if (objectiveType.equals(ObjectiveType.FETCH_ITEM))
                //todo: not done yet
                return new ArrayList<>();
        } catch (Exception ex) {
            new WarningMessage("Failed to register objective type for quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " ! This quest will be skipped");
        }

        return parsedCustomQuestObjectivesBuffer;

    }

    private enum ObjectiveType {
        KILL_CUSTOM,
        FETCH_ITEM
    }

    public Scoreboard displayObjectivesScoreboard(Player player){
        List<String> strings = new ArrayList<>();
        for (Objective objective : objectives){
            if (objective instanceof KillObjective)
                strings.add(QuestsConfig.getKillQuestScoreboardProgressionLine(objective));
        }
        return SimpleScoreboard.temporaryScoreboard(player, getCustomQuest().getCustomQuestsConfigFields().getQuestName(), strings, 20 * 5);
    }

}
