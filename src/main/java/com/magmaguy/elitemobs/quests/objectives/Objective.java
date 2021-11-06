package com.magmaguy.elitemobs.quests.objectives;

import lombok.Getter;

import java.io.Serializable;

public abstract class Objective implements Serializable {
    //todo: add the ability to have multiple quest objective types in here instead of just the one
    //todo: this class should be the one that handles giving out quest rewards

    @Getter
    protected boolean objectiveCompleted = false;

    public Objective() {
    }

    abstract void progressObjective(CustomQuestObjectives customQuestObjectives);

}
