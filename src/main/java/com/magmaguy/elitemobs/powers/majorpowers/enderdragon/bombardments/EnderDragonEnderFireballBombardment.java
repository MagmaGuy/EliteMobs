package com.magmaguy.elitemobs.powers.majorpowers.enderdragon.bombardments;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.concurrent.ThreadLocalRandom;

public class EnderDragonEnderFireballBombardment extends Bombardment {

    public EnderDragonEnderFireballBombardment() {
        super(PowersConfig.getPower("ender_dragon_ender_fireball_bombardment.yml"));
    }

    @Override
    public void taskBehavior(EliteMobEntity eliteMobEntity) {
        if (ThreadLocalRandom.current().nextDouble() > 0.1) return;
        for (Entity entity : eliteMobEntity.getLivingEntity().getNearbyEntities(200, 100, 200))
            if (entity.getType().equals(EntityType.PLAYER))
                ((DragonFireball) eliteMobEntity.getLivingEntity().getWorld().spawnEntity(
                        eliteMobEntity.getLivingEntity().getLocation(),
                        EntityType.DRAGON_FIREBALL)).setDirection(
                        entity.getLocation().clone().subtract(eliteMobEntity.getLivingEntity().getLocation()).toVector().normalize());
    }

}
