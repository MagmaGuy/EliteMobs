package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
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
        if (!PlayerData.isInMemory(player) || npcEntity == null) return;
        List<DynamicQuest> dynamicQuests = new ArrayList<>();
        DynamicQuest.getQuests(player).forEach(quest -> {
            if (!quest.getQuestObjectives().isTurnedIn()) dynamicQuests.add(quest);
        });

        if (!dynamicQuests.isEmpty())
            if (!QuestDialogueBossBarManager.showQuestMenuIntro(dynamicQuests, player, npcEntity,
                    () -> QuestMenu.generateDynamicQuestMenu(dynamicQuests, player, npcEntity)))
                QuestMenu.generateDynamicQuestMenu(dynamicQuests, player, npcEntity);
    }

    public static void processNPCQuests(Player player, NPCEntity npcEntity) {
        if (!PlayerData.isInMemory(player) || npcEntity == null) return;
        List<CustomQuest> customQuestList = new ArrayList<>();

        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());
        scanQuestTakerNPC(npcEntity, quests, customQuestList, player);

        List<String> invalidQuests = new ArrayList<>();
        CustomQuestsConfigFields lockedQuest = null;
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

                CustomQuestsConfigFields fields = CustomQuestsConfig.getCustomQuests().get(questString);
                if (fields == null) {
                    invalidQuests.add(questString);
                    continue;
                }
                CustomQuest customQuest = CustomQuest.getQuest(questString, player);
                if (customQuest != null && !customQuest.getQuestObjectives().isTurnedIn()) {
                    customQuestList.add(customQuest);
                    customQuest.setQuestGiver(npcEntity.getNPCsConfigFields().getFilename());
                } else {
                    // Check if player has completed this quest (either via old permission system or new lockout system)
                    String lockoutPerm = fields.getQuestLockoutPermission();
                    boolean completedViaPermission = lockoutPerm != null && !lockoutPerm.isEmpty() &&
                            player.hasMetadata(lockoutPerm);
                    QuestLockout lockout = PlayerData.getQuestLockout(player.getUniqueId());
                    boolean completedViaLockout = fields.getQuestLockoutMinutes() > 0 &&
                            lockout != null && lockout.isLockedOut(fields.getFilename());
                    if (completedViaLockout && lockedQuest == null) lockedQuest = fields;

                    if (completedViaPermission || completedViaLockout) {
                        questCompleteCount++;
                    }
                }
            }

        Quest.replaceOffers(player, customQuestList);
        // A turn-in, active quest or available offer is a successful interaction, even if
        // other quests on this NPC are locked. Only explain restrictions when nothing opens.
        if (!customQuestList.isEmpty()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !PlayerData.isInMemory(player)) return;
                    if (!QuestDialogueBossBarManager.showQuestMenuIntro(customQuestList, player, npcEntity,
                            () -> QuestMenu.generateCustomQuestMenu(customQuestList, player, npcEntity)))
                        QuestMenu.generateCustomQuestMenu(customQuestList, player, npcEntity);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 1);
            return;
        }

        if (!invalidQuests.isEmpty()) {
            player.sendMessage(QuestsConfig.getInvalidQuestNpcMessage());
            if (player.hasPermission("elitemobs.*"))
                for (String invalidQuest : invalidQuests)
                    player.sendMessage(QuestsConfig.getInvalidQuestMessage().replace("$quest", invalidQuest));
        } else if (lockedQuest != null) {
            QuestLockoutHandler.isLockedOut(player, lockedQuest.getFilename(), lockedQuest.getQuestName());
        } else if (npcEntity.getNPCsConfigFields().getQuestFilenames() != null
                && !npcEntity.getNPCsConfigFields().getQuestFilenames().isEmpty()) {
            if (questCompleteCount == npcEntity.getNPCsConfigFields().getQuestFilenames().size())
                player.sendMessage(QuestsConfig.getQuestAlreadyCompletedMessage());
            else
                player.sendMessage(QuestsConfig.getQuestPrerequisitesMissingMessage());
        }
    }

    private static void scanQuestTakerNPC(NPCEntity npcEntity, List<Quest> activeQuests, List<CustomQuest> npcQuests, Player player) {
        for (Quest quest : activeQuests) {
            if (quest instanceof CustomQuest &&
                    quest.isAccepted() && !quest.getQuestObjectives().isTurnedIn() &&
                    quest.getQuestTaker().equals(npcEntity.getNPCsConfigFields().getFilename())) {
                npcQuests.add((CustomQuest) quest);
                for (Objective objective : quest.getQuestObjectives().getObjectives())
                    if (objective instanceof CustomFetchObjective)
                        objective.progressNonlinearObjective(quest.getQuestObjectives(), player);
            }
        }
    }

}
