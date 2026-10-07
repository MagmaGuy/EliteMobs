package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Map;

public class FixPlayerOnLoginOrRespawn implements Listener {
    // Vanilla keys each effect's movement speed modifier by these ids. Without the effect, the modifier is stale.
    private static final Map<PotionEffectType, NamespacedKey> MOVEMENT_EFFECT_MODIFIERS = Map.of(
            PotionEffectType.SLOWNESS, NamespacedKey.minecraft("effect.slowness"),
            PotionEffectType.SPEED, NamespacedKey.minecraft("effect.speed"));

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // The respawn completes after this event, so inspect the player once it has.
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> clearStaleMovementEffects(player));
        resetScale(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        clearStaleMovementEffects(event.getPlayer());
        resetScale(event.getPlayer());
    }

    private static void resetScale(Player player) {
        if (DefaultConfig.isResetPlayerScaleOnLogin())
            AttributeManager.setAttribute(player, "generic_scale", 1);
    }

    private static void clearStaleMovementEffects(Player player) {
        if (!player.isOnline()) return;
        AttributeInstance movementSpeed = AttributeManager.getAttributeInstance(player, "generic_movement_speed");
        for (Map.Entry<PotionEffectType, NamespacedKey> entry : MOVEMENT_EFFECT_MODIFIERS.entrySet()) {
            PotionEffect effect = player.getPotionEffect(entry.getKey());
            // Older versions added zero-tick copies of these effects here; remove any that another plugin kept alive.
            if (effect != null && effect.getDuration() == 0) {
                player.removePotionEffect(entry.getKey());
                effect = player.getPotionEffect(entry.getKey());
            }
            if (effect != null || movementSpeed == null) continue;
            for (AttributeModifier modifier : new ArrayList<>(movementSpeed.getModifiers()))
                if (entry.getValue().equals(modifier.getKey()))
                    movementSpeed.removeModifier(modifier);
        }
    }
}
