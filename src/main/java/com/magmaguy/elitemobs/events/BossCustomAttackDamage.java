package com.magmaguy.elitemobs.events;

import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BossCustomAttackDamage {

    public static boolean dealCustomDamage(LivingEntity damager, LivingEntity damagee, double damage) {

        if (damager.equals(damagee)) return false;

        if (damagee.isInvulnerable() || damagee.getHealth() <= 0) return false;

        if (damagee instanceof Player)
            if (!(((Player) damagee).getGameMode().equals(GameMode.SURVIVAL) ||
                    ((Player) damagee).getGameMode().equals(GameMode.ADVENTURE))) return false;

        damagee.damage(damage, damager);
        damagee.setNoDamageTicks(0);

        return true;
    }

}
