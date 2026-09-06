package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.Objects;

/** Class damage attribution and actual health loss around the native delivery seam. */
public final class ClassAbilityDamage {
    @FunctionalInterface
    public interface Delivery {
        void damage(LivingEntity target, double amount, Entity source);
    }

    public static final Delivery NATIVE = (target, amount, source) ->
            Objects.requireNonNull(NMSManager.getAdapter(), "Class damage requires the native adapter")
                    .damageWithoutCooldown(target, amount, source);

    private final Delivery delivery;

    ClassAbilityDamage(Delivery delivery) {
        this.delivery = Objects.requireNonNull(delivery, "delivery");
    }

    double apply(Player caster, Projectile projectile, LivingEntity target, double amount,
                 CombatDamageContext.ClassAbilityDamageDomain domain) {
        if (target == null || !target.isValid() || target.isDead() || target.getHealth() <= 0D
                || !Double.isFinite(amount) || amount <= 0D) return 0D;
        double before = target.getHealth() + target.getAbsorptionAmount();
        Runnable hit = () -> CombatDamageContext.runClassAbilityDamage(domain,
                () -> delivery.damage(target, amount, projectile == null ? caster : projectile));
        if (projectile == null) hit.run();
        else CustomModel.runProjectileDamageBypass(hit);
        if (!target.isValid()) return Math.min(before, amount);
        double after = Math.max(0D, target.getHealth()) + Math.max(0D, target.getAbsorptionAmount());
        return Math.max(0D, before - after);
    }
}
