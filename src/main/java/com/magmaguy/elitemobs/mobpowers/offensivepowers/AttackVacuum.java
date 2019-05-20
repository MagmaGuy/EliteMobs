package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AttackVacuum extends MinorPower implements Listener {

    public AttackVacuum() {
        super("AttackVacuum", Material.LEAD);
    }

    @EventHandler
    public void onHit(PlayerDamagedByEliteMobEvent event) {
        AttackVacuum attackVacuum = (AttackVacuum) event.getEliteMobEntity().getPower(this);
        if (attackVacuum == null) return;

        event.getPlayer().setVelocity(event.getEliteMobEntity().getLivingEntity().getLocation().clone().subtract(event.getPlayer().getLocation()).toVector().normalize());
    }

}
