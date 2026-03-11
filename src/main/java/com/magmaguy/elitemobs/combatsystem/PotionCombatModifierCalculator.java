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

    private static double getEffectPercent(LivingEntity entity, PotionEffectType effectType, double perLevelMultiplier) {
        if (entity == null || !entity.hasPotionEffect(effectType)) return 0;

        PotionEffect potionEffect = entity.getPotionEffect(effectType);
        if (potionEffect == null) return 0;

        return (potionEffect.getAmplifier() + 1) * perLevelMultiplier;
    }

    public static double getIncomingDamageMultiplier(LivingEntity entity) {
        return Math.max(0, 1.0 - getEffectPercent(entity, PotionEffectType.RESISTANCE,
                MobCombatSettingsConfig.getResistanceDamageMultiplier()));
    }

    public static double getOutgoingDamageMultiplier(LivingEntity entity) {
        double strengthBonus = getEffectPercent(entity, PotionEffectType.STRENGTH,
                MobCombatSettingsConfig.getStrengthDamageMultiplier());
        double weaknessPenalty = getEffectPercent(entity, PotionEffectType.WEAKNESS,
                MobCombatSettingsConfig.getWeaknessDamageMultiplier());
        return Math.max(0, 1.0 + strengthBonus - weaknessPenalty);
    }
}
