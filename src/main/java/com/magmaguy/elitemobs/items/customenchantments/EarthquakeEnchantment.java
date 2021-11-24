package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.premade.EarthquakeConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EarthquakeEnchantment extends CustomEnchantment {
    public static String key = "earthquake";

    public EarthquakeEnchantment() {
        super(key, false);
    }

    public static void doEarthquakeEnchantment(int earthquakeLevel, Player player) {
        player.sendMessage(EarthquakeConfig.getEarthquakeActivationMessage());
        player.setVelocity(player.getLocation().getDirection().normalize().multiply((Math.log(earthquakeLevel+2 / 2D) + 1) / 20D).setY(Math.log(earthquakeLevel+2 / 2D)));
        Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, (task) -> {
            player.setFallDistance(0f);
            if (!player.isValid() || !player.getLocation().clone().subtract(new Vector(0, 1, 0)).getBlock().isPassable()
                    && player.getLocation().getY() - player.getLocation().getBlock().getY() < 0.1 || !player.getLocation().clone().getBlock().isPassable()) {
                task.cancel();
                doLanding(earthquakeLevel, player);
                return;
            }
            player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                    20, 0.1, 0.1, 0.1, 1, new Particle.DustOptions(Color.fromRGB(
                            ThreadLocalRandom.current().nextInt(80, 100),
                            ThreadLocalRandom.current().nextInt(20, 40),
                            ThreadLocalRandom.current().nextInt(10, 20)
                    ), 1));
        }, 5, 1);
    }

    public static void doLanding(int level, Player player) {
        double distance = Math.log(level+2 / 2D) * 3;
        for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
            if (entity instanceof LivingEntity) {
                if (entity.getType().equals(EntityType.PLAYER)) continue;
                EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
                if (eliteEntity == null) {
                    entity.setVelocity(entity.getLocation().subtract(player.getLocation()).toVector().normalize().setY(.3).multiply(distance / 2d));
                } else {
                    entity.setVelocity(entity.getLocation().subtract(player.getLocation()).toVector().normalize().setY(.3)
                            .multiply((distance / 2d) / (eliteEntity.getLevel() * eliteEntity.getHealthMultiplier())));
                }
            }
        }
    }

    public static class EarthquakeEnchantmentEvents implements Listener {
        private static HashSet<UUID> players = new HashSet<>();
        private static HashSet<UUID> cooldownPlayers = new HashSet<>();

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onSneak(PlayerToggleSneakEvent event) {
            if (!event.isSneaking()) return;
            if (event.getPlayer().isFlying()) return;
            if (cooldownPlayers.contains(event.getPlayer().getUniqueId())) return;
            //todo: don't check this event if the power is disabled
            if (ElitePlayerInventory.playerInventories.get(event.getPlayer().getUniqueId()) == null) return;
            double earthquakeLevel = ElitePlayerInventory.playerInventories.get(event.getPlayer().getUniqueId()).getEarthquakeLevel(true);
            if (earthquakeLevel < 1) return;
            if (!players.contains(event.getPlayer().getUniqueId())) {
                players.add(event.getPlayer().getUniqueId());
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, (task) -> players.remove(event.getPlayer().getUniqueId()), 10);
                return;
            }
            players.remove(event.getPlayer().getUniqueId());
            cooldownPlayers.add(event.getPlayer().getUniqueId());
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, (task) -> {
                event.getPlayer().sendMessage(EarthquakeConfig.getEarthquakeAvailableMessage());
                cooldownPlayers.remove(event.getPlayer().getUniqueId());
            }, 20L * 60 * 2);

            doEarthquakeEnchantment((int) earthquakeLevel, event.getPlayer());

        }
    }
}
