package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.Bombardment;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.concurrent.ThreadLocalRandom;

public class EnderDragonEnderFireballBombardment extends Bombardment {

    public EnderDragonEnderFireballBombardment() {
        super(PowersConfig.getPower("ender_dragon_ender_fireball_bombardment.yml"));
    }

    @Override
    public void taskBehavior(EliteEntity eliteEntity) {
        if (ThreadLocalRandom.current().nextDouble() > 0.1) return;
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(200, 100, 200))
            if (entity.getType().equals(EntityType.PLAYER))
                ((DragonFireball) eliteEntity.getLivingEntity().getWorld().spawnEntity(
                        eliteEntity.getLivingEntity().getLocation(),
                        EntityType.DRAGON_FIREBALL)).setDirection(
                        entity.getLocation().clone().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().normalize());
    }

}
