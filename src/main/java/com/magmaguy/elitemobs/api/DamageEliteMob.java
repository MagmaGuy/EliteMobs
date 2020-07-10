package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.Player;

public class DamageEliteMob {

    public static void lowDamage(Player player, EliteMobEntity eliteMobEntity) {
        damageFormula(player, eliteMobEntity, DamageAmount.LOW);
    }

    public static void mediumDamage(Player player, EliteMobEntity eliteMobEntity) {
        damageFormula(player, eliteMobEntity, DamageAmount.MEDIUM);
    }

    public static void highDamage(Player player, EliteMobEntity eliteMobEntity) {
        damageFormula(player, eliteMobEntity, DamageAmount.HIGH);
    }

    private enum DamageAmount {
        LOW,
        MEDIUM,
        HIGH
    }

    private static void damageFormula(Player player, EliteMobEntity eliteMobEntity, DamageAmount damageAmount) {

        double tier = 0;

        switch (damageAmount) {
            case LOW:
                tier = eliteMobEntity.getTier() - 3 < 0 ? 0 : eliteMobEntity.getTier() - 3;
                break;
            case MEDIUM:
                tier = eliteMobEntity.getTier();
                break;
            case HIGH:
                tier = eliteMobEntity.getTier() + 3;
                break;
        }

        //This is basically a modified version of EliteMobDamagedByPlayerHandler playerToEliteDamageFormula

        double tierDifference = tier - eliteMobEntity.getTier();

        /*
        This applies secondary enchantments, that is, it applies enchantments that only affect specific mob types
        such as smite which only works with undead mobs
         */
        double newTargetHitsToKill = eliteMobEntity.getDefaultMaxHealth() / 2; //10 for zombies

        double finalDamage = (eliteMobEntity.getDefaultMaxHealth() / newTargetHitsToKill +
                eliteMobEntity.getDefaultMaxHealth() / newTargetHitsToKill * tierDifference * 0.2);

        //Apply health multiplier
        finalDamage /= eliteMobEntity.getHealthMultiplier();

        //apply config multiplier
        finalDamage *= MobCombatSettingsConfig.damageToEliteMultiplier;

        finalDamage = finalDamage < 1 ? 1 : finalDamage;

        BossCustomAttackDamage.dealCustomDamage(player, eliteMobEntity.getLivingEntity(), finalDamage);

    }

}
