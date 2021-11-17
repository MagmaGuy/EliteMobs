package com.magmaguy.elitemobs.commands.quests;

import com.magmaguy.elitemobs.quests.CustomQuest;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestCommand {

    public static void completeQuest(String questID, Player player) {
        try {
            CustomQuest.completeQuest(UUID.fromString(questID), player);
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "[EliteMobs] Invalid quest ID!");
        }
    }

    // /em quest join questFilename
    public static void joinQuest(String questFilename, Player player) {
        CustomQuest.startQuest(questFilename, player);
    }

    // /em quest leave questFilename
    public static void leaveQuest(String questFilename, Player player) {
        CustomQuest.stopPlayerQuest(player);
    }

}
