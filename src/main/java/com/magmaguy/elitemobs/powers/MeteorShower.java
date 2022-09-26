package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class MeteorShower extends BossPower implements Listener {
    public MeteorShower() {
        super(PowersConfig.getPower("meteor_shower.yml"));
    }

    public static void doMeteorShower(EliteEntity eliteEntity) {
        eliteEntity.getLivingEntity().setAI(false);
        new BukkitRunnable() {
            final Location initialLocation = eliteEntity.getLivingEntity().getLocation().clone();
            int counter = 0;

            @Override
            public void run() {

                if (!eliteEntity.isValid()) {
                    cancel();
                    return;
                }

                if (counter > 10 * 20) {
                    cancel();
                    eliteEntity.getLivingEntity().setAI(true);
                    eliteEntity.getLivingEntity().teleport(initialLocation);
                    return;
                }

                counter++;

                doCloudEffect(eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)));

                if (counter > 2 * 20) {

                    doFireballs(eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)), eliteEntity);

                }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    public static void doCloudEffect(Location location) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            location.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, newLocation, 1, 0, 0, 0, 0);
        }
    }

    private static void doFireballs(Location location, EliteEntity eliteEntity) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            newLocation = newLocation.setDirection(new Vector(ThreadLocalRandom.current().nextDouble() - 0.5, -0.5, ThreadLocalRandom.current().nextDouble() - 0.5));
            Fireball fireball = (Fireball) location.getWorld().spawnEntity(newLocation, EntityType.FIREBALL);
            fireball.setShooter(eliteEntity.getLivingEntity());
            fireball.setDirection(fireball.getDirection().multiply(0.5));
        }
    }

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        MeteorShower meteorShower = (MeteorShower) event.getEliteMobEntity().getPower(this);
        if (meteorShower == null) return;
        if (!eventIsValid(event, meteorShower)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        meteorShower.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doMeteorShower(event.getEliteMobEntity());

    }

}
