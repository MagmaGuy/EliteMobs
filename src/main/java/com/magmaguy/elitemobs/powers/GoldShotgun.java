package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Boss power that fires a shotgun blast of gold nuggets at the player.
 * Uses packet-based FakeItem projectiles that cannot be picked up.
 */
public class GoldShotgun extends BossPower implements Listener {

    public GoldShotgun() {
        super(PowersConfig.getPower("gold_shotgun.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        GoldShotgun goldShotgun = (GoldShotgun) event.getEliteMobEntity().getPower(this);
        if (goldShotgun == null) return;
        if (!eventIsValid(event, goldShotgun)) return;

        goldShotgun.doCooldownTicks(event.getEliteMobEntity());
        goldShotgun.doGoldShotgun(event.getEliteMobEntity(), event.getPlayer());
    }

    private void doGoldShotgun(EliteEntity eliteEntity, Player player) {
        eliteEntity.getLivingEntity().setAI(false);
        Vector shotVector = player.getLocation().add(new Vector(0, 1, 0)).toVector()
                .subtract(eliteEntity.getLivingEntity().getLocation().toVector()).normalize().multiply(.5);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!eliteEntity.isValid()) {
                    cancel();
                    return;
                }

                if (counter % 10 == 0)
                    doSmokeEffect(eliteEntity, shotVector);
                counter++;

                if (counter < 20 * 3) return;

                cancel();
                eliteEntity.getLivingEntity().setAI(true);
                List<ProjectileDamage.FakeProjectile> projectiles = generateVisualProjectiles(eliteEntity, shotVector);
                if (projectiles == null || projectiles.isEmpty()) return;
                ProjectileDamage.doGoldNuggetDamage(projectiles, eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void doSmokeEffect(EliteEntity eliteEntity, Vector shotVector) {
        for (int i = 0; i < 200; i++) {
            Vector visualShotVector = getShotVector(shotVector);
            eliteEntity.getLivingEntity().getWorld().spawnParticle(
                    Particle.SMOKE,
                    eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 0.5, 0)),
                    0,
                    visualShotVector.getX(),
                    visualShotVector.getY(),
                    visualShotVector.getZ(),
                    0.75);
        }
    }

    private List<ProjectileDamage.FakeProjectile> generateVisualProjectiles(EliteEntity eliteEntity, Vector shotVector) {
        List<ProjectileDamage.FakeProjectile> projectiles = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            ProjectileDamage.FakeProjectile projectile = ProjectileDamage.createGoldNuggetProjectile(
                    eliteEntity.getLivingEntity().getLocation(),
                    getShotVector(shotVector));
            projectiles.add(projectile);
        }
        return projectiles;
    }

    private Vector getShotVector(Vector originalShotVector) {
        return originalShotVector.clone().add(
                new Vector(
                        ThreadLocalRandom.current().nextDouble(-.1, .1),
                        ThreadLocalRandom.current().nextDouble(-.1, .1),
                        ThreadLocalRandom.current().nextDouble(-.1, .1)
                ).normalize().multiply(0.1));
    }
}
