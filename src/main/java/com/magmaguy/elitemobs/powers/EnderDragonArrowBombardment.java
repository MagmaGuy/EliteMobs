package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.Bombardment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class EnderDragonArrowBombardment extends Bombardment {

    public EnderDragonArrowBombardment() {
        super(PowersConfig.getPower("ender_dragon_arrow_bombardment.yml"));
    }

    @Override
    public void taskBehavior(EliteEntity eliteEntity) {
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(200, 100, 200))

            if (entity.getType().equals(EntityType.PLAYER)) {
                Vector shotVector = entity.getLocation().clone().add(new Vector(0, 1, 0))
                        .subtract(eliteEntity.getLivingEntity().getLocation().clone())
                        .add(new Vector(ThreadLocalRandom.current().nextDouble() - 0.5,
                                ThreadLocalRandom.current().nextDouble() - 0.5,
                                ThreadLocalRandom.current().nextDouble() - 0.5))
                        .toVector().normalize().multiply(2);

                Projectile arrow = EliteProjectile.create(EntityType.ARROW, eliteEntity.getLivingEntity(), shotVector, false);

                //anti-lag measure, culls arrows after 4 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        arrow.remove();
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 20L * 4);
            }
    }
}
