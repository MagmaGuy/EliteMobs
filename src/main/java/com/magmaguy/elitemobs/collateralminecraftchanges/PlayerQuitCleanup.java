package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.powers.scripts.ScriptAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitCleanup implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if (ScriptAction.getInvulnerablePlayers().contains(event.getPlayer()))
            event.getPlayer().setInvulnerable(false);
    }
}
