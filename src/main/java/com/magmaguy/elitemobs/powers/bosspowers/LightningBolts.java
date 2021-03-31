package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.BossPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class LightningBolts extends BossPower implements Listener {

    public LightningBolts() {
        super(PowersConfig.getPower("lightning_bolts.yml"));
    }

    @EventHandler
    public void onDamagedEvent(EliteMobDamagedByPlayerEvent event) {
        LightningBolts lightningBolts = (LightningBolts) event.getEliteMobEntity().getPower(this);
        if (lightningBolts == null) return;
        if (lightningBolts.isCooldown()) return;

        lightningBolts.doCooldown(20 * PowersConfig.getPower("attack_lightning.yml").getConfiguration().getInt("delayBetweenStrikes"));

        lightningBolts.setIsFiring(true);
        setLightiningPaths(event.getEliteMobEntity());
    }

    private static void setLightiningPaths(EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity.getLivingEntity() == null) return;
        eliteMobEntity.getLivingEntity().setAI(false);
        for (Entity entity : eliteMobEntity.getLivingEntity().getNearbyEntities(20, 20, 20)) {
            if (entity.getType().equals(EntityType.PLAYER)) {
                Location playerLocationClone = entity.getLocation().clone();
                Location powerLocation = eliteMobEntity.getLivingEntity().getLocation().clone();
                Vector powerDirection = playerLocationClone.clone().subtract(powerLocation).toVector().normalize();
                int counter = 0;
                while (playerLocationClone.distance(powerLocation) > 0.55) {
                    counter++;
                    powerLocation.add(powerDirection);
                    lightningTask(powerLocation.clone(), counter);
                }
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (eliteMobEntity != null && eliteMobEntity.getLivingEntity() != null)
                    eliteMobEntity.getLivingEntity().setAI(true);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 4 * 20);
    }

    public static void lightningTask(Location location, int counter) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                if (counter > 20 * 2) {
                    location.getWorld().strikeLightning(location);
                    cancel();
                    return;
                }
                location.getWorld().spawnParticle(Particle.CRIT, location, 10, 0.5, 1.5, 0.5, 0.3);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, counter * 5L, 1);

    }

}
