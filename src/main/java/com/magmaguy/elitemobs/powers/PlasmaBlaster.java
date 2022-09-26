package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;


public class PlasmaBlaster extends CombatEnterScanPower {

    public PlasmaBlaster() {
        super(PowersConfig.getPower("plasma_blaster.yml"));
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
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 4);
    }

    private void doPower(EliteEntity eliteEntity) {
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(30, 30, 30))
            if (entity.getType().equals(EntityType.PLAYER)) {
                if (((Player) entity).getGameMode().equals(GameMode.SPECTATOR)) continue;
                Vector shotVector = entity.getLocation().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().normalize().multiply(0.5);
                createProjectile(shotVector, eliteEntity.getLocation(), eliteEntity);
                break;
            }
    }

    private void createProjectile(Vector shotVector, Location sourceLocation, EliteEntity sourceEntity) {
        new BukkitRunnable() {
            final Location currentLocation = sourceLocation.clone().add(new Vector(0, 1, 0));
            int counter = 0;

            @Override
            public void run() {
                if (counter > 20 * 3) {
                    cancel();
                    return;
                }
                counter++;
                for (Entity entity : currentLocation.getWorld().getNearbyEntities(currentLocation, 0.1 * counter / 12d, 0.1 * counter / 12d, 0.1 * counter / 12d)) {
                    if (entity.equals(sourceEntity.getLivingEntity())) continue;
                    if (!(entity instanceof LivingEntity)) continue;
                    cancel();
                    if (entity.getType() == EntityType.PLAYER) doDamage((Player) entity, sourceEntity);
                    break;
                }

                doVisualEffect(currentLocation, counter);

                currentLocation.add(shotVector);
                if (!currentLocation.getBlock().isPassable()) cancel();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void doDamage(Player player, EliteEntity sourceEntity) {
        player.damage(1, sourceEntity.getLivingEntity());
        doDamageFireworks(player.getLocation().clone().add(new Vector(0, 1, 0)));
    }

    private void doDamageFireworks(Location endLocation) {
        for (int i = 0; i < 200; i++) {
            endLocation.getWorld().spawnParticle(Particle.REDSTONE, endLocation.getX(), endLocation.getY(), endLocation.getZ(),
                    1, 3, 3, 3,
                    1, new Particle.DustOptions(Color.fromRGB(
                            ThreadLocalRandom.current().nextInt(122, 255),
                            ThreadLocalRandom.current().nextInt(122, 255),
                            ThreadLocalRandom.current().nextInt(0, 100)
                    ), 1));
        }
    }

    private void doVisualEffect(Location location, int counter) {
        location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(),
                20, 0.1 * counter / 12d, 0.1 * counter / 12d, 0.1 * counter / 12d, 1, new Particle.DustOptions(Color.fromRGB(
                        ThreadLocalRandom.current().nextInt(0, 100),
                        ThreadLocalRandom.current().nextInt(122, 255),
                        ThreadLocalRandom.current().nextInt(0, 100)
                ), 1));
    }

    protected void finishDeactivation(EliteEntity eliteEntity) {

    }
}
