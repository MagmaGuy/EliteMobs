package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.BossPower;
import com.magmaguy.elitemobs.powers.offensivepowers.AttackArrow;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class BulletHell extends BossPower implements Listener {
    public BulletHell() {
        super(PowersConfig.getPower("bullet_hell.yml"));
    }

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        BulletHell bulletHell = (BulletHell) event.getEliteMobEntity().getPower(this);
        if (bulletHell == null) return;
        if (!eventIsValid(event, bulletHell)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        bulletHell.doCooldown(20 * 20, event.getEliteMobEntity());
        doBulletHell(event.getEliteMobEntity());

    }

    public void doBulletHell(EliteMobEntity eliteMobEntity) {
        eliteMobEntity.getLivingEntity().setAI(false);
        if (eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)).getBlock().getType().equals(Material.AIR))
            eliteMobEntity.getLivingEntity().teleport(eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)));
        new BukkitRunnable() {
            int counter = 0;
            final Location initialLocation = eliteMobEntity.getLivingEntity().getLocation().clone();

            @Override
            public void run() {

                if (!eliteMobEntity.getLivingEntity().isValid() || eliteMobEntity.getLivingEntity().isDead()) {
                    cancel();
                    return;
                }

                eliteMobEntity.getLivingEntity().getWorld().spawnParticle(Particle.DRIP_WATER, eliteMobEntity.getLivingEntity().getLocation(), 10, 1, 1, 1);

                for (Entity nearbyEntity : eliteMobEntity.getLivingEntity().getNearbyEntities(20, 20, 20))
                    if (nearbyEntity instanceof Player)
                        if (((Player) nearbyEntity).getGameMode().equals(GameMode.ADVENTURE) ||
                                ((Player) nearbyEntity).getGameMode().equals(GameMode.SURVIVAL)) {
                            Arrow arrow = AttackArrow.shootArrow(eliteMobEntity.getLivingEntity(), (Player) nearbyEntity);
                            arrow.setVelocity(arrow.getVelocity().multiply(0.1));
                            arrow.setGravity(false);
                            trackingArrowLoop((Player) nearbyEntity, arrow);
                        }

                counter++;
                if (counter > 20) {
                    cancel();
                    eliteMobEntity.getLivingEntity().setAI(true);
                    eliteMobEntity.getLivingEntity().teleport(initialLocation);
                }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 10);
    }


    private static void trackingArrowLoop(Player player, Arrow arrow) {
        EntityTracker.registerCullableEntity(arrow);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (player.isValid() && !player.isDead() && arrow.isValid() && arrow.getWorld().equals(player.getWorld())
                        && player.getLocation().distanceSquared(arrow.getLocation()) < 900 && !arrow.isOnGround()) {
                    if (counter % 10 == 0)
                        arrow.setVelocity(arrow.getVelocity().add(arrowAdjustmentVector(arrow, player)));
                    arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 10, 0.01, 0.01, 0.01, 0.01);
                } else {
                    arrow.setGravity(true);
                    EntityTracker.unregisterCullableEntity(arrow);
                    cancel();
                }
                if (counter > 20 * 10) {
                    EntityTracker.unregisterCullableEntity(arrow);
                    arrow.setGravity(true);
                    cancel();
                }
                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static Vector arrowAdjustmentVector(Arrow arrow, Player player) {
        return player.getEyeLocation().clone().subtract(new Vector(0, 0.5, 0)).subtract(arrow.getLocation()).toVector().normalize().multiply(0.1);
    }

}
