package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class AttackWeakness extends MinorPower implements Listener {

    public AttackWeakness() {
        super(PowersConfig.getPower("attack_weakness.yml"));
    }

    @EventHandler
    public void attackWeakness(EliteMobTargetPlayerEvent event) {
        AttackWeakness attackWeakness = (AttackWeakness) event.getEliteMobEntity().getPower(this);
        if (attackWeakness == null) return;
        if (attackWeakness.isCooldown()) return;

        attackWeakness.doCooldown(20 * 10);
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 3, 0));
    }

}
