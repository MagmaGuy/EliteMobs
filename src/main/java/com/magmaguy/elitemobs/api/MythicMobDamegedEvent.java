package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.utils.Round;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import static com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.*;

public class MythicMobDamegedEvent implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onMythicMobDamaged(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Projectile) {
            if (e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                double eliteDamage = getEliteRangedDamage((Projectile) e.getDamager());
                double damage = e.getDamage() + eliteDamage;
                e.setDamage(EntityDamageEvent.DamageModifier.BASE, damage);
                return;
            }
        }
        if (!(e.getDamager() instanceof Player player)) return;
        Entity entity = e.getEntity();
        ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
        if (mob == null) return;
        double eliteDamage = 0;
        if (e.getCause().equals(EntityDamageEvent.DamageCause.THORNS)) {
            eliteDamage = getThornsDamage(player);
        } else if ((e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) || e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) && EliteItemManager.isEliteMobsItem(player.getInventory().getItemInMainHand()))) {
            eliteDamage = getEliteMeleeDamage(player, (LivingEntity) entity);
        }

        double damage = e.getDamage();

        damage = Round.twoDecimalPlaces(damage + eliteDamage);

        boolean criticalHit = false;
        criticalHit = isCriticalHit(player);
        if (criticalHit) damage *= 1.5;

        e.setDamage(EntityDamageEvent.DamageModifier.BASE, damage);
    }
}
