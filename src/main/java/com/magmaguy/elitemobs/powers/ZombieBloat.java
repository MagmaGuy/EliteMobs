package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.meta.MajorPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
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
        if (zombieBloat.isInGlobalCooldown()) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.20) return;
        zombieBloat.doGlobalCooldown(20 * 10);

        /*
        Create early warning that entity is about to bloat
         */
        new BukkitRunnable() {

            final LivingEntity eventZombie = (LivingEntity) event.getEntity();
            int timer = 0;

            @Override
            public void run() {

                if (timer > 40) {
                    bloatEffect(eventZombie);
                    cancel();
                }

                if (timer == 21)
                    eventZombie.setAI(false);


                if (MobCombatSettingsConfig.isEnableWarningVisualEffects())
                    eventZombie.getWorld().spawnParticle(Particle.TOTEM, new Location(eventZombie.getWorld(),
                                    eventZombie.getLocation().getX(), eventZombie.getLocation().getY() +
                                    eventZombie.getHeight(), eventZombie.getLocation().getZ()), 20, timer / 24,
                            timer / 9d, timer / 24d, 0.1);

                timer++;
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void bloatEffect(LivingEntity eventZombie) {
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
                livingEntity.setVelocity(new Vector(0, 1.5, 0));
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
        if (!MobCombatSettingsConfig.isEnableWarningVisualEffects())
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
