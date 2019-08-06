package com.magmaguy.elitemobs.commands.quest;

import com.magmaguy.elitemobs.quests.PlayerQuest;
import com.magmaguy.elitemobs.quests.QuestsMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QuestCommand {

    public QuestCommand(Player player, String[] args) {

        if (args.length == 1) {
            doMainQuestCommand(player);
            return;
        }
        if (args[1].equalsIgnoreCase("status")) {
            doQuestTrackCommand(player);
            return;
        }
        if (args[1].equalsIgnoreCase("cancel") && args[3].equalsIgnoreCase("confirm")) {
            PlayerQuest.cancelPlayerQuest(Bukkit.getPlayer(args[2]));
            if (QuestsMenu.playerHasPendingQuest(Bukkit.getPlayer(args[2]))) {
                PlayerQuest playerQuest = QuestsMenu.getPlayerQuestPair(Bukkit.getPlayer(args[2]));
                try {
                    PlayerQuest.addPlayerInQuests(Bukkit.getPlayer(args[2]), playerQuest.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                playerQuest.getQuestObjective().sendQuestStartMessage(Bukkit.getPlayer(args[2]));
                QuestsMenu.removePlayerQuestPair(Bukkit.getPlayer(args[2]));
            }
            return;
        }
        player.sendMessage("[EliteMobs] Invalid command.");
        player.sendMessage("[EliteMobs] Valid quest-related commands:");
        player.sendMessage("[EliteMobs] /em quest");
        player.sendMessage("[EliteMobs] /em quest status");

    }


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
