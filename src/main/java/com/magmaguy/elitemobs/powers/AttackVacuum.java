package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AttackVacuum extends MinorPower implements Listener {

    public AttackVacuum() {
        super(PowersConfig.getPower("attack_vacuum.yml"));
    }

    @EventHandler
    public void onHit(PlayerDamagedByEliteMobEvent event) {
        if (event.isCancelled()) return;
        AttackVacuum attackVacuum = (AttackVacuum) event.getEliteMobEntity().getPower(this);
        if (attackVacuum == null) return;

        if (event.getPlayer().isValid() && event.getEliteMobEntity().isValid() &&
                event.getPlayer().getWorld().equals(event.getEliteMobEntity().getLivingEntity().getWorld()))
            try {
                event.getPlayer().setVelocity(event.getEliteMobEntity().getLivingEntity().getLocation().clone().subtract(event.getPlayer().getLocation()).toVector().normalize());
            } catch (Exception ex) {
                //sometimes this results in non-finite vectors
            }
    }

}
