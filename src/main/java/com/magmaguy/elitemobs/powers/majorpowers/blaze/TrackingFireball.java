package com.magmaguy.elitemobs.powers.majorpowers.blaze;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MajorPower;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class TrackingFireball extends MajorPower {

    public TrackingFireball() {
        super(PowersConfig.getPower("tracking_fireball.yml"));
    }

    public static class TrackingFireballEvents implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void targetEvent(EliteMobTargetPlayerEvent event) {
            if (!(event.getEliteMobEntity().getLivingEntity() instanceof Monster)) return;
            TrackingFireball trackingFireball = (TrackingFireball) event.getEliteMobEntity().getPower(new TrackingFireball());
            if (trackingFireball == null) return;
            if (trackingFireball.getIsFiring()) return;

            trackingFireball.setIsFiring(true);
            new TrackingFireballTasks((Monster) event.getEntity(), trackingFireball);
        }

        @EventHandler(ignoreCancelled = true)
        public void onFireballPunched(EntityDamageByEntityEvent event) {
            TrackingFireballTasks.TrackingFireballTask trackingFireballTask = trackingFireballs.get(event.getEntity().getUniqueId());
            if (trackingFireballTask == null) return;
            trackingFireballTask.isAfterPlayer = !trackingFireballTask.isAfterPlayer;
        }


        public static HashMap<UUID, TrackingFireballTasks.TrackingFireballTask> trackingFireballs = new HashMap<>();

        private class TrackingFireballTasks {

            public TrackingFireballTasks(Monster monster, TrackingFireball trackingFireball) {

                new BukkitRunnable() {

                    @Override
                    public void run() {

                        if (!monster.isValid() || monster.getTarget() == null) {
                            trackingFireball.setIsFiring(false);
                            cancel();
                            return;
                        }

                        for (Entity nearbyEntity : monster.getNearbyEntities(20, 20, 20))
                            if (nearbyEntity instanceof Player)
                                if (((Player) nearbyEntity).getGameMode().equals(GameMode.ADVENTURE) ||
                                        ((Player) nearbyEntity).getGameMode().equals(GameMode.SURVIVAL))
                                    new TrackingFireballTask(monster, (Player) nearbyEntity);

                    }

                }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 8);

            }

            public class TrackingFireballTask {
                public boolean isAfterPlayer = true;
                private Fireball repeatingFireball = null;

                public TrackingFireballTask(Entity entity, Player player) {
                    Vector targetterToTargetted = player.getLocation().clone().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(0.0001);
                    repeatingFireball = (Fireball) EliteProjectile.create(EntityType.FIREBALL, entity, player, targetterToTargetted, true);
                    repeatingFireball.setYield(3F);
                    repeatingFireball.setIsIncendiary(true);
                    repeatingFireball.setShooter((ProjectileSource) entity);
                    trackingFireballs.put(repeatingFireball.getUniqueId(), this);

                    new BukkitRunnable() {
                        int counter = 0;

                        @Override
                        public void run() {
                            if (repeatingFireball == null ||
                                    !repeatingFireball.isValid() ||
                                    !entity.isValid() ||
                                    player.isDead() ||
                                    counter > 20 * 60 * 3) {
                                cancel();
                                return;
                            }

                            if (isAfterPlayer) {
                                repeatingFireball.setDirection(setFireballDirection(player));
                                repeatingFireball.setVelocity(setFireballDirection(player));
                            } else {
                                repeatingFireball.setDirection(setFireballDirection(entity));
                                repeatingFireball.setVelocity(setFireballDirection(entity));
                            }
                            counter++;
                        }
                    }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
                }


                private Vector setFireballDirection(Entity target) {
                    double fireballVelocity = PowersConfig.getPower("tracking_fireball.yml").getConfiguration().getDouble("fireballSpeed");
                    if (target.getLocation().distanceSquared(repeatingFireball.getLocation()) > 1)
                        return target.getLocation().clone()
                                .add(new Vector(0, 1, 0))
                                .toVector()
                                .subtract(repeatingFireball.getLocation().toVector())
                                .normalize()
                                .multiply(fireballVelocity);
                    //when close aim for the ground due to a bug where entity speed syncs up with fireball speed
                    return target.getLocation().clone()
                            .toVector()
                            .subtract(repeatingFireball.getLocation().toVector())
                            .normalize()
                            .multiply(fireballVelocity);
                }
            }
        }
    }

}
