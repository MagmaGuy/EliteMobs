package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.QuestObjectivesCompletedEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class QuestObjectives implements Serializable {

    @Getter
    @Setter
    protected QuestReward questReward;
    @Getter
    @Setter
    protected List<Objective> objectives;
    //The CustomQuest this objective belongs to
    @Getter
    private Quest quest;
    private boolean over = false;
    @Getter
    @Setter
    private boolean turnedIn = false;

    /**
     * Used for dynamic quests
     */
    public QuestObjectives(int questLevel) {
        generateRandomObjective(questLevel);
    }

    /**
     * Used for custom quests
     *
     * @param customQuestReward Predetermined Quest Reward
     */
    public QuestObjectives(QuestReward customQuestReward) {
        this.questReward = customQuestReward;
    }

    private void generateRandomObjective(int questLevel) {
        int killAmount = ThreadLocalRandom.current().nextInt(1 + questLevel, 1 + questLevel * 10);
        EntityType entityType = QuestsConfig.getQuestEntityTypes().get(ThreadLocalRandom.current().nextInt(QuestsConfig.getQuestEntityTypes().size()));
        KillObjective killObjective = new DynamicKillObjective(killAmount, entityType, questLevel);
        this.objectives = Collections.singletonList(killObjective);
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
        boolean checkOver = true;
        for (Objective objective : objectives)
            if (!objective.isObjectiveCompleted()) {
                checkOver = false;
                break;
            }
        return checkOver;
    }

    public void updateQuestStatus(UUID playerUUID) {
        //This checks if the player managed to try to progress an already over and turned in quest, which would reward them again.
        //This would in theory only be possible for quests that display the "completed" status, but should still be inaccessible in theory.
        if (turnedIn) return;
        if (over) return;
        if (!isOver()) return;
        over = true;
        QuestObjectivesCompletedEvent questObjectivesCompletedEvent = new QuestObjectivesCompletedEvent(Bukkit.getPlayer(playerUUID), quest);
        new EventCaller(questObjectivesCompletedEvent);
    }


    public Scoreboard displayTemporaryObjectivesScoreboard(Player player) {
        List<String> strings = new ArrayList<>();
        for (Objective objective : objectives)
            strings.add(QuestsConfig.getQuestScoreboardProgressionLine(objective));

        return SimpleScoreboard.temporaryScoreboard(player, ChatColorConverter.convert(getQuest().getQuestName()), strings, 20 * 20);
    }

    public Scoreboard displayLazyObjectivesScoreboard(Player player) {
        List<String> strings = new ArrayList<>();
        for (Objective objective : objectives)
            strings.add(QuestsConfig.getQuestScoreboardProgressionLine(objective));

        return SimpleScoreboard.lazyScoreboard(player, ChatColorConverter.convert(getQuest().getQuestName()), strings);
    }


}
