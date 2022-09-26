package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class Thunderstorm extends BossPower implements Listener {

    public Thunderstorm() {
        super(PowersConfig.getPower("thunderstorm.yml"));
    }

    public static void doThunderstorm(EliteEntity eliteEntity) {
        if (eliteEntity == null || !eliteEntity.getLivingEntity().isValid()) return;
        eliteEntity.getLivingEntity().setAI(false);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                if (counter > 20 * 5 || eliteEntity.getLivingEntity() == null || !eliteEntity.getLivingEntity().isValid()) {
                    cancel();
                    eliteEntity.getLivingEntity().setAI(true);
                    return;
                }

                if (counter % 2 == 0) {
                    Location randomLocation = eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(
                            ThreadLocalRandom.current().nextInt(-20, 20),
                            0,
                            ThreadLocalRandom.current().nextInt(-20, 20)));
                    lightningTask(randomLocation);
                }


                if (counter % 20 == 0) {
                    for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(20, 20, 20))
                        if (entity.getType().equals(EntityType.PLAYER))
                            lightningTask(entity.getLocation());
                }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    public static void lightningTask(Location location) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                if (counter > 20 * 3) {
                    LightningSpawnBypass.bypass();
                    location.getWorld().strikeLightning(location);
                    cancel();
                    return;
                }
                location.getWorld().spawnParticle(Particle.CRIT, location, 10, 0.5, 1.5, 0.5, 0.3);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        Thunderstorm thunderstorm = (Thunderstorm) event.getEliteMobEntity().getPower(this);
        if (thunderstorm == null) return;
        if (!eventIsValid(event, thunderstorm)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        thunderstorm.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doThunderstorm(event.getEliteMobEntity());

    }

}
