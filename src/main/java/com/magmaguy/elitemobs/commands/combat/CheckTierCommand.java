package com.magmaguy.elitemobs.commands.combat;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.commands.premade.CheckTierConfig;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import org.bukkit.entity.Player;

public class CheckTierCommand {

    public CheckTierCommand(Player player) {

        double gearTier = ItemTierFinder.findPlayerTier(player);
        double guildRank = GuildRank.getActiveRank(player);
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
