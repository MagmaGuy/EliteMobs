package com.magmaguy.elitemobs.commands.quests;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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

    public static void resetQuests(CommandSender commandSender, String playerString) {
        Player player = Bukkit.getPlayer(playerString);
        if (player == null) {
            commandSender.sendMessage("[EliteMobs] Error - player name not valid!");
            return;
        }
        PlayerQuestCooldowns.resetPlayerQuestCooldowns(player);
        commandSender.sendMessage("[EliteMobs] Successfully reset quests for player " + playerString);
    }

    public static void resetQuest(CommandSender commandSender, String playerString, String questName) {
        Player player = Bukkit.getPlayer(playerString);
        if (player == null) {
            commandSender.sendMessage("[EliteMobs] Error - player name is not valid!");
            return;
        }
        CustomQuestsConfigFields customQuestsConfigFields = CustomQuestsConfig.getCustomQuests().get(questName);
        if (customQuestsConfigFields == null) {
            commandSender.sendMessage("[EliteMobs] Error - quest filename " + questName + " is not valid!");
            return;
        }
        PlayerQuestCooldowns.resetPlayerQuestCooldown(player, customQuestsConfigFields);
        commandSender.sendMessage("[EliteMobs] Successfully quest " + questName + " for player " + playerString);
    }

    public static void completeQuest(Player player) {
        for (Quest quest : new ArrayList<Quest>(PlayerData.getQuests(player.getUniqueId()))) {
            quest.getQuestObjectives().setForceOver(true);
            Quest.completeQuest(quest.getQuestID(), player);
        }
    }

    public static void bypassQuestRequirements(Player player) {
        PlayerQuestCooldowns.toggleBypass(player);
    }

}
