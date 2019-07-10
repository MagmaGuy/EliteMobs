package com.magmaguy.elitemobs.powers.defensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class Invisibility extends MinorPower {

    public Invisibility() {
        super(PowersConfig.getPower("invisibility.yml"));
    }

    @Override
    public void applyPowers(LivingEntity livingEntity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

}
