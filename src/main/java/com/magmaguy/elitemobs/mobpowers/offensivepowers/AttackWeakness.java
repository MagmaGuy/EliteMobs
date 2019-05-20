package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class AttackWeakness extends MinorPower implements Listener {

    public AttackWeakness() {
        super("AttackWeakness", Material.TOTEM_OF_UNDYING);
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
