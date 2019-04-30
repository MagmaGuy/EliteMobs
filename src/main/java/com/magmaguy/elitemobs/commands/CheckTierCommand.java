package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import org.bukkit.entity.Player;

public class CheckTierCommand {

    public static void checkTier(Player player) {

        double gearTier = ItemTierFinder.findPlayerTier(player);
        double guildTier = (GuildRank.getRank(player) - 10) * 0.2;

        player.sendMessage("Your combat tier is " + gearTier);
        player.sendMessage("Your guild tier bonus is " + guildTier);
        player.sendMessage("Your threat tier is " + (gearTier + guildTier));

    }

}
