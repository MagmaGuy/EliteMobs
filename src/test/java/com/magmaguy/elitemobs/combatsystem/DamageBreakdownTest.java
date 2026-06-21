package com.magmaguy.elitemobs.combatsystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageBreakdownTest {

    private static final double EPSILON = 0.0001;

    @Test
    void equipmentEnchantmentMultiplierScalesOutgoingDamage() {
        DamageBreakdown breakdown = new DamageBreakdown();
        breakdown.setBaseDamage(20);
        breakdown.setAttackSpeedFactor(1);
        breakdown.setSkillAdjustment(1);
        breakdown.setWeaponAdjustment(1);
        breakdown.setCooldownOrVelocity(0.5);
        breakdown.setSweepMultiplier(1);
        breakdown.setPotionMultiplier(2);
        breakdown.setEnchantmentMultiplier(1);
        breakdown.setArrowDamageMultiplier(1);
        breakdown.setEquipmentEnchantmentMultiplier(1.25);

        breakdown.compute();

        assertEquals(25, breakdown.getFormulaDamage(), EPSILON);
        assertEquals(25, breakdown.getFinalDamage(), EPSILON);
    }

    @Test
    void sweepReductionAlsoAppliesBeforeEquipmentEnchantmentMultiplier() {
        DamageBreakdown breakdown = new DamageBreakdown();
        breakdown.setBaseDamage(20);
        breakdown.setAttackSpeedFactor(1);
        breakdown.setSkillAdjustment(1);
        breakdown.setWeaponAdjustment(1);
        breakdown.setCooldownOrVelocity(1);
        breakdown.setSweepMultiplier(WeaponOffenseCalculator.SWEEP_DAMAGE_FRACTION);
        breakdown.setPotionMultiplier(1);
        breakdown.setEnchantmentMultiplier(1);
        breakdown.setArrowDamageMultiplier(1);
        breakdown.setEquipmentEnchantmentMultiplier(1.25);

        breakdown.compute();

        assertEquals(6.25, breakdown.getFormulaDamage(), EPSILON);
        assertEquals(6.25, breakdown.getFinalDamage(), EPSILON);
    }

}
