package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.config.commands.premade.CheckTierOthersConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckTierOthersCommand {

    public CheckTierOthersCommand(CommandSender commandSender, String playerName) {

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Invalid player name!"));
            return;
        }

        double gearTier = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true);
        double guildRank = GuildRank.getActiveGuildRank(player);
        if (guildRank == 0)
            guildRank = -10;
        else
            guildRank--;
        guildRank = (guildRank * 0.2);

        commandSender.sendMessage(CheckTierOthersConfig.message1.replace("$player", player.getDisplayName()).replace("$tier", gearTier + ""));
        commandSender.sendMessage(CheckTierOthersConfig.message2.replace("$player", player.getDisplayName()).replace("$tier", guildRank + ""));
        commandSender.sendMessage(CheckTierOthersConfig.message3.replace("$player", player.getDisplayName()).replace("$tier", (gearTier + guildRank) + ""));

    }

}
