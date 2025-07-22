package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class AttackFireball extends MinorPower implements Listener {

    public AttackFireball() {
        super(PowersConfig.getPower("attack_fireball.yml"));
    }

    public static Fireball shootFireball(Entity entity, Player player) {
        // Get locations with null checks
        Location entityLoc = entity.getLocation();
        Location playerLoc = player.getLocation();

        if (entityLoc == null || playerLoc == null) {
            return null;
        }

        // Calculate direction vector
        Vector direction = playerLoc.toVector().subtract(entityLoc.toVector());

        // Check if the vector is zero-length (entity and player at same position)
        if (direction.lengthSquared() < 0.0001) {
            // Use a default direction (upward slightly)
            direction = new Vector(0, 1, 0);
        } else {
            direction.normalize();
        }

        // Multiply for velocity
        direction.multiply(0.01);

        // Create the fireball
        Fireball repeatingFireball = (Fireball) EliteProjectile.create(EntityType.FIREBALL, entity, player, direction, true);

        if (repeatingFireball != null) {
            repeatingFireball.setYield(3F);
            repeatingFireball.setIsIncendiary(true);
            repeatingFireball.setShooter((ProjectileSource) entity);
        }

        return repeatingFireball;
    }

    @EventHandler
    public void targetEvent(EliteMobTargetPlayerEvent event) {
        if (!(event.getEliteMobEntity().getLivingEntity() instanceof Monster)) return;
        AttackFireball attackFireball = (AttackFireball) event.getEliteMobEntity().getPower(this);
        if (attackFireball == null) return;
        if (attackFireball.isFiring()) return;

        attackFireball.setFiring(true);
        repeatingFireballTask((Monster) event.getEntity(), attackFireball);
    }

    private void repeatingFireballTask(Monster monster, AttackFireball attackFireball) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!monster.isValid() || monster.getTarget() == null) {
                    attackFireball.setFiring(false);
                    cancel();
                    return;
                }

                for (Entity nearbyEntity : monster.getNearbyEntities(20, 20, 20)) {
                    if (nearbyEntity instanceof Player) {
                        Player targetPlayer = (Player) nearbyEntity;
                        if (targetPlayer.getGameMode().equals(GameMode.ADVENTURE) ||
                                targetPlayer.getGameMode().equals(GameMode.SURVIVAL)) {

                            // Shoot fireball with null check
                            Fireball fireball = shootFireball(monster, targetPlayer);
                            if (fireball == null) {
                                // Log or handle the case where fireball creation failed
                                MetadataHandler.PLUGIN.getLogger().warning("Failed to create fireball for entity at " + monster.getLocation());
                            }
                        }
                    }
                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20L * 8);

    }

}