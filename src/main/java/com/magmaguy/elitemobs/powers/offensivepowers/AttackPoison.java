package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 12/12/2016.
 */
public class AttackPoison extends MinorPower implements Listener {

    public AttackPoison() {
        super(PowersConfig.getPower("attack_poison.yml"));
    }

    @EventHandler
    public void onHit(PlayerDamagedByEliteMobEvent event) {
        AttackPoison attackPoison = (AttackPoison) event.getEliteMobEntity().getPower(this);
        if (attackPoison == null) return;

        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 2, 3));
    }

}
