package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class AttackLightning extends MinorPower implements Listener {
    public AttackLightning() {
        super(PowersConfig.getPower("attack_lightning.yml"));
    }

    @EventHandler
    public void onTarget(EliteMobTargetPlayerEvent event) {
        AttackLightning attackLightning = (AttackLightning) event.getEliteMobEntity().getPower(this);
        if (attackLightning == null) return;
        if (attackLightning.getIsFiring()) return;

        attackLightning.setIsFiring(true);
        lightningScan(attackLightning, event.getEliteMobEntity());
    }

    public void lightningScan(AttackLightning attackLightning, EliteMobEntity eliteMobEntity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!eliteMobEntity.getLivingEntity().isValid() || ((Monster) eliteMobEntity.getLivingEntity()).getTarget() == null) {
                    cancel();
                    attackLightning.setIsFiring(false);
                    return;
                }
                for (Entity entity : eliteMobEntity.getLivingEntity().getNearbyEntities(20, 20, 20))
                    if (entity instanceof Player)
                        entity.getWorld().strikeLightning(entity.getLocation());
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 10);
    }

}
