package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class MeteorShowerEnchantment extends CustomEnchantment {

    public static String key = "meteor_shower";

    public MeteorShowerEnchantment() {
        super(key, false);
    }

    public static void doMeteorShower(Player player) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                if (!player.isValid()) {
                    cancel();
                    return;
                }

                if (counter > 10 * 20) {
                    cancel();
                    return;
                }

                counter++;

                doCloudEffect(player.getLocation().clone().add(new Vector(0, 10, 0)));

                if (counter > 2 * 20) {

                    doFireballs(player.getLocation().clone().add(new Vector(0, 10, 0)), player);

                }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static void doCloudEffect(Location location) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            location.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, newLocation, 1, 0, 0, 0, 0);
        }
    }

    private static void doFireballs(Location location, Player player) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            newLocation = newLocation.setDirection(new Vector(ThreadLocalRandom.current().nextDouble() - 0.5, -1, ThreadLocalRandom.current().nextDouble() - 0.5));
            Fireball fireball = (Fireball) location.getWorld().spawnEntity(newLocation, EntityType.FIREBALL);
            fireball.setShooter(player);
        }
    }

    public static class MeteorShowerEvents implements Listener {
        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            int meteorShower = ItemTagger.getEnchantment(event.getPlayer().getInventory().getItemInMainHand().getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, MeteorShowerEnchantment.key));
            if (meteorShower < 1) return;
            event.getItem().setAmount(event.getItem().getAmount() - 1);
            MeteorShowerEnchantment.doMeteorShower(event.getPlayer());
        }
    }

}
