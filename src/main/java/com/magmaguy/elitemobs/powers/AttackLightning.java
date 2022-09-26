package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class AttackLightning extends MinorPower implements Listener {
    public AttackLightning() {
        super(PowersConfig.getPower("attack_lightning.yml"));
    }

    @EventHandler
    public void onDamagedEvent(EliteMobDamagedByPlayerEvent event) {
        AttackLightning attackLightning = (AttackLightning) event.getEliteMobEntity().getPower(this);
        if (attackLightning == null) return;
        if (attackLightning.isInGlobalCooldown()) return;

        attackLightning.doGlobalCooldown(20 * PowersConfig.getPower("attack_lightning.yml").getFileConfiguration().getInt("delayBetweenStrikes"));

        attackLightning.setFiring(true);
        fireLightning(event.getEliteMobEntity());
    }

    public void fireLightning(EliteEntity eliteEntity) {
        for (Entity entity : eliteEntity.getLivingEntity().getLocation().getWorld().getNearbyEntities(eliteEntity.getLivingEntity().getLocation(), 20, 20, 20))
            if (entity.getType().equals(EntityType.PLAYER))
                lightningTask(entity.getLocation().clone());
    }

    public void lightningTask(Location location) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                if (counter > 20 * 3) {
                    LightningSpawnBypass.bypass();
                    location.getWorld().strikeLightning(location);
                    cancel();
                    return;
                }
                location.getWorld().spawnParticle(Particle.CRIT, location, 10, 0.5, 1.5, 0.5, 0.3);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

}
