package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.combatsystem.PlayerDamagedByEliteMobHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BossCustomAttackDamage {

    public static double dealCustomDamage(LivingEntity damager, LivingEntity damagee, double damage) {

        if (damager.equals(damagee)) return 0;

        if (damagee.isInvulnerable() || damagee.getHealth() <= 0) return 0;

        if (damagee instanceof Player)
            if (!(((Player) damagee).getGameMode().equals(GameMode.SURVIVAL) ||
                    ((Player) damagee).getGameMode().equals(GameMode.ADVENTURE))) return 0;

        PlayerDamagedByEliteMobHandler.bypass = true;
        damagee.damage(damage, damager);
        damagee.setNoDamageTicks(0);

        return damage;
    }

}
