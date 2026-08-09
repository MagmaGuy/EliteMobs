package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Predicate;

public class CombatTag implements Listener {

    private static Player playerFinder(EntityDamageByEntityEvent event) {
        return playerFinder(event.getEntity(), event.getDamager(), EntityTracker::isEliteMob);
    }

    static Player playerFinder(Entity damaged, Entity damager, Predicate<Entity> isEliteMob) {
        if (damager instanceof Player player && isEliteMob.test(damaged))
            return player;
        if (damaged instanceof Player player &&
                (isEliteMob.test(damager) ||
                        damager instanceof Projectile projectile &&
                                projectile.getShooter() instanceof LivingEntity shooter &&
                                isEliteMob.test(shooter)))
            return player;
        if (damager instanceof Projectile projectile &&
                projectile.getShooter() instanceof Player player &&
                isEliteMob.test(damaged))
            return player;

        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {

        Player player = playerFinder(event);

        if (player == null) return;

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        //if (player.isInvulnerable()) player.setInvulnerable(false);
        if (player.isFlying()) {
            player.setFlying(false);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(CombatTagConfig.getCombatTagMessage()));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 60, 0));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || player.isDead()) {
                        clearFlightSafetyEffect(player);
                        cancel();
                        return;
                    }
                    if (player.isOnGround()) {
                        cancel();
                        clearFlightSafetyEffect(player);
                    }
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        }
    }

    static void clearFlightSafetyEffect(Player player) {
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
    }

}
