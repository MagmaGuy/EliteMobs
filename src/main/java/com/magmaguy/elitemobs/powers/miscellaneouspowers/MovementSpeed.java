package com.magmaguy.elitemobs.powers.miscellaneouspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by MagmaGuy on 05/11/2016.
 */
public class MovementSpeed extends MinorPower {

    public MovementSpeed() {
        super(PowersConfig.getPower("movement_speed.yml"));
    }

    @Override
    public void applyPowers(LivingEntity livingEntity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                //todo: shouldn't this potion effect be 1?
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 1));
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

}