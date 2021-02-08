package com.magmaguy.elitemobs.commands.combat;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.config.commands.premade.CheckTierConfig;
import org.bukkit.entity.Player;

public class CheckTierCommand {

    public CheckTierCommand(Player player) {

        double gearTier = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true);
        double guildRank = GuildRank.getActiveGuildRank(player);
        if (guildRank == 0)
            guildRank = -10;
        else
            guildRank--;
        guildRank = (guildRank * 0.2);

        player.sendMessage(CheckTierConfig.message1.replace("$tier", gearTier + ""));
        player.sendMessage(CheckTierConfig.message2.replace("$tier", guildRank + ""));
        player.sendMessage(CheckTierConfig.message3.replace("$tier", (gearTier + guildRank) + ""));

    }


}
