package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class DeathSlice extends BossPower implements Listener {

    public DeathSlice() {
        super(PowersConfig.getPower("death_slice.yml"));
    }

    private static void doDeathSlice(EliteEntity eliteEntity) {
        ArrayList<Location> locations = raytracedLocationList(eliteEntity.getLivingEntity().getLocation());
        eliteEntity.getLivingEntity().setAI(false);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 20 * 5 || !eliteEntity.isValid()) {
                    if (eliteEntity.getLivingEntity() != null)
                        eliteEntity.getLivingEntity().setAI(true);
                    cancel();
                    return;
                }

                if (counter < 20 * 2.5) {
                    for (Location location : locations)
                        doWarningParticle(location);
                } else {
                    for (Location location : locations)
                        doDamagePhase(location, eliteEntity);
                }

                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 2);

    }

    private static ArrayList<Location> raytracedLocationList(Location originalLocation) {
        ArrayList<Location> locations = new ArrayList<>();
        int safeSide = ThreadLocalRandom.current().nextInt(16);
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 16; j++)
                for (int k = 0; k < 2; k++) {
                    if (safeSide == j)
                        continue;
                    locations.add(originalLocation.clone().add(GenericRotationMatrixMath.applyRotation(0, 1, 0, 16, i, 0, 0, j)).add(new Vector(0, k, 0)));
                }

        return locations;
    }

    private static void doWarningParticle(Location location) {
        if (ThreadLocalRandom.current().nextDouble() < 0.3)
            location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 1, 0.05, 0.05, 0.05, 0.05);
    }

    private static void doDamagePhase(Location location, EliteEntity eliteEntity) {
        if (ThreadLocalRandom.current().nextDouble() < 0.3)
            location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0.1, 0.1, 0.1, 0.05);
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5))
            if (entity instanceof LivingEntity && !EntityTracker.isEliteMob(entity))
                BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), (LivingEntity) entity, 1);
    }

    @EventHandler
    public void onEliteDamaged(EliteMobDamagedByPlayerEvent event) {
        DeathSlice deathSlice = (DeathSlice) event.getEliteMobEntity().getPower(this);
        if (deathSlice == null) return;
        if (deathSlice.isInGlobalCooldown()) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.10) return;
        deathSlice.doGlobalCooldown(20 * 20);

        doDeathSlice(event.getEliteMobEntity());
    }

}
