package com.magmaguy.elitemobs.commands.quest;

import com.magmaguy.elitemobs.quests.QuestsMenu;
import org.bukkit.entity.Player;

public class QuestCommand {

    public QuestCommand(Player player) {
        doMainQuestCommand(player);
    }

    public static void doMainQuestCommand(Player player) {
        QuestsMenu.initializeQuestsMenu(player);
    }

}
