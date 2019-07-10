package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 30/04/2017.
 */
public class AttackBlinding extends MinorPower implements Listener {

    public AttackBlinding() {
        super(PowersConfig.getPower("attack_blinding.yml"));
    }

    @EventHandler
    public void attackBlinding(PlayerDamagedByEliteMobEvent event) {
        AttackBlinding attackBlinding = (AttackBlinding) event.getEliteMobEntity().getPower(this);
        if (attackBlinding == null) return;
        if (attackBlinding.isCooldown()) return;

        attackBlinding.doCooldown(20 * 10);
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 3));
    }

}
