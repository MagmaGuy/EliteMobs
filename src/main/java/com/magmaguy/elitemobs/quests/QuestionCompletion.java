package com.magmaguy.elitemobs.quests;

import org.bukkit.entity.Player;

public class QuestionCompletion {

    public static void rewardPlayer(Player player, QuestReward questReward) {
        questReward.doReward(player);
    }

}
