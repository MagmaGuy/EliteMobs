package com.magmaguy.elitemobs.collateralminecraftchanges;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FixPlayerSpeedOnRespawn implements Listener {
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 0, 0));
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 0, 0));
    }
}
