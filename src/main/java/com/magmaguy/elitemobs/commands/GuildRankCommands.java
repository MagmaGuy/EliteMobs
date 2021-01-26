package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildRankCommands {
    public static void setGuildRank(CommandSender commandSender, String playerString, int prestigeLevel, int activeLevel) {

        Player player = Bukkit.getPlayer(playerString);
        if (player == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4The player isn't online!"));
            return;
        }

        if (activeLevel < 0 || activeLevel > 10 + prestigeLevel) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4The prestige rank / guild rank combination you picked is not valid!"));
            return;
        }

        GuildRank.setGuildPrestigeRank(player, prestigeLevel);
        GuildRank.setMaxGuildRank(player, activeLevel);
        GuildRank.setActiveGuildRank(player, activeLevel);

        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2You set the EliteMobs rank of " +
                playerString + " to " + GuildRank.getRankName(GuildRank.getGuildPrestigeRank(player), GuildRank.getActiveGuildRank(player))));
    }

}
