package com.magmaguy.elitemobs.events.mobs.sharedeventproperties;

import com.magmaguy.elitemobs.mobspawning.MobLevelCalculator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DynamicBossLevelConstructor {

    public static int findDynamicBossLevel() {

        int bossLevel = 1;

        for (Player player : Bukkit.getServer().getOnlinePlayers())
            if (MobLevelCalculator.determineMobLevel(player) > bossLevel)
                bossLevel = MobLevelCalculator.determineMobLevel(player);

        return bossLevel;

    }

}
