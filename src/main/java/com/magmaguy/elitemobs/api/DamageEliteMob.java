package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.entity.Player;

public class DamageEliteMob {

    public static double lowDamage(EliteEntity eliteEntity) {
        return damageFormula(eliteEntity, DamageAmount.LOW);
    }

    public static double mediumDamage(EliteEntity eliteEntity) {
        return damageFormula(eliteEntity, DamageAmount.MEDIUM);
    }

    public static double highDamage(EliteEntity eliteEntity) {
        return damageFormula(eliteEntity, DamageAmount.HIGH);
    }

    public static double lowDamage(Player player, EliteEntity eliteEntity) {
        return damageFormula(player, eliteEntity, DamageAmount.LOW);
    }

    public static double mediumDamage(Player player, EliteEntity eliteEntity) {
        return damageFormula(player, eliteEntity, DamageAmount.MEDIUM);
    }

    public static double highDamage(Player player, EliteEntity eliteEntity) {
        return damageFormula(player, eliteEntity, DamageAmount.HIGH);
    }

    private static double damageFormula(EliteEntity eliteEntity, DamageAmount damageAmount) {
        return eliteEntity.damage(tierCalc(eliteEntity, damageAmount));
    }

    private static double damageFormula(Player player, EliteEntity eliteEntity, DamageAmount damageAmount) {
        return BossCustomAttackDamage.dealCustomDamage(player, eliteEntity.getLivingEntity(), tierCalc(eliteEntity, damageAmount));
    }

    public static double getDamageValue(EliteEntity eliteEntity, DamageAmount damageAmount) {
        return tierCalc(eliteEntity, damageAmount);
    }

    private static double tierCalc(EliteEntity eliteEntity, DamageAmount damageAmount) {
        switch (damageAmount) {
            case LOW:
                return Math.max(eliteEntity.getLevel() - 3, 0);
            case MEDIUM:
            default:
                return eliteEntity.getLevel();
            case HIGH:
                return eliteEntity.getLevel() + 3;
        }
    }

    public enum DamageAmount {
        LOW,
        MEDIUM,
        HIGH
    }

}
