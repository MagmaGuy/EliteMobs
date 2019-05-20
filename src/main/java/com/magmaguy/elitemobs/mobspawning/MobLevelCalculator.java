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
        perPlayerEliteMobLevel += (GuildRank.getActiveRank(player) - 10) * 0.2;

        int finalTieredMobLevel = (int) (perPlayerEliteMobLevel * MobTierCalculator.PER_TIER_LEVEL_INCREASE);

        return finalTieredMobLevel;

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
