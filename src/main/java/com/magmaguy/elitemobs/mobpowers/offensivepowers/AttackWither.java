package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;

/**
 * Created by MagmaGuy on 12/12/2016.
 */
public class AttackWither extends MinorPower implements Listener {

    private static HashSet<EliteMobEntity> cooldownList = new HashSet<>();

    public AttackWither() {
        super("AttackWither", Material.WITHER_SKELETON_SKULL);
    }

    @EventHandler
    public void onHit(PlayerDamagedByEliteMobEvent event) {
        AttackWither attackWither = (AttackWither) event.getEliteMobEntity().getPower(this);
        if (attackWither == null) return;

        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 2, 1));
    }

}
