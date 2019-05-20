package com.magmaguy.elitemobs.mobpowers.bosspowers;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.mobpowers.BossPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HyperLoot extends BossPower implements Listener {

    public HyperLoot() {
        super("HyperLoot", null);
    }

    @EventHandler
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        for (int i = 0; i < 5; i++)
            LootTables.generateLoot(event.getEliteMobEntity());
    }

}
