package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlasmaBootsEnchantment extends CustomEnchantment {
    public static String key = "plasma_boots";

    public PlasmaBootsEnchantment() {
        super(key, false);
    }

    public static void doPlasmaBootsEnchantment(int level, Player player) {
        player.setVelocity(new Vector(0, .8, 0));
        Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, (task) -> {
            if (!player.isValid() || !player.getLocation().clone().subtract(new Vector(0, 1, 0)).getBlock().isPassable()
                    && player.getLocation().getY() - player.getLocation().getBlock().getY() < 0.1 || !player.getLocation().clone().getBlock().isPassable()) {
                task.cancel();
                doLanding(level, player);
                return;
            }
            player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                    20, 0.1, 0.1, 0.1, 1, new Particle.DustOptions(Color.fromRGB(
                            ThreadLocalRandom.current().nextInt(0, 100),
                            ThreadLocalRandom.current().nextInt(122, 255),
                            ThreadLocalRandom.current().nextInt(0, 100)
                    ), 1));
        }, 5, 1);
    }

    private static void doLanding(int level, Player player) {
        for (int i = 1; i < level + 1; i++)
            doProjectileArray(i, player);
    }

    private static void doProjectileArray(int level, Player player) {
        double height = (level + .2 - 1) / 2;
        Vector vector = new Vector(1, height, 0);
        for (int i = 0; i < 8; i++) {
            Vector offsetVector = vector.rotateAroundY(i * Math.PI / 4);
            Location shotLocation = player.getLocation().clone().add(offsetVector);
            Vector shotVector = shotLocation.clone().subtract(player.getLocation()).toVector().normalize().multiply(0.2);
            createProjectile(shotVector, shotLocation, player);
        }
    }

    private static void createProjectile(Vector shotVector, Location sourceLocation, Player player) {
        new BukkitRunnable() {
            int counter = 0;
            Location currentLocation = sourceLocation.clone();

            @Override
            public void run() {
                if (counter > 20 * 3) {
                    cancel();
                    return;
                }
                counter++;
                for (Entity entity : currentLocation.getWorld().getNearbyEntities(currentLocation, 0.1, 0.1, 0.1)) {
                    if (!(entity instanceof LivingEntity)) continue;
                    if (entity.getType().equals(EntityType.PLAYER)) continue;
                    cancel();
                    doDamage(player, (LivingEntity) entity);
                    break;
                }

                doVisualEffect(currentLocation);

                currentLocation.add(shotVector);
                if (!currentLocation.getBlock().isPassable()) {
                    cancel();
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static void doDamage(Player player, LivingEntity livingEntity) {
        livingEntity.damage(2, player);
    }

    private static void doVisualEffect(Location currentLocation) {
        currentLocation.getWorld().spawnParticle(Particle.REDSTONE, currentLocation.getX(), currentLocation.getY(), currentLocation.getZ(),
                5, 0.1, 0.1, 0.1, 1, new Particle.DustOptions(Color.fromRGB(
                        ThreadLocalRandom.current().nextInt(0, 100),
                        ThreadLocalRandom.current().nextInt(122, 255),
                        ThreadLocalRandom.current().nextInt(0, 100)),
                        1));
    }

    public static class PlasmaBootsEnchantmentEvents implements Listener {
        private static HashSet<UUID> players = new HashSet<>();
        private static HashSet<UUID> cooldownPlayers = new HashSet<>();

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onSneak(PlayerToggleSneakEvent event) {
            if (!event.isSneaking()) return;
            if (event.getPlayer().isFlying()) return;
            if (cooldownPlayers.contains(event.getPlayer().getUniqueId())) return;
            //todo: don't check this event if the power is disabled
            if (ElitePlayerInventory.playerInventories.get(event.getPlayer().getUniqueId()) == null) return;
            double plasmaBootLevel = ElitePlayerInventory.playerInventories.get(event.getPlayer().getUniqueId()).getPlasmaBootsLevel(true);
            if (plasmaBootLevel < 1) return;
            if (!players.contains(event.getPlayer().getUniqueId())) {
                players.add(event.getPlayer().getUniqueId());
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, (task) -> players.remove(event.getPlayer().getUniqueId()), 10);
                return;
            }
            players.remove(event.getPlayer().getUniqueId());
            cooldownPlayers.add(event.getPlayer().getUniqueId());
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, (task) -> cooldownPlayers.remove(event.getPlayer().getUniqueId()), 20L * 60 * 2);

            doPlasmaBootsEnchantment((int) plasmaBootLevel, event.getPlayer());

        }
    }

}
