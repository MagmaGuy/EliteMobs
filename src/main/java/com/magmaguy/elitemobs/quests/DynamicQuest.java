package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.menus.premade.DynamicQuestMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.DynamicKillObjective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
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

    public DynamicQuest(Player player, int targetMobLevel, QuestObjectives questObjectives) {
        super(player, questObjectives, DynamicQuestLevel.clamp(targetMobLevel));
        DynamicKillObjective dynamicKillObjective = (DynamicKillObjective) questObjectives.getObjectives().get(0);
        super.questName = DynamicQuestMenuConfig.getQuestName()
                .replace("$amount", questObjectives.getObjectives().get(0).getTargetAmount() + "")
                .replace("$name", ChatColor.stripColor(EliteMobProperties.getPluginData(dynamicKillObjective.getEntityType()).getName(dynamicKillObjective.getMinMobLevel())));
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
        if (randomizerTask != null)
            randomizerTask.cancel();
    }

    public static List<DynamicQuest> generateQuests(Player player) {
        int combatLevel = DynamicQuestLevel.clamp(CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
        int questTemplateBucket = DynamicQuestLevel.toTemplateBucket(combatLevel);
        if (questTemplateBucket == 0) {
            player.sendMessage(QuestsConfig.getLowRankDynamicQuestWarning());
            return new ArrayList<>();
        }
        List<DynamicQuest> dynamicQuests = new ArrayList<>();

        List<QuestObjectives> questTemplates = threeRandomDynamicObjectives.get(questTemplateBucket);
        if (questTemplates == null) return dynamicQuests;

        for (QuestObjectives questTemplate : questTemplates) {
            DynamicKillObjective templateObjective = (DynamicKillObjective) questTemplate.getObjectives().get(0);
            QuestObjectives playerQuestObjectives = new QuestObjectives(
                    questTemplate.getUuid(),
                    List.of(new DynamicKillObjective(
                            templateObjective.getTargetAmount(),
                            templateObjective.getEntityType(),
                            combatLevel)));
            QuestReward questReward = new QuestReward(combatLevel, playerQuestObjectives, player);
            playerQuestObjectives.setQuestReward(questReward);
            dynamicQuests.add(new DynamicQuest(player, combatLevel, playerQuestObjectives));
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

    /**
     * Adapts all active DynamicQuests for a player to a new mob level.
     * Called when a player enters a dynamic dungeon with a specific level selection.
     *
     * @param player The player whose quests should be adapted
     * @param newMobLevel The new mob level from the dynamic dungeon
     */
    public static void adaptPlayerQuestsToLevel(Player player, int newMobLevel) {
        if (PlayerData.getQuests(player.getUniqueId()) == null) return;

        for (Quest quest : PlayerData.getQuests(player.getUniqueId())) {
            if (quest instanceof DynamicQuest dynamicQuest) {
                dynamicQuest.adaptToLevel(newMobLevel);
            }
        }
    }

    /**
     * Adapts this quest to a new mob level.
     * Updates the quest level, objectives' minMobLevel, and quest name.
     *
     * @param newMobLevel The new mob level from the dynamic dungeon
     */
    public void adaptToLevel(int newMobLevel) {
        int cappedMobLevel = DynamicQuestLevel.clamp(newMobLevel);
        setQuestLevel(cappedMobLevel);

        // Adapt objectives
        getQuestObjectives().adaptToLevel(cappedMobLevel);

        // Update quest name to reflect new level
        if (!getQuestObjectives().getObjectives().isEmpty() &&
                getQuestObjectives().getObjectives().get(0) instanceof DynamicKillObjective dynamicKillObjective) {
            this.questName = DynamicQuestMenuConfig.getQuestName()
                    .replace("$amount", dynamicKillObjective.getTargetAmount() + "")
                    .replace("$name", ChatColor.stripColor(
                            EliteMobProperties.getPluginData(dynamicKillObjective.getEntityType()).getName(cappedMobLevel)));
        }
    }

}
