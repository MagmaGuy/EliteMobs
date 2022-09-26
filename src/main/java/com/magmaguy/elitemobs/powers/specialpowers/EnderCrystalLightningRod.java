package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.EnderDragonEmpoweredLightning;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class EnderCrystalLightningRod {

    public EnderCrystalLightningRod(EliteEntity eliteEntity, EnderCrystal enderCrystal) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!eliteEntity.isValid() || !enderCrystal.isValid()) {
                    cancel();
                    return;
                }

                if (counter % 5 == 0) {
                    Vector randomVector = new Vector(
                            ThreadLocalRandom.current().nextInt(-15, 15),
                            0,
                            ThreadLocalRandom.current().nextInt(-15, 15));

                    EnderDragonEmpoweredLightning.lightningTask(enderCrystal.getLocation().clone().add(randomVector));
                }

                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
    }

    public static class EnderCrystalLightningRodEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void damageEvent(EntityDamageEvent event) {
            if (!event.getEntity().getType().equals(EntityType.ENDER_CRYSTAL)) return;
            if (!(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) ||
                    event.getCause().equals(EntityDamageEvent.DamageCause.FALLING_BLOCK) ||
                    event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING))) return;
            if (!event.getEntity().getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, "eliteCrystal"), PersistentDataType.STRING))
                return;
            event.setCancelled(true);
        }
    }

}
