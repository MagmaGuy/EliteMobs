package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class ChannelHealing extends CombatEnterScanPower {
    public ChannelHealing() {
        super(PowersConfig.getPower("channel_healing.yml"));
    }

    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        super.bukkitTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                    return;
                }
                doPower(eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 2);
    }

    private void doPower(EliteEntity eliteEntity) {
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(20, 20, 20)) {
            EliteEntity parsedEntity = EntityTracker.getEliteMobEntity(entity);
            if (parsedEntity == null) continue;
            if (parsedEntity.getHealth() / parsedEntity.getMaxHealth() > .8) continue;
            if (parsedEntity.isHealing()) continue;
            channelHealing(eliteEntity, parsedEntity);
            return;
        }

    }

    private void channelHealing(EliteEntity healer, EliteEntity damagedEntity) {
        super.setInCooldown(healer, true);
        healer.getLivingEntity().setAI(false);
        damagedEntity.setHealing(true);
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                if (!healer.isValid() ||
                        !damagedEntity.isValid() ||
                        damagedEntity.getHealth() / damagedEntity.getMaxHealth() > .8 ||
                        healer.getLocation().distance(damagedEntity.getLocation()) > 25) {
                    cancel();
                    setInCooldown(healer, false);
                    doCooldown(healer);
                    damagedEntity.setHealing(false);
                    if (healer.isValid())
                        healer.getLivingEntity().setAI(true);
                    return;
                }

                if (timer % 10 == 0 && timer > 0) {
                    double healAmount = healer.getLevel() / 2d;
                    damagedEntity.heal(healAmount);
                    damagedEntity.getLocation().getWorld().spawnParticle(Particle.TOTEM, damagedEntity.getLocation().add(new Vector(0, 1, 0)), 20, 0.1, 0.1, 0.1);
                }

                Vector toDamaged = damagedEntity.getLocation().add(new Vector(0, 1, 0))
                        .subtract(healer.getLocation().add(new Vector(0, 1, 0)))
                        .toVector().normalize().multiply(.5);

                Location rayLocation = healer.getLocation().add(new Vector(0, 1, 0)).add(toDamaged);

                for (int i = 0; i < 55; i++) {
                    rayLocation.getWorld().spawnParticle(Particle.TOTEM, rayLocation, 1, toDamaged.getX(), toDamaged.getY(), toDamaged.getZ(), .2D);
                    rayLocation.add(toDamaged);
                    if (rayLocation.distance(damagedEntity.getLocation()) < 2)
                        break;
                }

                timer++;

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {

    }

}
