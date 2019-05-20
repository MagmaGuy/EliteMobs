package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class AttackFire extends MinorPower implements Listener {

    public AttackFire() {
        super("AttackFire", Material.BLAZE_POWDER);
    }

    @EventHandler
    public void attackFire(PlayerDamagedByEliteMobEvent event) {

        AttackFire attackFire = (AttackFire) event.getEliteMobEntity().getPower(this);
        if (attackFire == null) return;

        event.getPlayer().setFireTicks(40);

    }

}
