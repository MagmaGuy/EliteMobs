package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.entity.LivingEntity;

public class MobTierFinder {

    public static final double PER_TIER_LEVEL_INCREASE = ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.PER_TIER_LEVEL_INCREASE);

    public static double findMobTier(LivingEntity livingEntity) {

        if (!EntityTracker.isEliteMob(livingEntity)) return 0;

        return EntityTracker.getEliteMobEntity(livingEntity).getLevel() / PER_TIER_LEVEL_INCREASE;

    }

    public static double findMobTier(int mobLevel) {

        return mobLevel / PER_TIER_LEVEL_INCREASE;

    }

}
