package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.mobconstructor.CombatSystem;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BossSpecialAttackDamage {

    public static boolean dealSpecialDamage(LivingEntity damager, LivingEntity damagee, double damage) {

        if (damagee.isInvulnerable() || damagee.getHealth() <= 0) return false;

        if (damagee instanceof Player) {

            if (!(((Player) damagee).getGameMode().equals(GameMode.SURVIVAL) ||
                    ((Player) damagee).getGameMode().equals(GameMode.ADVENTURE))) return false;

        }

        CombatSystem.addCustomEntity(damagee);
        damagee.damage(damage, damager);
        damagee.setNoDamageTicks(0);

        if (damagee instanceof Player && damagee.getHealth() <= 0)
            PlayerDeathMessageByEliteMob.initializeDeathMessage((Player) damagee, damager);

        return true;

    }

}
