package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
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

        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());
        scanQuestTakerNPC(npcEntity, quests, customQuestList);

        //This value can be null for NPC entities that have the custom quest interaction but are only used to turn quests in
        if (npcEntity.getNpCsConfigFields().getQuestFilenames() != null)
            for (String questString : npcEntity.getNpCsConfigFields().getQuestFilenames()) {
                CustomQuest customQuest = CustomQuest.getQuest(questString, player);
                if (customQuest == null) {
                    player.sendMessage("[EliteMobs] This NPC's quest is not valid! This might be a configuration error on the NPC or on the quest.");
                    return;
                }

                if (playerHasPermissionToAcceptQuest(player, customQuest)) {
                    customQuestList.add(customQuest);
                    customQuest.setQuestGiver(npcEntity.getNpCsConfigFields().getFilename());
                }
            }

        if (!customQuestList.isEmpty())
            new BukkitRunnable() {
                @Override
                public void run() {
                    QuestMenu.generateCustomQuestMenu(customQuestList, player, npcEntity);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

    private static boolean playerHasPermissionToAcceptQuest(Player player, CustomQuest customQuest) {
        if (!customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission().isEmpty() &&
                player.hasPermission(customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission())) {
            if (player.hasPermission("elitemobs.*")) {
                player.sendMessage("[EliteMobs - Admin message] You are locked out from this quest! The permission is " + customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission());
                player.sendMessage(ChatColorConverter.convert("&cNote: If you are an admin and are blocked from all quests, make sure you deny the permission elitequest.*"));
            }
            return false;
        }
        if (customQuest.getCustomQuestsConfigFields().getQuestAcceptPermission().isEmpty() ||
                player.hasPermission(customQuest.getCustomQuestsConfigFields().getQuestAcceptPermission())) {
            return true;
        } else {
            player.sendMessage("[EliteMobs] You don't have the required permission! " + customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission());
            return false;
        }
    }

    private static void scanQuestTakerNPC(NPCEntity npcEntity, List<Quest> activeQuests, List<CustomQuest> npcQuests) {
        for (Quest quest : activeQuests)
            if (quest instanceof CustomQuest &&
                    !quest.getQuestTaker().equals(quest.getQuestGiver()) &&
                    npcEntity.getNpCsConfigFields().getFilename().equals(quest.getQuestTaker())) {
                npcQuests.add((CustomQuest) quest);
                for (Objective objective : quest.getQuestObjectives().getObjectives())
                    if (objective instanceof CustomFetchObjective)
                        objective.progressNonlinearObjective(quest.getQuestObjectives());
            }
    }

}
