package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestObjectivesCompletedEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.CustomQuestReward;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestObjectives {

    @Getter
    @Setter
    protected CustomQuestReward customQuestReward;
    @Getter
    @Setter
    protected List<Objective> objectives;
    //The CustomQuest this objective belongs to
    @Getter
    private Quest quest;
    private boolean isOver = false;

    public QuestObjectives(CustomQuestReward customQuestReward) {
        this.customQuestReward = customQuestReward;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
        if (quest instanceof CustomQuest)
            this.objectives = CustomObjectivesParser.processCustomObjectives((CustomQuest) quest);
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
        QuestObjectivesCompletedEvent questObjectivesCompletedEvent = new QuestObjectivesCompletedEvent(Bukkit.getPlayer(playerUUID), quest);
        new EventCaller(questObjectivesCompletedEvent);
    }


    public Scoreboard displayObjectivesScoreboard(Player player) {
        List<String> strings = new ArrayList<>();
        for (Objective objective : objectives) {
            if (objective instanceof KillObjective)
                strings.add(QuestsConfig.getKillQuestScoreboardProgressionLine(objective));
        }
        return SimpleScoreboard.temporaryScoreboard(player, getQuest().getQuestName(), strings, 20 * 5);
    }


}
