package com.magmaguy.elitemobs.events.mobs.sharedeventproperties;

import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DynamicBossLevelConstructor {

    public static int findDynamicBossLevel() {
        int bossLevel = 1;
        for (Player player : Bukkit.getServer().getOnlinePlayers())
            if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getNaturalMobSpawnLevel(true) > bossLevel)
                bossLevel = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getNaturalMobSpawnLevel(false);
        return bossLevel;
    }

}
