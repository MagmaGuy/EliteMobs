package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.BossPower;
import com.magmaguy.elitemobs.powers.ProjectileTagger;
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

    public FrostCone() {
        super(PowersConfig.getPower("frost_cone.yml"));
    }

    @EventHandler
    public void onDamagedEvent(EliteMobDamagedByPlayerEvent event) {
        FrostCone frostCone = (FrostCone) event.getEliteMobEntity().getPower(this);
        if (frostCone == null) return;
        if (frostCone.isCooldown()) return;

        frostCone.doCooldown(20 * 10, event.getEliteMobEntity());

        frostCone.setIsFiring(true);
        startFrostCone(event.getEliteMobEntity(), event.getPlayer().getLocation().clone());
    }

    public static void startFrostCone(EliteMobEntity eliteMobEntity, Location damager) {
        if (eliteMobEntity == null || eliteMobEntity.getLivingEntity() == null || !eliteMobEntity.getLivingEntity().isValid())
            return;
        eliteMobEntity.getLivingEntity().setAI(false);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;

                //ending phase
                if (counter > 20 * 6 || !eliteMobEntity.getLivingEntity().isValid()) {
                    cancel();
                    if (!eliteMobEntity.getLivingEntity().isDead())
                        eliteMobEntity.getLivingEntity().setAI(true);
                    return;
                }

                //warning phase
                if (counter < 20 * 3) {
                    doSmokeEffect(eliteMobEntity, damager);
                    return;
                }

                //firing phase
                for (int i = 0; i < 10; i++)
                    createSnowball(eliteMobEntity, damager);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static void doSmokeEffect(EliteMobEntity eliteMobEntity, Location location) {
        for (int i = 0; i < 100; i++) {
            Vector shotVector = getShotVector(eliteMobEntity, location);
            eliteMobEntity.getLivingEntity().getWorld().spawnParticle(
                    Particle.SMOKE_NORMAL,
                    eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 1, 0)),
                    0,
                    shotVector.getX(),
                    shotVector.getY(),
                    shotVector.getZ(),
                    0.75);
        }
    }

    private static Vector getShotVector(EliteMobEntity eliteMobEntity, Location location) {
        return location.clone().subtract(eliteMobEntity.getLivingEntity().getLocation().clone()).toVector()
                .normalize()
                .add(new Vector(
                        ThreadLocalRandom.current().nextDouble() * 1 - 0.5,
                        ThreadLocalRandom.current().nextDouble() * 0.5 - 0.25,
                        ThreadLocalRandom.current().nextDouble() * 1 - 0.5))
                .normalize();
    }

    private static Snowball createSnowball(EliteMobEntity eliteMobEntity, Location location) {
        Projectile snowball = EliteProjectile.create(EntityType.SNOWBALL, eliteMobEntity.getLivingEntity(), getShotVector(eliteMobEntity, location), false);
        ProjectileTagger.tagProjectileWithCustomDamage(snowball, 2);
        snowball.getPersistentDataContainer().set(frostConeSnowballKey, PersistentDataType.STRING, "true");
        return (Snowball) snowball;
    }

    private static final NamespacedKey frostConeSnowballKey = new NamespacedKey(MetadataHandler.PLUGIN, "frost_cone_snowball");
    private static HashMap<Player, Integer> frostconePlayer = new HashMap<>();

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
            int amount = frostconePlayer.get(event.getPlayer());
            @Override
            public void run() {
                if (!frostconePlayer.containsKey(event.getPlayer())) return;
                if (amount != frostconePlayer.get(event.getPlayer())) return;
                frostconePlayer.remove(event.getPlayer());
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 *5);
    }

}
