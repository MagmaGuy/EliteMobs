package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrackingFireballSupport {

    private static final Map<UUID, TrackingFireballTask> trackingFireballs = new HashMap<>();

    private TrackingFireballSupport() {
    }

    /** The calling Lua runtime owns when this producer runs; launched shots retain their flight lifetime. */
    public static void fireAtNearbyPlayers(Monster monster, double fireballSpeed) {
        for (Entity nearbyEntity : monster.getNearbyEntities(20, 20, 20)) {
            if (nearbyEntity instanceof Player player
                    && (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL))
                new TrackingFireballTask(monster, player, fireballSpeed);
        }
    }

    public static void shutdown() {
        new ArrayList<>(trackingFireballs.values()).forEach(TrackingFireballTask::close);
    }

    private static final class TrackingFireballTask {
        private boolean isAfterPlayer = true;
        private final Fireball repeatingFireball;
        private BukkitTask task;

        private TrackingFireballTask(Entity entity, Player player, double fireballSpeed) {
            Vector targetterToTargetted = player.getLocation().clone().toVector()
                    .subtract(entity.getLocation().toVector())
                    .normalize()
                    .multiply(0.0001);
            repeatingFireball = (Fireball) EliteProjectile.create(EntityType.FIREBALL, entity, player, targetterToTargetted, true);
            repeatingFireball.setYield(3F);
            repeatingFireball.setIsIncendiary(true);
            repeatingFireball.setShooter((ProjectileSource) entity);
            trackingFireballs.put(repeatingFireball.getUniqueId(), this);

            task = new BukkitRunnable() {
                int counter = 0;

                @Override
                public void run() {
                    if (repeatingFireball == null
                            || !repeatingFireball.isValid()
                            || !entity.isValid()
                            || player.isDead()
                            || counter > 20 * 60 * 3
                            || repeatingFireball.getLocation().getWorld() != player.getWorld()) {
                        close();
                        return;
                    }

                    if (isAfterPlayer) {
                        repeatingFireball.setDirection(setFireballDirection(player, fireballSpeed));
                        repeatingFireball.setVelocity(setFireballDirection(player, fireballSpeed));
                    } else {
                        repeatingFireball.setDirection(setFireballDirection(entity, fireballSpeed));
                        repeatingFireball.setVelocity(setFireballDirection(entity, fireballSpeed));
                    }
                    counter++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
        }

        private void close() {
            if (task != null) task.cancel();
            task = null;
            trackingFireballs.remove(repeatingFireball.getUniqueId(), this);
        }

        private Vector setFireballDirection(Entity target, double fireballSpeed) {
            if (target.getLocation().distanceSquared(repeatingFireball.getLocation()) > 1) {
                return target.getLocation().clone()
                        .add(new Vector(0, 1, 0))
                        .toVector()
                        .subtract(repeatingFireball.getLocation().toVector())
                        .normalize()
                        .multiply(fireballSpeed);
            }
            return target.getLocation().clone()
                    .toVector()
                    .subtract(repeatingFireball.getLocation().toVector())
                    .normalize()
                    .multiply(fireballSpeed);
        }
    }

    public static class Events implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onFireballPunched(EntityDamageByEntityEvent event) {
            TrackingFireballTask trackingFireballTask = trackingFireballs.get(event.getEntity().getUniqueId());
            if (trackingFireballTask == null) return;
            trackingFireballTask.isAfterPlayer = !trackingFireballTask.isAfterPlayer;
        }
    }
}
