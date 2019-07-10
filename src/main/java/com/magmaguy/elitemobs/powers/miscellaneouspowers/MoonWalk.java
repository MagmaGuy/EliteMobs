package com.magmaguy.elitemobs.powers.miscellaneouspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class MoonWalk extends MinorPower {

    public MoonWalk() {
        super(PowersConfig.getPower("moonwalk.yml"));
    }

    @Override
    public void applyPowers(LivingEntity livingEntity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000, 3));
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);

    }

}
