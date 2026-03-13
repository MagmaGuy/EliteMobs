package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FrozenCustomBossKnockbackHandler implements Listener {

    @EventHandler
    public void onEliteDamaged(EliteMobDamagedEvent event) {
        if (!(event.getEliteEntity() instanceof CustomBossEntity customBossEntity)) {
            return;
        }
        if (!customBossEntity.getCustomBossesConfigFields().isFrozen() || !customBossEntity.isValid()) {
            return;
        }

        event.getEntity().setVelocity(new Vector(0, 0, 0));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!customBossEntity.isValid()) {
                    return;
                }
                event.getEntity().setVelocity(new Vector(0, 0, 0));
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }
}
