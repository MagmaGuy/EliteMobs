package com.magmaguy.elitemobs.powers.majorpowers.zombie;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MajorPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ZombieBloat extends MajorPower implements Listener {

    public ZombieBloat() {
        super(PowersConfig.getPower("zombie_bloat.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        ZombieBloat zombieBloat = (ZombieBloat) event.getEliteMobEntity().getPower(this);
        if (zombieBloat == null) return;
        if (zombieBloat.isCooldown()) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.20) return;
        zombieBloat.doCooldown(20 * 10);

        /*
        Create early warning that entity is about to bloat
         */
        new BukkitRunnable() {

            int timer = 0;
            Zombie eventZombie = (Zombie) event.getEntity();

            @Override
            public void run() {

                if (timer > 40) {
                    bloatEffect(eventZombie);
                    cancel();
                }

                if (timer == 21)
                    eventZombie.setAI(false);


                if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_WARNING_VISUAL_EFFECTS))
                    eventZombie.getWorld().spawnParticle(Particle.TOTEM, new Location(eventZombie.getWorld(),
                                    eventZombie.getLocation().getX(), eventZombie.getLocation().getY() +
                                    eventZombie.getHeight(), eventZombie.getLocation().getZ()), 20, timer / 24,
                            timer / 9, timer / 24, 0.1);

                timer++;
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void bloatEffect(Zombie eventZombie) {
        /*
        Spawn giant for "bloat" effect
         */
        Giant giant = (Giant) eventZombie.getWorld().spawnEntity(eventZombie.getLocation(), EntityType.GIANT);
        giant.setAI(false);

        /*
        Apply knockback to all living entities around the giant except for the original zombie entity
         */
        List<Entity> nearbyEntities = giant.getNearbyEntities(4, 15, 4);
        List<LivingEntity> nearbyValidLivingEntities = new ArrayList<>();

        if (nearbyEntities.size() > 0)
            for (Entity entity : nearbyEntities)
                if (entity instanceof LivingEntity && !entity.equals(eventZombie))
                    nearbyValidLivingEntities.add((LivingEntity) entity);

        Location entityLocation = eventZombie.getLocation();

        for (LivingEntity livingEntity : nearbyValidLivingEntities) {

            Location livingEntityLocation = livingEntity.getLocation();
            Vector toLivingEntityVector = livingEntityLocation.subtract(entityLocation).toVector();

            /*
            Normalize vector to apply powers uniformly
             */
            Vector normalizedVector = toLivingEntityVector.normalize();
            normalizedVector = normalizedVector.multiply(new Vector(2, 0, 2)).add(new Vector(0, 1, 0));

            try {
                livingEntity.setVelocity(normalizedVector);
            } catch (Exception e) {
            }

        }

        livingEntityEffect(nearbyValidLivingEntities);

        /*
        Effect is done, start task to remove giant
         */
        new BukkitRunnable() {

            @Override
            public void run() {
                giant.remove();
                eventZombie.setAI(true);
            }

        }.runTaskLater(MetadataHandler.PLUGIN, 10);

    }

    private void livingEntityEffect(List<LivingEntity> livingEntities) {
        if (livingEntities.size() == 0) return;
        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_WARNING_VISUAL_EFFECTS))
            return;

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 1.5 * 20)
                    cancel();
                for (LivingEntity livingEntity : livingEntities)
                    if (!(livingEntity == null || livingEntity.isDead() || !livingEntity.isValid()))
                        livingEntity.getWorld().spawnParticle(Particle.CLOUD, new Location(livingEntity.getWorld(),
                                        livingEntity.getLocation().getX(),
                                        livingEntity.getLocation().getY() + livingEntity.getHeight() - 1,
                                        livingEntity.getLocation().getZ()),
                                0, 0, 0, 0);
                counter++;
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

}
