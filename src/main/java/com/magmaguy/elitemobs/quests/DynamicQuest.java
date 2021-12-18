package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.menus.premade.DynamicQuestMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.DynamicKillObjective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DynamicQuest extends Quest {

    private static HashMap<UUID, List<DynamicQuest>> dynamicPlayerQuests = new HashMap<>();

    public DynamicQuest(Player player, int questLevel, QuestObjectives questObjectives) {
        super(player, questObjectives, questLevel);
        super.questName = DynamicQuestMenuConfig.getQuestName()
                .replace("$amount", questObjectives.getObjectives().get(0).getTargetAmount() + "")
                .replace("$name", ChatColor.stripColor(EliteMobProperties.getPluginData(((DynamicKillObjective) questObjectives.getObjectives().get(0)).getEntityType()).getName(questLevel * 10)));
        questObjectives.setQuest(this);
    }

    public static List<DynamicQuest> generateQuests(Player player) {
        List<DynamicQuest> dynamicQuests = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int questLevel = GuildRank.getActiveGuildRank(player);
            QuestObjectives questObjectives = new QuestObjectives(questLevel);
            QuestReward questReward = new QuestReward(questLevel, questObjectives, player);
            questObjectives.setQuestReward(questReward);
            dynamicQuests.add(new DynamicQuest(player, questLevel, questObjectives));
        }
        dynamicPlayerQuests.put(player.getUniqueId(), dynamicQuests);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    dynamicPlayerQuests.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                generateQuests(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L * 60 * 60);
        return dynamicQuests;
    }

    public static List<DynamicQuest> getQuests(Player player) {
        List<DynamicQuest> dynamicQuests = new ArrayList<>();

        if (PlayerData.getQuests(player.getUniqueId()) instanceof DynamicQuest)
            dynamicQuests.add((DynamicQuest) PlayerData.getQuests(player.getUniqueId()));

        if (dynamicPlayerQuests.get(player.getUniqueId()) == null)
            dynamicQuests.addAll(generateQuests(player));
        else
            for (DynamicQuest dynamicQuest : dynamicPlayerQuests.get(player.getUniqueId()))
                if (!(!dynamicQuests.isEmpty() && dynamicQuests.get(0).getQuestID().equals(dynamicQuest.getQuestID())))
                    dynamicQuests.add(dynamicQuest);

        return dynamicQuests;
    }

}
