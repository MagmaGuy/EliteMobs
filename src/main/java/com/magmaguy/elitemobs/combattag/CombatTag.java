package com.magmaguy.elitemobs.combattag;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatTag implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        Player player = playerFinder(event);

        if (player == null) return;

        if (player.isInvulnerable()) player.setInvulnerable(false);
        if (player.isFlying()) {
            player.setFlying(false);
            player.setMetadata(MetadataHandler.SAFE_FALL, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || player.isDead())
                        cancel();
                    if (player.isOnGround()) {
                        event.getEntity().removeMetadata(MetadataHandler.SAFE_FALL, MetadataHandler.PLUGIN);
                        cancel();
                    }
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        }
    }

    private static Player playerFinder(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player) return (Player) event.getDamager();
        if (event.getEntity() instanceof Player) return (Player) event.getEntity();
        if (event.getDamager() instanceof Projectile && event.getDamager() instanceof Projectile
                && ((Projectile) event.getDamager()).getShooter() instanceof Player)
            return (Player) ((Projectile) event.getDamager()).getShooter();

        return null;

    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player && event.getEntity().hasMetadata(MetadataHandler.SAFE_FALL))) return;

        event.setCancelled(true);
        event.getEntity().removeMetadata(MetadataHandler.SAFE_FALL, MetadataHandler.PLUGIN);

    }

}
