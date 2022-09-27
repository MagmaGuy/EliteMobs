package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import com.magmaguy.elitemobs.powers.meta.ProjectileTagger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class FrostCone extends BossPower implements Listener {

    private static final NamespacedKey frostConeSnowballKey = new NamespacedKey(MetadataHandler.PLUGIN, "frost_cone_snowball");
    private static final HashMap<Player, Integer> frostconePlayer = new HashMap<>();

    public FrostCone() {
        super(PowersConfig.getPower("frost_cone.yml"));
    }

    public static void startFrostCone(EliteEntity eliteEntity, Location damager, FrostCone frostCone) {

        if (eliteEntity == null || eliteEntity.getLivingEntity() == null || !eliteEntity.getLivingEntity().isValid())
            return;
        eliteEntity.getLivingEntity().setAI(false);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;

                //ending phase
                if (counter > 20 * 6 || !isPowerStillValid(eliteEntity, damager)) {
                    cancel();
                    if (eliteEntity.getLivingEntity() != null && !eliteEntity.getLivingEntity().isDead())
                        eliteEntity.getLivingEntity().setAI(true);
                    frostCone.setFiring(false);
                    return;
                }

                //warning phase
                if (counter < 20 * 3) {
                    doSmokeEffect(eliteEntity, damager);
                    return;
                }

                //firing phase
                for (int i = 0; i < 10; i++)
                    createSnowball(eliteEntity, damager);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);


    }

    private static void doSmokeEffect(EliteEntity eliteEntity, Location location) {
        for (int i = 0; i < 100; i++) {
            Vector shotVector = getShotVector(eliteEntity, location);
            eliteEntity.getLivingEntity().getWorld().spawnParticle(
                    Particle.SMOKE_NORMAL,
                    eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 1, 0)),
                    0,
                    shotVector.getX(),
                    shotVector.getY(),
                    shotVector.getZ(),
                    0.75);
        }
    }

    private static boolean isPowerStillValid(EliteEntity eliteEntity, Location playerLocation) {
        if (!eliteEntity.isValid())
            return false;
        return eliteEntity.getLivingEntity().getWorld().equals(playerLocation.getWorld());
    }

    private static Vector getShotVector(EliteEntity eliteEntity, Location location) {
        return location.clone().subtract(eliteEntity.getLivingEntity().getLocation().clone()).toVector()
                .normalize()
                .add(new Vector(
                        ThreadLocalRandom.current().nextDouble() * 1 - 0.5,
                        ThreadLocalRandom.current().nextDouble() * 0.5 - 0.25,
                        ThreadLocalRandom.current().nextDouble() * 1 - 0.5))
                .normalize();
    }

    private static Snowball createSnowball(EliteEntity eliteEntity, Location location) {
        Projectile snowball = EliteProjectile.create(EntityType.SNOWBALL, eliteEntity.getLivingEntity(), getShotVector(eliteEntity, location), false);
        ProjectileTagger.tagProjectileWithCustomDamage(snowball, 2);
        snowball.getPersistentDataContainer().set(frostConeSnowballKey, PersistentDataType.STRING, "true");
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, snowball::remove, 20L * 3);
        return (Snowball) snowball;
    }

    @EventHandler
    public void onDamagedEvent(EliteMobDamagedByPlayerEvent event) {
        FrostCone frostCone = (FrostCone) event.getEliteMobEntity().getPower(this);
        if (frostCone == null) return;
        if (frostCone.isInCooldown(event.getEliteMobEntity())) return;
        frostCone.doCooldownTicks(event.getEliteMobEntity());

        frostCone.setFiring(true);
        startFrostCone(event.getEliteMobEntity(), event.getPlayer().getLocation().clone(), frostCone);
    }

    @EventHandler(ignoreCancelled = true)
    public void playerHitByFrostConeSnowball(PlayerDamagedByEliteMobEvent event) {
        if (event.getProjectile() == null) return;
        if (!event.getProjectile().getPersistentDataContainer().has(frostConeSnowballKey, PersistentDataType.STRING))
            return;
        if (!frostconePlayer.containsKey(event.getPlayer()))
            frostconePlayer.put(event.getPlayer(), 1);
        else
            frostconePlayer.put(event.getPlayer(), frostconePlayer.get(event.getPlayer()) + 1);
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 7, frostconePlayer.get(event.getPlayer())));
        new BukkitRunnable() {
            final int amount = frostconePlayer.get(event.getPlayer());

            @Override
            public void run() {
                if (!frostconePlayer.containsKey(event.getPlayer())) return;
                if (amount != frostconePlayer.get(event.getPlayer())) return;
                frostconePlayer.remove(event.getPlayer());
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L * 5);
    }

}
