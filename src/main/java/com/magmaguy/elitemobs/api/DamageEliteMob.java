package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.Player;

public class DamageEliteMob {

    public static double lowDamage(EliteMobEntity eliteMobEntity) {
        return damageFormula(eliteMobEntity, DamageAmount.LOW);
    }

    public static double mediumDamage(EliteMobEntity eliteMobEntity) {
        return damageFormula(eliteMobEntity, DamageAmount.MEDIUM);
    }

    public static double highDamage(EliteMobEntity eliteMobEntity) {
        return damageFormula(eliteMobEntity, DamageAmount.HIGH);
    }

    public static double lowDamage(Player player, EliteMobEntity eliteMobEntity) {
        return damageFormula(player, eliteMobEntity, DamageAmount.LOW);
    }

    public static double mediumDamage(Player player, EliteMobEntity eliteMobEntity) {
        return damageFormula(player, eliteMobEntity, DamageAmount.MEDIUM);
    }

    public static double highDamage(Player player, EliteMobEntity eliteMobEntity) {
        return damageFormula(player, eliteMobEntity, DamageAmount.HIGH);
    }

    public enum DamageAmount {
        LOW,
        MEDIUM,
        HIGH
    }

    private static double damageFormula(EliteMobEntity eliteMobEntity, DamageAmount damageAmount) {
        return eliteMobEntity.damage(tierCalc(eliteMobEntity, damageAmount));
    }

    private static double damageFormula(Player player, EliteMobEntity eliteMobEntity, DamageAmount damageAmount) {
        return BossCustomAttackDamage.dealCustomDamage(player, eliteMobEntity.getLivingEntity(), tierCalc(eliteMobEntity, damageAmount));
    }

    public static double getDamageValue(EliteMobEntity eliteMobEntity, DamageAmount damageAmount) {
        return tierCalc(eliteMobEntity, damageAmount);
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
