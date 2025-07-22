package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FixPlayerOnLoginOrRespawn implements Listener {
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 0, 0));
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 0, 0));
        if (DefaultConfig.isResetPlayerScaleOnLogin())
            AttributeManager.setAttribute(event.getPlayer(), "generic_scale", 1);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 0, 0));
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 0, 0));
        if (DefaultConfig.isResetPlayerScaleOnLogin())
            AttributeManager.setAttribute(event.getPlayer(), "generic_scale", 1);
    }
}
