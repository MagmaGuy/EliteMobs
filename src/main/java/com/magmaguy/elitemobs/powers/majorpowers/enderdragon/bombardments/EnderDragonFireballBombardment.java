package com.magmaguy.elitemobs.powers.majorpowers.enderdragon.bombardments;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;

import java.util.concurrent.ThreadLocalRandom;

public class EnderDragonFireballBombardment extends Bombardment {

    public EnderDragonFireballBombardment() {
        super(PowersConfig.getPower("ender_dragon_fireball_bombardment.yml"));
    }

    @Override
    public void taskBehavior(EliteMobEntity eliteMobEntity) {
        if (ThreadLocalRandom.current().nextDouble() > 0.1) return;
        for (Entity entity : eliteMobEntity.getLivingEntity().getNearbyEntities(200, 100, 200))
            if (entity.getType().equals(EntityType.PLAYER)) {
                Fireball fireball = ((Fireball) eliteMobEntity.getLivingEntity().getWorld().spawnEntity(
                        eliteMobEntity.getLivingEntity().getLocation(),
                        EntityType.FIREBALL));
                fireball.setDirection(entity.getLocation().clone().subtract(eliteMobEntity.getLivingEntity().getLocation()).toVector().normalize());
                fireball.setYield(5F);
                EntityTracker.registerProjectileEntity(fireball);
            }
    }
}
