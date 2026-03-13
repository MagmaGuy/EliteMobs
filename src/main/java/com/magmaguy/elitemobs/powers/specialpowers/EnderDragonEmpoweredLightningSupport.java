package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EnderDragonEmpoweredLightningSupport {
    private EnderDragonEmpoweredLightningSupport() {
    }

    public static void lightningTask(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                if (counter > 20 * 3) {
                    LightningSpawnBypass.bypass();
                    location.getWorld().strikeLightning(location);
                    Fireball fireball = (Fireball) location.getWorld().spawnEntity(location, EntityType.FIREBALL);
                    EntityTracker.registerProjectileEntity(fireball);
                    fireball.setDirection(new Vector(0, -3, 0));
                    fireball.setVelocity(new Vector(0, -3, 0));
                    fireball.setYield(5F);
                    EliteProjectile.signExplosiveWithPower(fireball, "ender_dragon_empowered_lightning.yml");
                    cancel();
                    return;
                }
                location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 10, 0.5, 1.5, 0.5, 0.3);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }
}
