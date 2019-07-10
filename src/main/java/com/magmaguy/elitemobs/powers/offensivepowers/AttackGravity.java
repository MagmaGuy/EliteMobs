package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 05/11/2016.
 */
public class AttackGravity extends MinorPower implements Listener {

    public AttackGravity() {
        super(PowersConfig.getPower("attack_gravity.yml"));
    }

    @EventHandler
    public void attackGravity(PlayerDamagedByEliteMobEvent event) {
        AttackGravity attackGravity = (AttackGravity) event.getEliteMobEntity().getPower(this);
        if (attackGravity == null) return;
        if (attackGravity.isCooldown()) return;

        attackGravity.doCooldown(20 * 10);
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 2 * 20, 3));
    }

}
