package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.BossPower;
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

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        MeteorShower meteorShower = (MeteorShower) event.getEliteMobEntity().getPower(this);
        if (meteorShower == null) return;
        if (!eventIsValid(event, meteorShower)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        meteorShower.doCooldown(20 * 20, event.getEliteMobEntity());
        doMeteorShower(event.getEliteMobEntity());

    }

    public static void doMeteorShower(EliteMobEntity eliteMobEntity) {
        eliteMobEntity.getLivingEntity().setAI(false);
        new BukkitRunnable() {
            int counter = 0;
            final Location initialLocation = eliteMobEntity.getLivingEntity().getLocation().clone();
            @Override
            public void run() {

                if (!eliteMobEntity.getLivingEntity().isValid()) {
                    cancel();
                    return;
                }

                if (counter > 10 * 20) {
                    cancel();
                    eliteMobEntity.getLivingEntity().setAI(true);
                    eliteMobEntity.getLivingEntity().teleport(initialLocation);
                    return;
                }

                counter++;

                doCloudEffect(eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)));

                if (counter > 2 * 20) {

                    doFireballs(eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)), eliteMobEntity);

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

    private static void doFireballs(Location location, EliteMobEntity eliteMobEntity) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            newLocation = newLocation.setDirection(new Vector(ThreadLocalRandom.current().nextDouble() - 0.5, -0.5, ThreadLocalRandom.current().nextDouble() - 0.5));
            Fireball fireball = (Fireball) location.getWorld().spawnEntity(newLocation, EntityType.FIREBALL);
            fireball.setShooter(eliteMobEntity.getLivingEntity());
            fireball.setDirection(fireball.getDirection().multiply(0.5));
        }
    }

}
