package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LuaPowerEvents implements Listener {

    @EventHandler
    public void onEliteMobRemove(EliteMobRemoveEvent event) {
        for (ElitePower elitePower : event.getEliteMobEntity().getElitePowers()) {
            if (elitePower instanceof LuaElitePower luaElitePower) {
                luaElitePower.closeRuntime();
            }
        }
    }
}
