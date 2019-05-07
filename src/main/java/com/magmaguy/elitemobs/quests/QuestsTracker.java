package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class QuestsTracker implements Listener {


    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;
        if (!eliteMobEntity.getHasSpecialLoot()) return;
        if (!eliteMobEntity.hasDamagers()) return;

        for (Player player : eliteMobEntity.getDamagers())
            if (PlayerQuest.hasPlayerQuest(player))
                if (!PlayerQuest.getPlayerQuest(player).getQuestObjective().isTurnedIn())
                    PlayerQuest.getPlayerQuest(player).processQuestProgression(eliteMobEntity, player);

    }

}
