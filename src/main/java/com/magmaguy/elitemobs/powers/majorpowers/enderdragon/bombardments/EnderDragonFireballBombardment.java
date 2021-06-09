package com.magmaguy.elitemobs.powers.majorpowers.enderdragon.bombardments;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;

public class EnderDragonFireballBombardment extends Bombardment {
    public EnderDragonFireballBombardment() {
        super(PowersConfig.getPower("ender_dragon_fireball_bombardment.yml"));
    }

    @Override
    public void taskBehavior(EliteMobEntity eliteMobEntity) {
        if (super.firingTimer % 10 != 0)
            return;

        Vector direction = eliteMobEntity.getLivingEntity().getLocation().getDirection();

        float rotation = (float) (Math.atan2(direction.getX(), direction.getZ()) * 180 / Math.PI);

        Vector direction1 = new Vector(3, -4, 0);
        Vector direction2 = new Vector(-3, -4, 0);

        direction1 = direction1.rotateAroundY(rotation);
        direction2 = direction2.rotateAroundY(rotation);

        generateFireball(eliteMobEntity, direction1);
        generateFireball(eliteMobEntity, direction2);
    }

    private void generateFireball(EliteMobEntity eliteMobEntity, Vector vector) {
        Fireball fireball = ((Fireball) eliteMobEntity.getLivingEntity().getWorld().spawnEntity(
                eliteMobEntity.getLivingEntity().getLocation().clone().add(vector),
                EntityType.FIREBALL));
        fireball.setDirection(new Vector(0, -0.1, 0));
        fireball.setYield(5F);
        EntityTracker.registerProjectileEntity(fireball);
    }

}
