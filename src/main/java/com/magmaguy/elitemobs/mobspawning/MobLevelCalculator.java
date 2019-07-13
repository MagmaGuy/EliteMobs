package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MobLevelCalculator {

    public static int determineMobLevel(Player player) {

        //Add selected player gear tier modifier
        double perPlayerEliteMobLevel = ItemTierFinder.findPlayerTier(player);
        //Add selected guild rank modifier
        double guildRank = GuildRank.getActiveRank(player);
        if (guildRank == 0)
            guildRank = -10;
        else
            guildRank--;
        perPlayerEliteMobLevel += (guildRank * 0.2);

        return (int) (perPlayerEliteMobLevel * MobTierCalculator.PER_TIER_LEVEL_INCREASE);

    }

    public static int determineMobLevel(Location location) {

        int mobLevel = 1;

        for (Entity entity : location.getWorld().getNearbyEntities(location, 80, 80, 80))
            if (entity instanceof Player) {
                mobLevel = determineMobLevel((Player) entity);
                return mobLevel;
            }

        return mobLevel;

    }

}
