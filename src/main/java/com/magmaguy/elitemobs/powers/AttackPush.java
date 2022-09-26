package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by MagmaGuy on 05/11/2016.
 */
public class AttackPush extends MinorPower implements Listener {

    public AttackPush() {
        super(PowersConfig.getPower("attack_push.yml"));
    }

    @EventHandler
    public void attackPush(PlayerDamagedByEliteMobEvent event) {
        if (event.isCancelled()) return;
        AttackPush attackPush = (AttackPush) event.getEliteMobEntity().getPower(this);
        if (!event.getPlayer().isValid()) return;
        if (!event.getEliteMobEntity().isValid()) return;
        if (!event.getPlayer().getWorld().equals(event.getEliteMobEntity().getLivingEntity().getWorld())) return;
        if (attackPush == null) return;
        if (attackPush.isInGlobalCooldown()) return;

        attackPush.doGlobalCooldown(20 * 10);
        try {
            event.getPlayer().setVelocity(event.getPlayer().getLocation().clone().subtract(event.getEliteMobEntity().getLivingEntity().getLocation()).toVector().normalize().multiply(3));
        } catch (Exception ex) {
        }
    }

}
