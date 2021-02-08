package com.magmaguy.elitemobs.powers.miscellaneouspowers;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class BonusLoot extends MinorPower implements Listener {

    public BonusLoot() {
        super(PowersConfig.getPower("bonus_loot.yml"));
    }

    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        LootTables.generatePlayerLoot(event.getEliteMobEntity());
    }

}
