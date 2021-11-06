package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public abstract class KillObjective extends Objective {

    @Getter
    @Setter
    protected int currentAmount = 0, targetAmount;
    @Getter
    protected String entityName;

    public KillObjective(int targetAmount, String entityName) {
        super();
        this.targetAmount = targetAmount;
        this.entityName = entityName;
    }

    @Override
    public void progressObjective(CustomQuestObjectives customQuestObjectives) {
        new DeveloperMessage("progressing kill objective");
        currentAmount++;
        if (currentAmount < targetAmount) return;
        objectiveCompleted = true;
        customQuestObjectives.updateQuestStatus(customQuestObjectives.getCustomQuest().getPlayerUUID(), customQuestObjectives.getCustomQuest().getQuestLevel());
    }

    public abstract void checkProgress(EliteMobDeathEvent event, CustomQuestObjectives customQuestObjectives);

    public static class KillQuestEvents implements Listener {
        @EventHandler
        public void onEliteDeath(EliteMobDeathEvent event) {
            new DeveloperMessage("elit death");
            for (CustomQuest customQuest : CustomQuest.getPlayerQuests().values()) {
                new DeveloperMessage("Iterating quest");
                for (Objective objective : customQuest.getCustomQuestObjectives().getObjectives()) {
                    new DeveloperMessage("Iterating objective");
                    if (objective instanceof KillObjective) {
                        new DeveloperMessage("Found kill objective");
                        ((KillObjective) objective).checkProgress(event, customQuest.getCustomQuestObjectives());
                    }
                }
            }
        }
    }

}
