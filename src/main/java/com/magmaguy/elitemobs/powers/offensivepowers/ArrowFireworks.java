package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import com.magmaguy.elitemobs.utils.NonSolidBlockTypes;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class ArrowFireworks extends MinorPower implements Listener {

    public ArrowFireworks() {
        super(PowersConfig.getPower("arrow_fireworks.yml"));
    }

    @EventHandler
    public void onEliteDamagedByPlayer(EliteMobDamagedByPlayerEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEliteMobEntity().getLivingEntity() instanceof Monster)) return;
        ArrowFireworks arrowFireworks = (ArrowFireworks) event.getEliteMobEntity().getPower(this);
        if (arrowFireworks == null) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.15) return;
        doArrowFireworks(event.getEliteMobEntity());

    }

    private static void doArrowFireworks(EliteMobEntity eliteMobEntity) {

        Location centeredLocation = eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 3, 0));

        for (int x = 0; x < eliteMobEntity.getDamagers().size(); x++) {

            Location newLocation = centeredLocation.clone();

            boolean validBlockFound = false;
            for (int i = 0; i < 5; i++) {
                Vector randomizedVector = new Vector(ThreadLocalRandom.current().nextInt(9) - 4, 0, ThreadLocalRandom.current().nextInt(9) - 4);
                newLocation = centeredLocation.clone().add(randomizedVector);
                if (NonSolidBlockTypes.isPassthrough(newLocation.getBlock().getType())) {
                    validBlockFound = true;
                    break;
                }
            }

            if (!validBlockFound) return;

            SpectralArrow rocketArrow = (SpectralArrow) newLocation.getWorld().spawnEntity(newLocation, EntityType.SPECTRAL_ARROW);
            rocketArrow.setShooter(eliteMobEntity.getLivingEntity());
            rocketArrow.setVelocity(new Vector(0, 0.5, 0));
            rocketArrow.setGravity(false);
            rocketArrow.setGlowing(true);

            new BukkitRunnable() {
                int counter = 0;

                @Override
                public void run() {
                    if (!rocketArrow.isValid() || eliteMobEntity.getLivingEntity().isDead()) {
                        cancel();
                        return;
                    }

                    if (counter < 20 * 1.5) {
                        rocketArrow.getWorld().spawnParticle(Particle.CRIT, rocketArrow.getLocation(), 1);
                    } else {

                        for (int i = 0; i < 30; i++) {
                            Arrow fireworkArrow = (Arrow) rocketArrow.getWorld().spawnEntity(rocketArrow.getLocation(), EntityType.ARROW);
                            Vector randomizedDirection = new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) * 2, (ThreadLocalRandom.current().nextDouble() - 0.5) * 2, (ThreadLocalRandom.current().nextDouble() - 0.5) * 2);
                            fireworkArrow.setVelocity(randomizedDirection);
                            fireworkArrow.setShooter(eliteMobEntity.getLivingEntity());
                            fireworkArrow.setGlowing(true);
                        }

                        rocketArrow.remove();

                        cancel();
                    }

                    counter++;


                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        }

    }

}
