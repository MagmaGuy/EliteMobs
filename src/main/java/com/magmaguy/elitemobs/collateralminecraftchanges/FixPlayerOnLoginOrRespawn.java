package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.DefaultConfig;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FixPlayerOnLoginOrRespawn implements Listener {
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 0, 0));
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 0, 0));
        if (DefaultConfig.isResetPlayerScaleOnLogin())
            event.getPlayer().getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1f);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerLoginEvent event) {
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 0, 0));
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 0, 0));
        if (DefaultConfig.isResetPlayerScaleOnLogin())
            event.getPlayer().getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1f);
    }
}
