package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.powers.BossPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Flamethrower extends BossPower implements Listener {

    public Flamethrower() {
        super(PowersConfig.getPower("flamethrower.yml"));
    }

    /**
     * Warning phase
     *
     * @param eliteMobEntity
     */
    private void doFlamethrowerPhase1(EliteMobEntity eliteMobEntity, Location fixedPlayerLocation) {

        eliteMobEntity.getLivingEntity().setAI(false);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                doParticleEffect(eliteMobEntity, fixedPlayerLocation, Particle.SMOKE_NORMAL);
                counter++;

                if (counter < 20 * 2) return;
                doFlamethrowerPhase2(eliteMobEntity, fixedPlayerLocation);
                cancel();

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {

        Flamethrower flameThrower = (Flamethrower) event.getEliteMobEntity().getPower(this);
        if (flameThrower == null) return;
        if (!eventIsValid(event, flameThrower)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        flameThrower.doCooldown(20 * 20, event.getEliteMobEntity());
        doFlamethrowerPhase1(event.getEliteMobEntity(), event.getPlayer().getLocation().clone());

    }

    private void doParticleEffect(EliteMobEntity eliteMobEntity, Location fixedPlayerLocation, Particle particle) {
        Vector directionVector = fixedPlayerLocation.clone().subtract(eliteMobEntity.getLivingEntity().getLocation()).toVector().normalize();
        for (int i = 0; i < 5; i++) {
            eliteMobEntity.getLivingEntity().getWorld().spawnParticle(
                    particle,
                    eliteMobEntity.getLivingEntity().getEyeLocation().clone().add(directionVector.getX(), -0.5, directionVector.getZ()),
                    0,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getX(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getY(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getZ(),
                    ThreadLocalRandom.current().nextDouble() + 0.05);
        }
    }

    /**
     * Damage phase
     *
     * @param eliteMobEntity
     */
    private void doFlamethrowerPhase2(EliteMobEntity eliteMobEntity, Location fixedPlayerLocation) {
        List<Location> damagePoints = generateDamagePoints(eliteMobEntity, fixedPlayerLocation);
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                doParticleEffect(eliteMobEntity, fixedPlayerLocation, Particle.FLAME);
                doDamage(damagePoints, eliteMobEntity);
                timer++;
                if (timer < 20 * 3) return;
                doFlamethrowerPhase3(eliteMobEntity, fixedPlayerLocation);
                cancel();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static List<Location> generateDamagePoints(EliteMobEntity eliteMobEntity, Location fixedPlayerLocation) {
        List<Location> locations = new ArrayList<>();
        Location eliteMobLocation = eliteMobEntity.getLivingEntity().getLocation().clone();
        Vector toPlayerVector = fixedPlayerLocation.clone().subtract(eliteMobLocation).toVector().normalize().multiply(0.5);
        for (int i = 0; i < 40; i++)
            locations.add(eliteMobLocation.add(toPlayerVector).clone());
        return locations;
    }

    private static void doDamage(List<Location> locations, EliteMobEntity eliteMobEntity) {
        for (Location location : locations)
            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5))
                if (entity instanceof LivingEntity) {
                    if (eliteMobEntity.getLivingEntity().equals(entity)) continue;
                    BossCustomAttackDamage.dealCustomDamage(eliteMobEntity.getLivingEntity(), (LivingEntity) entity, 1);
                }

    }

    /**
     * Cooldown phase
     *
     * @param eliteMobEntity
     */
    private void doFlamethrowerPhase3(EliteMobEntity eliteMobEntity, Location fixedPlayerLocation) {
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                timer++;
                doParticleEffect(eliteMobEntity, fixedPlayerLocation, Particle.SMOKE_NORMAL);
                if (timer < 20) return;
                cancel();
                eliteMobEntity.getLivingEntity().setAI(true);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

}
