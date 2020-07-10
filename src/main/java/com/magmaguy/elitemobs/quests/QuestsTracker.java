package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.utils.DebugMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestsTracker implements Listener {

    @EventHandler
    public void onEntityKill(EliteMobDeathEvent event) {

        if (!event.getEliteMobEntity().getHasSpecialLoot()) return;
        if (!event.getEliteMobEntity().hasDamagers()) return;

        new DebugMessage("1");
        for (Player player : event.getEliteMobEntity().getDamagers().keySet()) {
            new DebugMessage(1.5);
            if (EliteQuest.hasPlayerQuest(player)) {
                new DebugMessage("2");
                if (!EliteQuest.getPlayerQuest(player).getQuestObjective().isTurnedIn()) {
                    new DebugMessage("3");
                    EliteQuest.getPlayerQuest(player).processQuestProgression(event.getEliteMobEntity(), player);
                }
            }
        }
    }

}
