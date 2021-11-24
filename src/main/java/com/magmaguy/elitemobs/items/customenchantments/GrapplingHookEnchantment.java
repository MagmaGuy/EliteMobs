package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GrapplingHookEnchantment extends CustomEnchantment {
    public static String key = "grappling_hook";

    public GrapplingHookEnchantment() {
        super(key, false);
    }

    public static boolean isGrapplingHookProjectile(ItemMeta itemMeta) {
        return ItemTagger.getEnchantment(itemMeta, key) > 0;
    }

    public static void trackGrapplingHook(AbstractArrow arrow, Player player) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 20 * 10 || arrow.isInBlock()) {
                    if (arrow.getAttachedBlock() != null) {
                        Location targetBlock = getTargetBlock(arrow.getLocation());
                        if (targetBlock != null) {
                            zipline(player, arrow.getAttachedBlock().getRelative(arrow.getFacing()).getLocation().add(new Vector(0.5, 0.5, 0.5)));
                            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        }
                    }
                    cancel();
                    return;
                }
                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
    }

    private static void zipline(Player player, Location location) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20 * 10, 1));
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                if (timer > 20 * 10 || player.getLocation().distance(location) < 1) {
                    player.setGravity(true);
                    player.removePotionEffect(PotionEffectType.LEVITATION);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 3, 1));
                    cancel();
                    return;
                }
                player.setVelocity(location.clone().subtract(player.getLocation()).toVector().normalize().multiply(.5));
                timer++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
    }

    private static Location getTargetBlock(Location airLocation) {
        if (checkBlock(airLocation)) return airLocation;
        Location up = airLocation.clone().add(new Vector(0, 1, 0));
        if (checkBlock(up)) return up;
        Location down = airLocation.clone().add(new Vector(0, -1, 0));
        if (checkBlock(down)) return down;
        Location front = airLocation.clone().add(new Vector(1, 0, 0));
        if (checkBlock(front)) return front;
        Location back = airLocation.clone().add(new Vector(-1, 0, 0));
        if (checkBlock(back)) return back;
        Location left = airLocation.clone().add(new Vector(0, 0, 1));
        if (checkBlock(left)) return left;
        Location right = airLocation.clone().add(new Vector(0, 0, -1));
        if (checkBlock(right)) return right;
        return null;
    }

    private static boolean checkBlock(Location location) {
        return location.getBlock().getType().equals(Material.TARGET);
    }

    public static class GrapplingHookEnchantmentEvents implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onArrowFire(EntityShootBowEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            if (!event.getProjectile().getType().equals(EntityType.ARROW) &&
                    !event.getProjectile().getType().equals(EntityType.SPECTRAL_ARROW)) return;
            if (event.getConsumable() == null || !isGrapplingHookProjectile(event.getConsumable().getItemMeta()))
                return;
            ((AbstractArrow) event.getProjectile()).setBounce(false);
            event.getEntity().setLeashHolder(event.getEntity());
            trackGrapplingHook((AbstractArrow) event.getProjectile(), (Player) event.getEntity());
        }
    }

}
