package com.magmaguy.elitemobs.quests;

import org.bukkit.entity.Player;

public class QuestCommand {

    public static void doMainQuestCommand(Player player) {
        QuestsMenu questsMenu = new QuestsMenu();
        questsMenu.initializeQuestTierSelectorMenu(player);
    }

    public static void doQuestTrackCommand(Player player) {
        if (!PlayerQuest.hasPlayerQuest(player)) {
            player.sendMessage("[EliteMobs] You don't currently have an active quest!");
            return;
        }

        player.sendMessage(PlayerQuest.getPlayerQuest(player).getQuestStatus());

    }

}
