package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatTag implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {

        Player player = CombatParticipantResolver.resolveEliteCombatPlayer(event);

        if (player == null) return;

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        //if (player.isInvulnerable()) player.setInvulnerable(false);
        if (player.isFlying()) {
            player.setFlying(false);
            ActionBarCompositor.show(player, ActionBarCompositor.Source.COMBAT_TRANSITION,
                    CombatTagConfig.getCombatTagMessage());
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
