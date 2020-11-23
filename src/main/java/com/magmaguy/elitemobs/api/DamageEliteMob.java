package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.Player;

public class DamageEliteMob {

    public static void lowDamage(EliteMobEntity eliteMobEntity) {
        damageFormula(eliteMobEntity, DamageAmount.LOW);
    }

    public static void mediumDamage(EliteMobEntity eliteMobEntity) {
        damageFormula(eliteMobEntity, DamageAmount.MEDIUM);
    }

    public static void highDamage(EliteMobEntity eliteMobEntity) {
        damageFormula(eliteMobEntity, DamageAmount.HIGH);
    }

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

    private static void damageFormula(EliteMobEntity eliteMobEntity, DamageAmount damageAmount) {
        eliteMobEntity.damage(tierCalc(eliteMobEntity, damageAmount));
    }

    private static void damageFormula(Player player, EliteMobEntity eliteMobEntity, DamageAmount damageAmount) {
        BossCustomAttackDamage.dealCustomDamage(player, eliteMobEntity.getLivingEntity(), tierCalc(eliteMobEntity, damageAmount));
    }

    private static double tierCalc(EliteMobEntity eliteMobEntity, DamageAmount damageAmount) {
        switch (damageAmount) {
            case LOW:
                return eliteMobEntity.getTier() - 3 < 0 ? 0 : eliteMobEntity.getTier() - 3;
            case MEDIUM:
            default:
                return eliteMobEntity.getTier();
            case HIGH:
                return eliteMobEntity.getTier() + 3;
        }
    }

}
