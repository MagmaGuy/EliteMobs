package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class SpiritWalk extends BossPower implements Listener {

    private int hitCounter = 0;

    public SpiritWalk() {
        super(PowersConfig.getPower("spirit_walk.yml"));
    }

    public static void spiritWalkAnimation(EliteEntity eliteEntity, Location entityLocation, Location finalLocation) {

        eliteEntity.getLivingEntity().setAI(false);
        eliteEntity.getLivingEntity().setInvulnerable(true);
        eliteEntity.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 10, 1));
        Vector toDestination = finalLocation.clone().subtract(entityLocation.clone()).toVector().normalize().divide(new Vector(2, 2, 2));
        eliteEntity.setCombatGracePeriod(20 * 20);

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (!eliteEntity.isValid()) {
                    cancel();
                    return;
                }

                if (eliteEntity.getLivingEntity().getLocation().clone().distance(finalLocation) < 2 || counter > 20 * 10) {

                    eliteEntity.getLivingEntity().teleport(finalLocation);
                    eliteEntity.getLivingEntity().setAI(true);
                    eliteEntity.getLivingEntity().setInvulnerable(false);
                    eliteEntity.getLivingEntity().removePotionEffect(PotionEffectType.GLOWING);
                    cancel();

                }

                eliteEntity.getLivingEntity().teleport(eliteEntity.getLivingEntity().getLocation().clone().add(toDestination.clone()));

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    public static void spiritWalkRegionalBossAnimation(EliteEntity eliteEntity, Location entityLocation, Location finalLocation) {
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, bukkitTask -> {
                    if (eliteEntity.getLivingEntity() == null) return;
                    eliteEntity.getLivingEntity().setAI(false);
                    eliteEntity.getLivingEntity().setInvulnerable(true);
                    Vector toDestination = finalLocation.clone().subtract(entityLocation.clone()).toVector().normalize().divide(new Vector(2, 2, 2));

                    Entity vehicle = null;

                    if (eliteEntity.getLivingEntity().isInsideVehicle()) {
                        vehicle = eliteEntity.getLivingEntity().getVehicle();
                        if (vehicle instanceof LivingEntity)
                            ((LivingEntity) vehicle).setAI(false);
                        vehicle.setInvulnerable(true);
                        if (((CustomBossEntity) eliteEntity).getPhaseBossEntity() != null)
                            vehicle.remove();
                    }

                    new BukkitRunnable() {
                        final Entity vehicle = eliteEntity.getLivingEntity().getVehicle();

                        int counter = 0;

                        @Override
                        public void run() {
                            if (!eliteEntity.isValid()) {
                                cancel();
                                return;
                            }

                            if (eliteEntity.getLivingEntity().isInsideVehicle())
                                eliteEntity.getLivingEntity().leaveVehicle();

                            if (eliteEntity.getLivingEntity().getLocation().clone().distance(finalLocation) < 2 || counter > 20 * 10) {

                                eliteEntity.getLivingEntity().setAI(true);
                                eliteEntity.getLivingEntity().setInvulnerable(false);

                                if (vehicle != null && !vehicle.isDead())
                                    vehicle.teleport(finalLocation);
                                eliteEntity.getLivingEntity().teleport(finalLocation);

                                if (vehicle != null && !vehicle.isDead()) {
                                    if (vehicle instanceof LivingEntity) {
                                        ((LivingEntity) vehicle).setAI(true);
                                        EliteEntity vehicleBoss = EntityTracker.getEliteMobEntity(vehicle);
                                        if (vehicleBoss != null)
                                            Bukkit.getServer().getPluginManager().callEvent(new EliteMobExitCombatEvent(vehicleBoss, EliteMobExitCombatEvent.EliteMobExitCombatReason.SPIRIT_WALK));

                                    }

                                    vehicle.setInvulnerable(false);
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            PreventMountExploit.bypass = true;
                                            vehicle.addPassenger(eliteEntity.getLivingEntity());
                                        }
                                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                                }
                                cancel();

                                Bukkit.getServer().getPluginManager().callEvent(new EliteMobExitCombatEvent(eliteEntity, EliteMobExitCombatEvent.EliteMobExitCombatReason.SPIRIT_WALK));
                                if (eliteEntity.getLivingEntity() instanceof Mob)
                                    if (((Mob) eliteEntity.getLivingEntity()).getTarget() == null && eliteEntity instanceof RegionalBossEntity regionalBossEntity)
                                        CustomBossEntity.CustomBossEntityEvents.slowRegionalBoss(regionalBossEntity);

                            }

                            if (vehicle != null && !vehicle.isDead()) {
                                vehicle.teleport(eliteEntity.getLivingEntity().getLocation().clone().add(toDestination.clone()));
                            }
                            eliteEntity.getLivingEntity().teleport(eliteEntity.getLivingEntity().getLocation().clone().add(toDestination.clone()));

                            counter++;

                        }

                    }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
                }

        );

    }

    private void incrementHitCounter() {
        hitCounter++;
    }

    private void resetHitsCounter() {
        hitCounter = 0;
    }

    private int getHitsCounter() {
        return hitCounter;
    }

    @EventHandler
    public void onBossMobGotHit(EliteMobDamagedEvent event) {
        if (event.isCancelled()) return;
        SpiritWalk spiritWalk = (SpiritWalk) event.getEliteEntity().getPower(this);
        if (spiritWalk == null) return;

        switch (event.getEntityDamageEvent().getCause()) {
            case FIRE:
            case FIRE_TICK:
            case POISON:
            case WITHER:
            case THORNS:
            case ENTITY_SWEEP_ATTACK:
                return;
        }

        if (event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.DROWNING) ||
                event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION))
            initializeSpiritWalk(event.getEliteEntity());

        spiritWalk.incrementHitCounter();


        if (spiritWalk.getHitsCounter() < 9) return;

        spiritWalk.resetHitsCounter();
        initializeSpiritWalk(event.getEliteEntity());

    }

    public void initializeSpiritWalk(EliteEntity eliteEntity) {
        new BukkitRunnable() {

            int counter = 1;

            @Override
            public void run() {

                if (counter > 3 || !eliteEntity.isValid()) {
                    cancel();
                    return;
                }

                Location bossLocation = eliteEntity.getLocation().clone();

                for (int i = 0; i < 20; i++) {

                    double randomizedX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 5;
                    double randomizedY = ThreadLocalRandom.current().nextDouble() * 5;
                    double randomizedZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 5;

                    Vector normalizedVector = new Vector(randomizedX, randomizedY, randomizedZ).normalize().multiply(7).multiply(counter);

                    Location newSimulatedLocation = bossLocation.add(normalizedVector).clone();

                    Location newValidLocation = scanVertically(newSimulatedLocation);

                    if (newValidLocation != null) {
                        spiritWalkAnimation(eliteEntity, eliteEntity.getLivingEntity().getLocation(), newValidLocation.add(new Vector(0.5, 1, 0.5)));
                        cancel();
                        break;
                    }

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private Location scanVertically(Location simulatedLocation) {
        int counter = 0;

        while (true) {
            Location newLocation = simulatedLocation.clone().subtract(new Vector(0, counter, 0));
            if (locationValid(newLocation)) return newLocation;
            counter++;
            if (counter > 8) return null;
        }
    }

    private boolean locationValid(Location feetLocation) {
        Location groundLocation = feetLocation.clone().add(new Vector(0, -1, 0));
        Location headLocation = feetLocation.clone().add(new Vector(0, 1, 0));
        return isNonVoidAir(feetLocation.getBlock().getType()) && isNonVoidAir(headLocation.getBlock().getType()) && groundLocation.getBlock().getType().isSolid();
    }

    private boolean isNonVoidAir(Material material) {
        return material.equals(Material.AIR) || material.equals(Material.CAVE_AIR);
    }

}
