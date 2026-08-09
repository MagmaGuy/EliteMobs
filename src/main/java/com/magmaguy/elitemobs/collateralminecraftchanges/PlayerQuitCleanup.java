package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.combatsystem.antiexploit.AutoclickerThrottle;
import com.magmaguy.elitemobs.powers.scripts.ScriptAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitCleanup implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        java.util.UUID playerId = event.getPlayer().getUniqueId();
        AutoclickerThrottle.onPlayerQuit(playerId);
        if (ScriptAction.getInvulnerablePlayers().remove(playerId))
            event.getPlayer().setInvulnerable(false);
    }
}
