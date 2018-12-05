package com.magmaguy.elitemobs.mobpowers.defensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class Invisibility extends MinorPower {

    @Override
    public void applyPowers(Entity entity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

}
