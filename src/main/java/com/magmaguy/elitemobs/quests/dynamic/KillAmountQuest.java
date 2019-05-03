package com.magmaguy.elitemobs.quests.dynamic;

import com.magmaguy.elitemobs.quests.PlayerQuest;
import com.magmaguy.elitemobs.quests.QuestObjective;
import com.magmaguy.elitemobs.quests.QuestReward;

public class KillAmountQuest extends PlayerQuest {

//    private static HashMap<Player, KillAmountQuest> activeKillAmountQuests = new HashMap<>();
//
//    public static HashMap<Player, KillAmountQuest> getActiveKillAmountQuests() {
//        return activeKillAmountQuests;
//    }
//
//    public static void addActiveKillAmountQuests(Player player, KillAmountQuest killAmountQuest) {
//        getActiveKillAmountQuests().put(player, killAmountQuest);
//    }
//
//    public static boolean hasKillAmountQuest(Player player) {
//        return activeKillAmountQuests.containsKey(player);
//    }
//
//    public static KillAmountQuest getKillAmountQuest(Player player) {
//        if (activeKillAmountQuests.containsKey(player))
//            return activeKillAmountQuests.get(player);
//        return null;
//    }

    public KillAmountQuest(QuestType questType, QuestObjective questObjective, QuestReward questReward) {
        super(questType, questObjective, questReward);
    }

}
