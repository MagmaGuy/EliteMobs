package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.BossPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HyperLoot extends BossPower implements Listener {

    public HyperLoot() {
        super(PowersConfig.getPower("hyper_loot.yml"));
    }

    @EventHandler
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        for (int i = 0; i < 10; i++)
            LootTables.generatePlayerLoot(event.getEliteMobEntity());
    }

}
