package com.magmaguy.elitemobs.combattag;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
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
                    TextComponent.fromLegacyText(ChatColorConverter.chatColorConverter(ConfigValues.mobCombatSettingsConfig.getString(MobCombatSettingsConfig.COMBAT_TAG_TRIGGER_MESSAGE))));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || player.isDead())
                        cancel();
                    if (player.isOnGround()) {
                        player.setFallDistance(0F);
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

}
