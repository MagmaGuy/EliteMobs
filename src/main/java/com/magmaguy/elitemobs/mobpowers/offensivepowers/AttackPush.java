package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by MagmaGuy on 05/11/2016.
 */
public class AttackPush extends MinorPower implements Listener {

    public AttackPush() {
        super("AttackPush", Material.PISTON);
    }

    @EventHandler
    public void attackPush(PlayerDamagedByEliteMobEvent event) {
        AttackPush attackPush = (AttackPush) event.getEliteMobEntity().getPower(this);
        if (attackPush == null) return;
        if (attackPush.isCooldown()) return;

        attackPush.doCooldown(20 * 10);
        event.getPlayer().setVelocity(event.getPlayer().getLocation().clone().subtract(event.getEliteMobEntity().getLivingEntity().getLocation()).toVector().normalize().multiply(3));
    }

}
