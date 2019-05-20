package com.magmaguy.elitemobs.mobpowers.miscellaneouspowers;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class BonusLoot extends MinorPower implements Listener {

    public BonusLoot() {
        super("BonusLoot", Material.CHEST);
    }

    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        LootTables.generateLoot(event.getEliteMobEntity());
    }

}
