package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GuildRankCommands {
    public static void setGuildRank(String[] args) {
        Player player = Bukkit.getPlayer(args[1]);
        int prestigeLevel = Integer.valueOf(args[2]);
        int maxGuildRank = Integer.valueOf(args[3]);
        GuildRank.setGuildPrestigeRank(player, prestigeLevel);
        GuildRank.setMaxGuildRank(player, maxGuildRank);
        GuildRank.setActiveGuildRank(player, maxGuildRank);
    }
}
