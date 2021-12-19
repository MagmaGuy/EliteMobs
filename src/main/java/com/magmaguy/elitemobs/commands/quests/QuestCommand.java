package com.magmaguy.elitemobs.commands.quests;

import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestCommand {

    private QuestCommand() {
    }

    public static void completeQuest(String questID, Player player) {
        try {
            Quest.completeQuest(UUID.fromString(questID), player);
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "[EliteMobs] Invalid quest ID!");
        }
    }

    // /em quest join questFilename
    public static void joinQuest(String questID, Player player) {
        CustomQuest.startQuest(questID, player);
    }

    public static void trackQuest(String questID, Player player) {
        QuestTracking.toggleTracking(player, questID);
    }

    // /em quest leave questFilename
    public static void leaveQuest(Player player, String questID) {
        Quest.stopPlayerQuest(player, questID);
    }

}
