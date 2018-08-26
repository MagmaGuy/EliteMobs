package com.magmaguy.elitemobs.events.mobs.sharedeventproperties;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DynamicBossLevelConstructor {

    public static int findDynamicBossLevel() {

        int mobLevel = 0;

        for (Player player : Bukkit.getServer().getOnlinePlayers())
            if (ItemTierFinder.findPlayerTier(player) * ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.PER_TIER_LEVEL_INCREASE) > mobLevel)
                mobLevel = (int) ((ItemTierFinder.findPlayerTier(player) + 2.5) * ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.PER_TIER_LEVEL_INCREASE));

        if (mobLevel == 0)
            mobLevel = (int) (2.5 * ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.PER_TIER_LEVEL_INCREASE));

        return mobLevel;

    }

}
