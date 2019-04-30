package com.magmaguy.elitemobs.quests;

import org.bukkit.entity.Player;

public class QuestCommand {

    public QuestCommand(Player player) {
        QuestsMenu questsMenu = new QuestsMenu();
        questsMenu.initializeMainQuestMenu(player);
    }

}
