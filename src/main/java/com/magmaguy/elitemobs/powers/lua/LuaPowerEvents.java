package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

public class LuaPowerEvents implements Listener {

    // A dead boss's runtime is already detached from its power, so this does not end its death phase.
    @EventHandler
    public void onEliteMobRemove(EliteMobRemoveEvent event) {
        for (ElitePower elitePower : event.getEliteMobEntity().getElitePowers()) {
            if (elitePower instanceof LuaElitePower luaElitePower) {
                luaElitePower.closeRuntime();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        ScriptableBoss.shutdownDeathPhasesIn(event.getWorld());
    }
}
