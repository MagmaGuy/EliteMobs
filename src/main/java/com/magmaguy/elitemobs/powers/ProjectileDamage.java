package com.magmaguy.elitemobs.powers;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.internal.FakeItem;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles visual projectile damage for boss powers like GoldShotgun and GoldExplosion.
 * Uses packet-based FakeItem entities that cannot be picked up by hoppers or players.
 */
public class ProjectileDamage {

    /**
     * Handles damage dealing for fake gold nugget projectiles.
     */
    public static void doGoldNuggetDamage(List<FakeProjectile> projectiles, EliteEntity eliteEntity) {
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                timer++;

                if (projectiles.isEmpty()) {
                    cancel();
                    return;
                }

                for (Iterator<FakeProjectile> iterator = projectiles.iterator(); iterator.hasNext(); ) {
                    FakeProjectile projectile = iterator.next();
                    if (projectile == null || projectile.isRemoved()) {
                        iterator.remove();
                        continue;
                    }

                    // Update physics
                    projectile.tick();

                    // Check for collisions
                    boolean removed = false;
                    for (LivingEntity livingEntity : projectile.getNearbyLivingEntities(0.5)) {
                        // Skip the boss itself
                        if (livingEntity == eliteEntity.getLivingEntity()) continue;
                        // Skip blocking players
                        if (livingEntity instanceof Player player && player.isBlocking()) continue;

                        BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), livingEntity, 1);
                        removed = true;
                    }

                    if (removed) {
                        projectile.remove();
                        iterator.remove();
                        continue;
                    }

                    // Remove if hit ground
                    if (projectile.isOnGround()) {
                        projectile.remove();
                        iterator.remove();
                    }
                }

                // Timeout after 5 seconds
                if (timer >= 5 * 20) {
                    for (FakeProjectile projectile : projectiles) {
                        projectile.remove();
                    }
                    projectiles.clear();
                    cancel();
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    /**
     * Creates a gold nugget projectile at the specified location with the given velocity.
     */
    public static FakeProjectile createGoldNuggetProjectile(Location location, Vector velocity) {
        ItemStack goldNugget = ItemStackGenerator.generateItemStack(
                Material.GOLD_NUGGET,
                "visual projectile",
                List.of(ThreadLocalRandom.current().nextDouble() + ""));

        FakeProjectile projectile = new FakeProjectile(location, goldNugget, velocity);
        projectile.displayToNearbyPlayers(64);
        return projectile;
    }

    /**
     * Represents a fake visual projectile with physics simulation.
     */
    public static class FakeProjectile {
        private final FakeItem fakeItem;
        private Location location;
        private Vector velocity;
        private boolean removed = false;
        private boolean hasGravity = false;

        public FakeProjectile(Location spawnLocation, ItemStack itemStack, Vector velocity) {
            this.location = spawnLocation.clone();
            this.velocity = velocity.clone();

            this.fakeItem = NMSManager.getAdapter().fakeItemBuilder()
                    .itemStack(itemStack)
                    .scale(0.5f)
                    .build(spawnLocation);
        }

        public void displayToNearbyPlayers(double radius) {
            for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                if (entity instanceof Player player) {
                    fakeItem.displayTo(player);
                }
            }
        }

        public void setGravity(boolean gravity) {
            this.hasGravity = gravity;
        }

        public void tick() {
            if (removed) return;

            // Apply gravity if enabled
            if (hasGravity) {
                velocity.setY(velocity.getY() - 0.04);
            }

            // Update position
            location.add(velocity);

            // Teleport the visual
            fakeItem.teleport(location);
        }

        public Location getLocation() {
            return location.clone();
        }

        public boolean isOnGround() {
            // Check if the block below is solid
            Location below = location.clone().subtract(0, 0.1, 0);
            return below.getBlock().getType().isSolid();
        }

        public void remove() {
            if (!removed) {
                removed = true;
                fakeItem.remove();
            }
        }

        public boolean isRemoved() {
            return removed;
        }

        /**
         * Gets nearby living entities for collision detection.
         * Uses manual distance calculation since FakeItem doesn't exist in the world.
         */
        public List<LivingEntity> getNearbyLivingEntities(double radius) {
            List<LivingEntity> nearby = new ArrayList<>();
            for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                if (entity instanceof LivingEntity livingEntity) {
                    nearby.add(livingEntity);
                }
            }
            return nearby;
        }
    }
}
