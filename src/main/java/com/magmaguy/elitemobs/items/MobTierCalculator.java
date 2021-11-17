package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;

public class MobTierCalculator {
    public static final double PER_TIER_LEVEL_INCREASE = 1;

    private MobTierCalculator() {
    }

    public static double findMobTier(EliteEntity eliteEntity) {
        return eliteEntity.getLevel() / PER_TIER_LEVEL_INCREASE;
    }

    public static double findMobTier(int mobLevel) {
        return mobLevel / PER_TIER_LEVEL_INCREASE;
    }

    public static int findMobLevel(int mobTier) {
        return (int) (mobTier * PER_TIER_LEVEL_INCREASE);
    }

}
