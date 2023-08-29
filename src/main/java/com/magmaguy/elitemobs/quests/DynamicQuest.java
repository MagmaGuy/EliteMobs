package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.menus.premade.DynamicQuestMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.DynamicKillObjective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DynamicQuest extends Quest {

    //These are generated fresh every hour
    private static final HashMap<Integer, List<QuestObjectives>> threeRandomDynamicObjectives = new HashMap<>();
    private static BukkitTask randomizerTask;

    public DynamicQuest(Player player, int questLevel, QuestObjectives questObjectives) {
        super(player, questObjectives, questLevel);
        super.questName = DynamicQuestMenuConfig.getQuestName()
                .replace("$amount", questObjectives.getObjectives().get(0).getTargetAmount() + "")
                .replace("$name", ChatColor.stripColor(EliteMobProperties.getPluginData(((DynamicKillObjective) questObjectives.getObjectives().get(0)).getEntityType()).getName(questLevel * 10)));
        questObjectives.setQuest(this);
    }

    public static void startRandomizingQuests() {
        randomizerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (int activeLevel = 1; activeLevel < 21; activeLevel++) {
                    List<QuestObjectives> questObjectives = new ArrayList<>();
                    for (int questNumber = 0; questNumber < 3; questNumber++) {
                        questObjectives.add(new QuestObjectives(activeLevel));
                    }
                    threeRandomDynamicObjectives.put(activeLevel, questObjectives);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 60L);
    }

    public static void shutdown() {
        threeRandomDynamicObjectives.clear();
        randomizerTask.cancel();
    }

    public static List<DynamicQuest> generateQuests(Player player) {
        int questLevel = GuildRank.getActiveGuildRank(player);
        if (questLevel == 0) {
            player.sendMessage(QuestsConfig.getLowRankDynamicQuestWarning());
            return new ArrayList<>();
        }
        List<DynamicQuest> dynamicQuests = new ArrayList<>();

        for (QuestObjectives questObjectives : threeRandomDynamicObjectives.get(questLevel)) {
            QuestReward questReward = new QuestReward(questLevel, questObjectives, player);
            questObjectives.setQuestReward(questReward);
            dynamicQuests.add(new DynamicQuest(player, questLevel, questObjectives));
        }

        return dynamicQuests;
    }

    public static List<DynamicQuest> getQuests(Player player) {
        List<DynamicQuest> dynamicQuests = new ArrayList<>();

        if (PlayerData.getQuests(player.getUniqueId()) != null)
            for (Quest quest : PlayerData.getQuests(player.getUniqueId())) {
                if (quest instanceof DynamicQuest dynamicQuest)
                    dynamicQuests.add(dynamicQuest);
            }

        for (DynamicQuest generatedQuest : generateQuests(player)) {
            boolean exists = false;
            for (DynamicQuest dynamicQuest : dynamicQuests) {
                if (dynamicQuest.getQuestObjectives().getUuid().equals(generatedQuest.getQuestObjectives().getUuid())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) dynamicQuests.add(generatedQuest);
        }

        return dynamicQuests;
    }

}
