package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.ArenaCompleteEvent;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ArenaObjective extends Objective {

    @Getter
    public String arenaFilename;

    protected ArenaObjective(String objectiveName, String arenaFilename) {
        super(1, objectiveName);
        this.arenaFilename = arenaFilename;
    }

    public static class ArenaObjectiveEvents implements Listener {
        @EventHandler
        public void onArenaComplete(ArenaCompleteEvent arenaCompleteEvent) {
            for (Player player : arenaCompleteEvent.getArenaInstance().getPlayers())
                for (Quest quest : PlayerData.getQuests(player.getUniqueId()))
                    if (quest instanceof CustomQuest)
                        for (Objective objective : quest.getQuestObjectives().getObjectives())
                            if (objective instanceof ArenaObjective arenaObjective &&
                                    arenaObjective.arenaFilename.equals(arenaCompleteEvent.getArenaInstance().getCustomArenasConfigFields().getFilename()))
                                objective.progressObjective(quest.getQuestObjectives());
        }
    }


}
