package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Boss power that explodes gold nuggets outward in all directions.
 * Uses packet-based FakeItem projectiles that cannot be picked up.
 */
public class GoldExplosion extends BossPower implements Listener {

    public GoldExplosion() {
        super(PowersConfig.getPower("gold_explosion.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        GoldExplosion goldExplosion = (GoldExplosion) event.getEliteMobEntity().getPower(this);
        if (goldExplosion == null) return;
        if (!eventIsValid(event, goldExplosion)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        goldExplosion.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doGoldExplosion(event.getEliteMobEntity());
    }

    private void doGoldExplosion(EliteEntity eliteEntity) {
        eliteEntity.getLivingEntity().setAI(false);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!eliteEntity.isValid()) {
                    cancel();
                    return;
                }

                counter++;
                if (MobCombatSettingsConfig.isEnableWarningVisualEffects())
                    eliteEntity.getLivingEntity().getWorld().spawnParticle(
                            Particle.SMOKE,
                            eliteEntity.getLivingEntity().getLocation(),
                            counter, 1, 1, 1, 0);

                if (counter < 20 * 1.5) return;
                cancel();
                eliteEntity.getLivingEntity().setAI(true);
                List<ProjectileDamage.FakeProjectile> projectiles = generateVisualProjectiles(eliteEntity);
                ProjectileDamage.doGoldNuggetDamage(projectiles, eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private List<ProjectileDamage.FakeProjectile> generateVisualProjectiles(EliteEntity eliteEntity) {
        List<ProjectileDamage.FakeProjectile> projectiles = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Vector velocityVector = new Vector(
                    ThreadLocalRandom.current().nextDouble() - 0.5,
                    ThreadLocalRandom.current().nextDouble() / 1.5,
                    ThreadLocalRandom.current().nextDouble() - 0.5);

            ProjectileDamage.FakeProjectile projectile = ProjectileDamage.createGoldNuggetProjectile(
                    eliteEntity.getLivingEntity().getLocation().clone()
                            .add(new Vector(velocityVector.getX(), 0.5, velocityVector.getZ())),
                    velocityVector);
            projectile.setGravity(true);
            projectiles.add(projectile);
        }
        return projectiles;
    }
}
