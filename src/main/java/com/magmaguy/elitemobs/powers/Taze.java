package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Taze extends BossPower implements Listener {

    public Taze() {
        super(PowersConfig.getPower("taze.yml"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamagedEvent(PlayerDamagedByEliteMobEvent event) {
        Taze taze = (Taze) event.getEliteMobEntity().getPower(this);
        if (taze == null) return;
        if (!eventIsValid(event, taze)) return;
        taze.doCooldown(event.getEliteMobEntity());
        taze(event.getPlayer(), event.getEliteMobEntity().getLocation(), 0);
    }

    public void taze(Player player, Location entityLocation, int counter) {
        if (counter > 2) return;
        player.setVelocity(player.getLocation().subtract(entityLocation.toVector()).toVector().normalize());
        player.sendTitle("", "Shocked!", 1, 30, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 5));
        new BukkitRunnable() {
            @Override
            public void run() {
                taze(player, entityLocation, counter + 1);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 5);
    }

}
