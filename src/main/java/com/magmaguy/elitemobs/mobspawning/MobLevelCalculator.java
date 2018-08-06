package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.MobTierFinder;
import org.bukkit.entity.Player;

public class MobLevelCalculator {

    public static int determineMobLevel(Player player) {

        //Add selected player gear tier modifier
        double perPlayerEliteMobLevel = ItemTierFinder.findPlayerTier(player);
        //Add selected guild rank modifier
        perPlayerEliteMobLevel += (GuildRank.getGuildRank(player.getUniqueId()) - 10) * 0.2;

        int finalTieredMobLevel = (int) (perPlayerEliteMobLevel * MobTierFinder.PER_TIER_LEVEL_INCREASE);

        return finalTieredMobLevel;

    }

}
