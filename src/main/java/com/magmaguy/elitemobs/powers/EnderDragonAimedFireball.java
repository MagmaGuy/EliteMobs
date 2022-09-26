package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.Bombardment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;

public class EnderDragonAimedFireball extends Bombardment {

    public EnderDragonAimedFireball() {
        super(PowersConfig.getPower("ender_dragon_aimed_fireball.yml"));
    }

    @Override
    public void taskBehavior(EliteEntity eliteEntity) {
        if (super.firingTimer % 20 != 0)
            return;
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(200, 100, 200))
            if (entity.getType().equals(EntityType.PLAYER)) {
                Fireball fireball = ((Fireball) eliteEntity.getLivingEntity().getWorld().spawnEntity(
                        eliteEntity.getLivingEntity().getLocation(),
                        EntityType.FIREBALL));
                fireball.setDirection(entity.getLocation().clone().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().normalize());
                fireball.setYield(5F);
                EntityTracker.registerProjectileEntity(fireball);
            }
    }

}
