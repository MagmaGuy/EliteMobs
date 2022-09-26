package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ArrowRain extends MinorPower implements Listener {

    public ArrowRain() {
        super(PowersConfig.getPower("arrow_rain.yml"));
    }

    public static void doArrowRain(EliteEntity eliteEntity) {
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
                    eliteEntity.getLivingEntity().teleport(initialLocation);
                    return;
                }

                counter++;
                MeteorShower.doCloudEffect(eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)));
                if (counter > 20)
                    doArrows(eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)), eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static void doArrows(Location location, EliteEntity eliteEntity) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            newLocation = newLocation.setDirection(new Vector(ThreadLocalRandom.current().nextDouble() - 0.5, -0.5, ThreadLocalRandom.current().nextDouble() - 0.5));
            Arrow arrow = (Arrow) Objects.requireNonNull(location.getWorld()).spawnEntity(newLocation, EntityType.ARROW);
            arrow.setShooter(eliteEntity.getLivingEntity());
        }
    }

    @EventHandler
    public void onEliteDamaged(EliteMobDamagedByPlayerEvent event) {

        ArrowRain arrowRain = (ArrowRain) event.getEliteMobEntity().getPower(this);
        if (arrowRain == null) return;
        if (!eventIsValid(event, arrowRain)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.15) return;

        arrowRain.doGlobalCooldown(20 * 15, event.getEliteMobEntity());
        doArrowRain(event.getEliteMobEntity());

    }

}
