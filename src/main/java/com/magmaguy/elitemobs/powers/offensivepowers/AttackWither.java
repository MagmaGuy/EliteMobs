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
public class AttackWither extends MinorPower implements Listener {

    public AttackWither() {
        super(PowersConfig.getPower("attack_wither.yml"));
    }

    @EventHandler
    public void onHit(PlayerDamagedByEliteMobEvent event) {
        AttackWither attackWither = (AttackWither) event.getEliteMobEntity().getPower(this);
        if (attackWither == null) return;

        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 2, 1));
    }

}
