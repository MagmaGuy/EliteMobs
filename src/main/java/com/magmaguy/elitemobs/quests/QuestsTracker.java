package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestsTracker implements Listener {

    @EventHandler
    public void onEntityKill(EliteMobDeathEvent event) {

        if (!event.getEliteEntity().isEliteLoot()) return;
        if (!event.getEliteEntity().hasDamagers()) return;

        for (Player player : event.getEliteEntity().getDamagers().keySet())
            if (EliteQuest.hasPlayerQuest(player))
                if (!EliteQuest.getPlayerQuest(player).getQuestObjective().isTurnedIn())
                    EliteQuest.getPlayerQuest(player).processQuestProgression(event.getEliteEntity(), player);
    }

}
