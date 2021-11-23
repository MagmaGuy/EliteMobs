package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public abstract class KillObjective extends Objective {

    public KillObjective(int targetAmount, String objectiveName) {
        super(targetAmount, objectiveName);
    }

    public abstract void checkProgress(EliteMobDeathEvent event, QuestObjectives questObjectives);

    public static class KillObjectiveEvents implements Listener {
        @EventHandler
        public void onEliteDeath(EliteMobDeathEvent event) {
            for (Player player : event.getEliteEntity().getDamagers().keySet()) {
                Quest quest = Quest.getPlayerQuests().get(player.getUniqueId());
                if (quest != null)
                    for (Objective objective : quest.getQuestObjectives().getObjectives())
                        if (objective instanceof KillObjective)
                            ((KillObjective) objective).checkProgress(event, quest.getQuestObjectives());
            }
        }
    }

}
