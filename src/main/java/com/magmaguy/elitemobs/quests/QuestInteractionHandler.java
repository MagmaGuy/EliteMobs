package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
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
        List<DynamicQuest> dynamicQuests = new ArrayList<>();
        DynamicQuest.getQuests(player).forEach(quest -> {
            if (!quest.getQuestObjectives().isTurnedIn()) dynamicQuests.add(quest);
        });
        if (PlayerData.getQuests(player.getUniqueId()) != null)
            for (Quest quest : PlayerData.getQuests(player.getUniqueId()))
                if (quest instanceof DynamicQuest && !dynamicQuests.contains(quest))
                    dynamicQuests.add((DynamicQuest) quest);
        QuestMenu.generateDynamicQuestMenu(dynamicQuests, player, npcEntity);
    }

    public static void processNPCQuests(Player player, NPCEntity npcEntity) {
        List<CustomQuest> customQuestList = new ArrayList<>();

        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());
        scanQuestTakerNPC(npcEntity, quests, customQuestList, player);

        boolean anyQuestIsValid = false;
        int questCompleteCount = 0;
        //This value can be null for NPC entities that have the custom quest interaction but are only used to turn quests in
        if (npcEntity.getNPCsConfigFields().getQuestFilenames() != null)
            for (String questString : npcEntity.getNPCsConfigFields().getQuestFilenames()) {
                boolean activeQuest = false;
                for (CustomQuest customQuest : customQuestList)
                    if (customQuest.getCustomQuestsConfigFields().getFilename().equals(questString)) {
                        activeQuest = true;
                        continue;
                    }
                if (activeQuest) continue;

                CustomQuest customQuest = CustomQuest.getQuest(questString, player);
                if (customQuest == null) {
                    player.sendMessage("[EliteMobs] This NPC's quest is not valid! This might be a configuration error on the NPC or on the quest.");
                    if (player.hasPermission("elitemobs.*"))
                        player.sendMessage("Invalid quest: " + questString);
                    continue;
                }

                if (customQuest.hasPermissionForQuest(player)) {
                    customQuestList.add(customQuest);
                    customQuest.setQuestGiver(npcEntity.getNPCsConfigFields().getFilename());
                    anyQuestIsValid = true;
                } else if (!customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission().isEmpty() &&
                        player.hasMetadata(customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission()))
                    questCompleteCount++;
            }

        if (npcEntity.getNPCsConfigFields().getQuestFilenames() != null &&
                questCompleteCount == npcEntity.getNPCsConfigFields().getQuestFilenames().size())
            player.sendMessage(QuestsConfig.getQuestAlreadyCompletedMessage());
        else if (!anyQuestIsValid && npcEntity.getNPCsConfigFields().getQuestFilenames() != null)
            player.sendMessage(QuestsConfig.getQuestPrerequisitesMissingMessage());

        if (!customQuestList.isEmpty())
            new BukkitRunnable() {
                @Override
                public void run() {
                    QuestMenu.generateCustomQuestMenu(customQuestList, player, npcEntity);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

    private static void scanQuestTakerNPC(NPCEntity npcEntity, List<Quest> activeQuests, List<CustomQuest> npcQuests, Player player) {
        for (Quest quest : activeQuests) {
            if (quest instanceof CustomQuest &&
                    quest.getQuestTaker().equals(npcEntity.getNPCsConfigFields().getFilename())) {
                npcQuests.add((CustomQuest) quest);
                for (Objective objective : quest.getQuestObjectives().getObjectives())
                    if (objective instanceof CustomFetchObjective)
                        objective.progressNonlinearObjective(quest.getQuestObjectives(), player, true);
            }
        }
    }

}
