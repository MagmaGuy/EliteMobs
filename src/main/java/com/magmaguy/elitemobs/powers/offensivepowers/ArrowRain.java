package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import com.magmaguy.elitemobs.powers.bosspowers.MeteorShower;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class ArrowRain extends MinorPower implements Listener {

    public ArrowRain() {
        super(PowersConfig.getPower("arrow_rain.yml"));
    }

    @EventHandler
    public void onEliteDamaged(EliteMobDamagedByPlayerEvent event) {

        ArrowRain arrowRain = (ArrowRain) event.getEliteMobEntity().getPower(this);
        if (arrowRain == null) return;
        if (!eventIsValid(event, arrowRain)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.15) return;

        arrowRain.doCooldown(20 * 15, event.getEliteMobEntity());
        doArrowRain(event.getEliteMobEntity());

    }

    public static void doArrowRain(EliteMobEntity eliteMobEntity) {
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

                MeteorShower.doCloudEffect(eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)));

                if (counter > 20) {

                    doArrows(eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)), eliteMobEntity);

                }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }


    private static void doArrows(Location location, EliteMobEntity eliteMobEntity) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            newLocation = newLocation.setDirection(new Vector(ThreadLocalRandom.current().nextDouble() - 0.5, -0.5, ThreadLocalRandom.current().nextDouble() - 0.5));
            Arrow arrow = (Arrow) location.getWorld().spawnEntity(newLocation, EntityType.ARROW);
            arrow.setShooter(eliteMobEntity.getLivingEntity());
        }
    }

}
