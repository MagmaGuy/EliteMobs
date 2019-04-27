package com.magmaguy.elitemobs.combattag;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.ConfigValues;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatTag implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        Player player = playerFinder(event);

        if (player == null) return;

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        if (player.isInvulnerable()) player.setInvulnerable(false);
        if (player.isFlying()) {
            player.setFlying(false);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(
                            ChatColorConverter.convert(
                                    ConfigValues.combatTagConfig.getString(CombatTagConfig.COMBAT_TAG_MESSAGE))));
            new BukkitRunnable() {
                @Override
                public void run() {
                    //TODO: introduce the featherfall potion effect for versions above 1.12.2
//                    if (NameHandler.currentVersionIsUnder(13, 0)) {
                    if (!player.isOnline() || player.isDead())
                        cancel();
                    if (player.isOnGround()) {
                        player.setFallDistance(0F);
                        cancel();
                    }
//                    } else {
////TODO: introduce the featherfall potion effect for versions above 1.12.2
//                        if (!player.isOnline() || player.isDead())
//                            player.removePotionEffect(PotionEffectType.SLOW);
//                            cancel();
//                        if (player.isOnGround()) {
//                            player.setFallDistance(0F);
//                            cancel();
//                        }
//
//                    }
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        }
    }

    private static Player playerFinder(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player && EntityTracker.isEliteMob(event.getEntity()))
            return (Player) event.getDamager();
        if (event.getEntity() instanceof Player && (EntityTracker.isEliteMob(event.getEntity()) ||
                event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity &&
                        EntityTracker.isEliteMob(((LivingEntity) ((Projectile) event.getDamager()).getShooter()))))
            return (Player) event.getEntity();
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player &&
                EntityTracker.isEliteMob(event.getEntity()))
            return (Player) ((Projectile) event.getDamager()).getShooter();

        return null;

    }

}
