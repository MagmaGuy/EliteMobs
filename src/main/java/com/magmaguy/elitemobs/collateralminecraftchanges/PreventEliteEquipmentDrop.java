package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PreventEliteEquipmentDrop implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEliteDeath(EliteMobDeathEvent event) {
        if (event.getEntityDeathEvent() == null) return;
        if (!(event.getEntity() instanceof Mob)) return;
        if (((Mob) event.getEntity()).getEquipment() == null) return;
        for (ItemStack drop : event.getEntityDeathEvent().getDrops())
            for (ItemStack equipment : ((Mob) event.getEntity()).getEquipment().getArmorContents())
                if (equipment != null && drop != null && drop.equals(equipment))
                        drop.setType(Material.AIR);
    }

}
