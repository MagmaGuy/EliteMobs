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
        scanQuestTakerNPC(npcEntity, quests, customQuestList);

        //This value can be null for NPC entities that have the custom quest interaction but are only used to turn quests in
        if (npcEntity.getNpCsConfigFields().getQuestFilenames() != null)
            for (String questString : npcEntity.getNpCsConfigFields().getQuestFilenames()) {
                CustomQuest customQuest = CustomQuest.getQuest(questString, player);
                if (customQuest == null) {
                    player.sendMessage("[EliteMobs] This NPC's quest is not valid! This might be a configuration error on the NPC or on the quest.");
                    return;
                }

                if (customQuest.hasPermissionForQuest(player)) {
                    customQuestList.add(customQuest);
                    customQuest.setQuestGiver(npcEntity.getNpCsConfigFields().getFilename());
                } else if (!customQuest.getCustomQuestsConfigFields().getQuestAcceptPermission().isEmpty() &&
                        !player.hasMetadata(customQuest.getCustomQuestsConfigFields().getQuestAcceptPermission())) {
                    player.sendMessage(ChatColorConverter.convert("&4[EliteMobs] You can't accept this quest yet!"));
                } else if (!customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission().isEmpty() &&
                        player.hasMetadata(customQuest.getCustomQuestsConfigFields().getQuestLockoutPermission()))
                    player.sendMessage(ChatColorConverter.convert("&4[EliteMobs] You already completed this quest!"));
            }

        if (!customQuestList.isEmpty())
            new BukkitRunnable() {
                @Override
                public void run() {
                    QuestMenu.generateCustomQuestMenu(customQuestList, player, npcEntity);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 1);
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
