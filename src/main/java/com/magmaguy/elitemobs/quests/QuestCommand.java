package com.magmaguy.elitemobs.quests;

import org.bukkit.entity.Player;

public class QuestCommand {

    public static void doMainQuestCommand(Player player) {
        QuestsMenu questsMenu = new QuestsMenu();
        questsMenu.initializeMainQuestMenu(player);
    }

    public static void doQuestTrackCommand(Player player) {
        if (!PlayerQuest.hasPlayerQuest(player)) {
            player.sendMessage("[EliteMobs] You don't currently have an active quest!");
            return;
        }

        PlayerQuest.getPlayerQuest(player).getQuestObjective().sendQuestProgressionMessage(player);

    }

}
