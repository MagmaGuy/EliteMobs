package com.magmaguy.elitemobs.commands.quest;

import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import com.magmaguy.elitemobs.quests.EliteQuest;
import com.magmaguy.elitemobs.quests.PlayerQuests;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestStatusCommand {

    public QuestStatusCommand(Player player, String[] args) {

        if (args[1].equalsIgnoreCase("status")) {
            doQuestTrackCommand(player);
            return;
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("cancel") && args[3].equalsIgnoreCase("confirm")) {
            EliteQuest.cancelPlayerQuest(Bukkit.getPlayer(args[2]));
            //if (QuestsMenu.playerHasPendingQuest(Bukkit.getPlayer(args[2]))) {
            //    EliteQuest eliteQuest = QuestsMenu.getPlayerQuestPair(Bukkit.getPlayer(args[2]));
            //    EliteQuest.addPlayerInQuests(Bukkit.getPlayer(args[2]), eliteQuest);
            //    eliteQuest.getQuestObjective().sendQuestStartMessage(Bukkit.getPlayer(args[2]));
            //    QuestsMenu.removePlayerQuestPair(Bukkit.getPlayer(args[2]));
            //}
            return;
        }

        if (args[1].equalsIgnoreCase("cancel") && args[2].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            PlayerQuests.removeQuest(player, UUID.fromString(args[2]));
            player.sendMessage(QuestMenuConfig.questCancelMessage);
            return;
        }

        player.sendMessage("[EliteMobs] Invalid command.");
        player.sendMessage("[EliteMobs] Valid quest-related commands:");
        player.sendMessage("[EliteMobs] /em quest");
        player.sendMessage("[EliteMobs] /em quest status");
    }

    public static void doQuestTrackCommand(Player player) {
        if (!EliteQuest.hasPlayerQuest(player)) {
            player.sendMessage("[EliteMobs] You don't currently have an active quest!");
            return;
        }

        player.sendMessage(EliteQuest.getPlayerQuest(player).getQuestStatus());

    }

}
