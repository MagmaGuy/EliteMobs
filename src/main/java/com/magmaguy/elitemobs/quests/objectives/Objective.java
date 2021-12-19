package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.io.Serializable;

public abstract class Objective implements Serializable {
    //todo: add the ability to have multiple quest objective types in here instead of just the one
    //todo: this class should be the one that handles giving out quest rewards

    @Getter
    protected boolean objectiveCompleted = false;

    @Getter
    @Setter
    protected int currentAmount = 0;
    @Getter
    protected int targetAmount;

    @Getter
    protected String objectiveName;

    public Objective(int targetAmount, String objectiveName) {
        this.targetAmount = targetAmount;
        this.objectiveName = objectiveName;
    }

    public void progressObjective(QuestObjectives questObjectives) {
        currentAmount++;
        if (currentAmount >= targetAmount)
            objectiveCompleted = true;
        QuestProgressionEvent questProgressionEvent = new QuestProgressionEvent(
                Bukkit.getPlayer(questObjectives.getQuest().getPlayerUUID()),
                questObjectives.getQuest(),
                this);
        new EventCaller(questProgressionEvent);
    }

    public void progressNonlinearObjective(QuestObjectives questObjectives) {
        QuestProgressionEvent questProgressionEvent = new QuestProgressionEvent(
                Bukkit.getPlayer(questObjectives.getQuest().getPlayerUUID()),
                questObjectives.getQuest(),
                this);
        new EventCaller(questProgressionEvent);
        objectiveCompleted = currentAmount >= targetAmount;
    }

}
