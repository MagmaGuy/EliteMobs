package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.PlayerDataLoadedEvent;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

/**
 * Talk-to objectives aimed at a retired scrap NPC can never be finished, because those NPCs no longer spawn.
 * Quests accepted before the scrapper was removed carry such an objective, so it completes when the player's data loads.
 */
public class RetiredNPCObjectives implements Listener {
    /** Kelly's default file. The welcome quest dropped her objective when scrap was retired. */
    private static final String RETIRED_SCRAPPER = "scrapper_config.yml";

    @EventHandler
    public void onPlayerDataLoaded(PlayerDataLoadedEvent event) {
        UUID player = event.getPlayer().getUniqueId();
        boolean changed = false;
        for (Quest quest : List.copyOf(PlayerData.getQuests(player))) {
            if (quest.getQuestObjectives() == null || quest.getQuestObjectives().getObjectives() == null) continue;
            boolean questChanged = false;
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective instanceof DialogObjective dialogue && !dialogue.isObjectiveCompleted()
                        && isRetired(dialogue.getNpcFilename())) {
                    dialogue.progressObjective(quest.getQuestObjectives());
                    questChanged = true;
                }
            if (!questChanged) continue;
            quest.getQuestObjectives().updateQuestStatus(player);
            changed = true;
        }
        if (changed) PlayerData.updateQuestStatus(player);
    }

    private static boolean isRetired(String npcFilename) {
        if (RETIRED_SCRAPPER.equals(npcFilename)) return true;
        NPCsConfigFields fields = NPCsConfig.getNpcEntities().get(npcFilename);
        return fields != null && fields.isRetiredRole();
    }
}
