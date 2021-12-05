package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class QuestInteractionHandler {
    private QuestInteractionHandler() {
    }

    public static void processDynamicQuests(Player player, NPCEntity npcEntity) {
        QuestMenu.generateDynamicQuestMenu(DynamicQuest.getQuests(player), player);
    }

    public static void processNPCQuests(Player player, NPCEntity npcEntity) {
        List<CustomQuest> customQuestList = new ArrayList<>();
        //This value can be null for NPC entities that have the custom quest interaction but are only used to turn quests in
        if (npcEntity.getNpCsConfigFields().getQuestFilename() != null)
            for (String questString : npcEntity.getNpCsConfigFields().getQuestFilename()) {
                CustomQuest customQuest = CustomQuest.getQuest(questString, player);
                if (customQuest == null) {
                    player.sendMessage("[EliteMobs] This NPC's quest is not valid! This might be a configuration error on the NPC or on the quest.");
                    return;
                }

                if ((customQuest.getCustomQuestsConfigFields().getQuestAcceptPermission() == null ||
                        customQuest.getCustomQuestsConfigFields().getQuestAcceptPermission() != null &&
                                player.hasPermission(customQuest.getCustomQuestsConfigFields().getQuestAcceptPermission())) &&
                        (customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission() == null ||
                                customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission() != null &&
                                        player.hasPermission(customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission())))
                    customQuestList.add(customQuest);
            }

        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());
        if (quests != null)
            for (Quest quest : quests)
                if (quest instanceof CustomQuest &&
                        (!((CustomQuest) quest).getCustomQuestsConfigFields().getTurnInNPC().isEmpty() &&
                                ((CustomQuest) quest).getCustomQuestsConfigFields().getTurnInNPC().equals(npcEntity.getNpCsConfigFields().getFilename())))
                    customQuestList.add((CustomQuest) quest);

        if (!customQuestList.isEmpty())
            new BukkitRunnable() {
                @Override
                public void run() {
                    QuestMenu.generateCustomQuestMenu(customQuestList, player, npcEntity);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }
}
