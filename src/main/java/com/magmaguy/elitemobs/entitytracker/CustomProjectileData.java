package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class CustomProjectileData implements Listener {
    @Getter
    private static final HashMap<Projectile, CustomProjectileData> customProjectileDataHashMap = new HashMap<>();
    private final Player player;
    @Getter
    private ItemStack projectileShooter = null;
    @Getter
    private Material projectileShooterMaterial = null;

    private CustomProjectileData(Player player, Projectile projectile) {
        this.player = player;
        if (projectile instanceof Trident trident) {
            projectileShooter = trident.getItem();
            projectileShooterMaterial = Material.TRIDENT;
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() == Material.BOW || player.getInventory().getItemInMainHand().getType() == Material.CROSSBOW) {
            projectileShooter = player.getInventory().getItemInMainHand();
            projectileShooterMaterial = projectileShooter.getType();
        } else if (player.getInventory().getItemInOffHand().getType() == Material.BOW || player.getInventory().getItemInOffHand().getType() == Material.CROSSBOW) {
            projectileShooter = player.getInventory().getItemInOffHand();
            projectileShooterMaterial = projectileShooter.getType();
        }
    }

    public static void shutdown() {
        customProjectileDataHashMap.clear();
    }

    @EventHandler
    public void onPlayerFireProjectile(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() == null || !(event.getEntity().getShooter() instanceof Player player))
            return;
        customProjectileDataHashMap.put(event.getEntity(), new CustomProjectileData(player, event.getEntity()));
        //Cull entity 5 minutes later to avoid leaking
        new BukkitRunnable() {
            @Override
            public void run() {
                customProjectileDataHashMap.remove(event.getEntity());
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 60 * 20 * 5);
    }

    @EventHandler
    public void onArrowLandEvent(ProjectileHitEvent event) {
        if (customProjectileDataHashMap.containsKey(event.getEntity()))
            //Delay removal by 1 just in case the data needs to be used for some event that runs after the hit event
            new BukkitRunnable() {
                @Override
                public void run() {
                    customProjectileDataHashMap.remove(event.getEntity());
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }
}
