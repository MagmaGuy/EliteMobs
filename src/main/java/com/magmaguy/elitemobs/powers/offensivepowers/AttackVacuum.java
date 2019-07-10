package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AttackVacuum extends MinorPower implements Listener {

    public AttackVacuum() {
        super(PowersConfig.getPower("attack_vacuum.yml"));
    }

    @EventHandler
    public void onHit(PlayerDamagedByEliteMobEvent event) {
        AttackVacuum attackVacuum = (AttackVacuum) event.getEliteMobEntity().getPower(this);
        if (attackVacuum == null) return;

        event.getPlayer().setVelocity(event.getEliteMobEntity().getLivingEntity().getLocation().clone().subtract(event.getPlayer().getLocation()).toVector().normalize());
    }

}
