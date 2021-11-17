package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
    public void progressObjective(QuestObjectives questObjectives) {
        currentAmount++;
        if (currentAmount < targetAmount) return;
        objectiveCompleted = true;
        QuestProgressionEvent questProgressionEvent = new QuestProgressionEvent(
                Bukkit.getPlayer(questObjectives.getQuest().getPlayerUUID()),
                questObjectives.getQuest(),
                this);
        new EventCaller(questProgressionEvent);
    }

    public abstract void checkProgress(EliteMobDeathEvent event, QuestObjectives questObjectives);

    public static class KillQuestEvents implements Listener {
        @EventHandler
        public void onEliteDeath(EliteMobDeathEvent event) {
            for (Player player : event.getEliteEntity().getDamagers().keySet()) {
                Quest quest = CustomQuest.getPlayerQuests().get(player.getUniqueId());
                if (quest != null)
                    for (Objective objective : quest.getQuestObjectives().getObjectives())
                        if (objective instanceof KillObjective)
                            ((KillObjective) objective).checkProgress(event, quest.getQuestObjectives());
            }
        }
    }

}
