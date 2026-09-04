package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Centralizes combat-relevant potion math so incoming and outgoing damage stay symmetric.
 * <p>
 * All supported effects use percentage-per-amplifier values from
 * {@link MobCombatSettingsConfig}:
 * <ul>
 *   <li>Resistance reduces incoming damage via {@link MobCombatSettingsConfig#getResistanceDamageMultiplier()}</li>
 *   <li>Strength increases outgoing damage via {@link MobCombatSettingsConfig#getStrengthDamageMultiplier()}</li>
 *   <li>Weakness reduces outgoing damage via {@link MobCombatSettingsConfig#getWeaknessDamageMultiplier()}</li>
 * </ul>
 * <p>
 * Strength and Weakness are combined linearly so equal levels cancel exactly.
 */
public class PotionCombatModifierCalculator {

    private PotionCombatModifierCalculator() {
    }

    private static int getEffectLevel(
            LivingEntity entity,
            PotionEffectType effectType) {
        if (entity == null || !entity.hasPotionEffect(effectType)) return 0;

        PotionEffect potionEffect = entity.getPotionEffect(effectType);
        if (potionEffect == null) return 0;

        return potionEffect.getAmplifier() + 1;
    }

    public static double getIncomingDamageMultiplier(LivingEntity entity) {
        return getIncomingDamageMultiplier(
                getEffectLevel(entity, PotionEffectType.RESISTANCE));
    }

    public static double getOutgoingDamageMultiplier(LivingEntity entity) {
        return getOutgoingDamageMultiplier(
                getEffectLevel(entity, PotionEffectType.STRENGTH),
                getEffectLevel(entity, PotionEffectType.WEAKNESS));
    }

    /**
     * Composes potion effects at the damage boundary where both combatants are known.
     * Source Strength and Weakness affect outgoing damage while target Resistance affects
     * incoming damage.
     */
    public static double getCombinedDamageMultiplier(
            LivingEntity source,
            LivingEntity target) {
        return getCombinedDamageMultiplier(
                getEffectLevel(source, PotionEffectType.STRENGTH),
                getEffectLevel(source, PotionEffectType.WEAKNESS),
                getEffectLevel(target, PotionEffectType.RESISTANCE));
    }

    static double getIncomingDamageMultiplier(int resistanceLevel) {
        return Math.max(
                0,
                1.0 - resistanceLevel
                        * MobCombatSettingsConfig.getResistanceDamageMultiplier());
    }

    static double getOutgoingDamageMultiplier(
            int strengthLevel,
            int weaknessLevel) {
        double strengthBonus = strengthLevel
                * MobCombatSettingsConfig.getStrengthDamageMultiplier();
        double weaknessPenalty = weaknessLevel
                * MobCombatSettingsConfig.getWeaknessDamageMultiplier();
        return Math.max(0, 1.0 + strengthBonus - weaknessPenalty);
    }

    static double getCombinedDamageMultiplier(
            int sourceStrengthLevel,
            int sourceWeaknessLevel,
            int targetResistanceLevel) {
        return getOutgoingDamageMultiplier(
                sourceStrengthLevel,
                sourceWeaknessLevel)
                * getIncomingDamageMultiplier(targetResistanceLevel);
    }
}
