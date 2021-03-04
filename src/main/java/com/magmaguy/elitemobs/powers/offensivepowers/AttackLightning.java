package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
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
    public void onTarget(EliteMobDamagedByPlayerEvent event) {
        AttackLightning attackLightning = (AttackLightning) event.getEliteMobEntity().getPower(this);
        if (attackLightning == null) return;
        if (attackLightning.isCooldown()) return;

        attackLightning.doCooldown(20 * PowersConfig.getPower("attack_lightning.yml").getConfiguration().getInt("delayBetweenStrikes"));

        attackLightning.setIsFiring(true);
        fireLightning(event.getEliteMobEntity());
    }

    public void fireLightning(EliteMobEntity eliteMobEntity) {
        for (Entity entity : eliteMobEntity.getLivingEntity().getLocation().getWorld().getNearbyEntities(eliteMobEntity.getLivingEntity().getLocation(), 20, 20, 20))
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
                    location.getWorld().strikeLightning(location);
                    cancel();
                    return;
                }
                location.getWorld().spawnParticle(Particle.CRIT, location, 10, 0.5, 1.5, 0.5, 0.3);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    //public void lightningScan(AttackLightning attackLightning, EliteMobEntity eliteMobEntity) {
    //    new BukkitRunnable() {
    //        @Override
    //        public void run() {
    //            if (!eliteMobEntity.getLivingEntity().isValid() || ((Mob) eliteMobEntity.getLivingEntity()).getTarget() == null) {
    //                cancel();
    //                attackLightning.setIsFiring(false);
    //                return;
    //            }
    //            for (Entity entity : eliteMobEntity.getLivingEntity().getNearbyEntities(20, 20, 20))
    //                if (entity instanceof Player)
    //                    lightningUUIDs.add(entity.getWorld().strikeLightning(entity.getLocation()).getUniqueId());
    //        }
    //    }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * PowersConfig.getPower("attack_lightning.yml").getConfiguration().getInt("delayBetweenStrikes"));
    //}
//
    //@EventHandler
    //public void onDamage(EntityDamageByEntityEvent event) {
    //    if (!lightningUUIDs.contains(event.getDamager().getUniqueId())) return;
    //    lightningUUIDs.remove(event.getDamager().getUniqueId());
    //    event.setDamage(1);
    //}

}
