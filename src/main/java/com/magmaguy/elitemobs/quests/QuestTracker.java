package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.quests.dynamic.KillAmountQuest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class QuestTracker implements Listener {


    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;
        if (!eliteMobEntity.hasDamagers()) return;

        for (Player player : eliteMobEntity.getDamagers()) {
            if (KillAmountQuest.hasKillAmountQuest(player)) {
                KillAmountQuest killAmountQuest = KillAmountQuest.getKillAmountQuest(player);
                killAmountQuest.incrementCurrentKills();
                if (killAmountQuest.getIsComplete()) {
                    QuestionCompletion.rewardPlayer(player, killAmountQuest.getQuestReward());
                }
            }
        }

    }

}
