package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.powers.ProjectileLocationGenerator;
import com.magmaguy.elitemobs.config.events.premade.KrakenEventConfig;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

public class Kraken implements Listener {

    private static final HashSet<Squid> krakens = new HashSet<>();

    public static boolean isKraken(Squid kraken) {
        return krakens.contains(kraken);
    }

    public static void removeKraken(Squid kraken) {
        krakens.remove(kraken);
    }

    private static final HashSet<Fireball> fireballs = new HashSet<>();

    public static boolean isFireball(Fireball fireball) {
        return fireballs.contains(fireball);
    }

    public static void removeFireball(Fireball fireball) {
        fireballs.remove(fireball);
    }

    public static void spawnKraken(Location location) {
        krakenSpawnEffect(location);
    }

    private static void krakenSpawnEffect(Location location) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > 20 * 3) {

                    initializeKraken(location);
                    cancel();
                    return;

                }

                location.getWorld().spawnParticle(Particle.PORTAL, location, 20, 1, 1, 1, 0.1);

                counter++;

            }


        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static void initializeKraken(Location location) {

        Squid kraken = (Squid) location.getWorld().spawnEntity(location, EntityType.SQUID);

        krakens.add(kraken);

        /*
        This boss is not fully integrated into the plugin yet, might not really make the cut
         */

        kraken.setMaxHealth(100);
        kraken.setHealth(100);

        kraken.setCustomName(KrakenEventConfig.krakenName);
        kraken.setCustomNameVisible(true);

        krakenDamageLoop(kraken);
        krakenVisualEffectLoop(kraken);

    }

    private static void krakenDamageLoop(Squid kraken) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (kraken.isDead()) {
                    cancel();
                    return;
                }

                for (Entity entity : kraken.getNearbyEntities(16, 16, 16))
                    if (entity instanceof Player && (((Player) entity).getGameMode().equals(GameMode.ADVENTURE) ||
                            ((Player) entity).getGameMode().equals(GameMode.SURVIVAL)))
                        fireballInitializer(kraken, (Player) entity);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 3, 20 * 3);

    }

    private static void fireballInitializer(Squid kraken, Player target) {

        Location offsetLocation = ProjectileLocationGenerator.generateLocation(kraken, target);
        Fireball repeatingFireball = (Fireball) kraken.getWorld().spawnEntity(offsetLocation, EntityType.FIREBALL);
        Vector targetterToTargetted = target.getEyeLocation().subtract(repeatingFireball.getLocation()).toVector()
                .normalize().multiply(1);

        repeatingFireball.setDirection(targetterToTargetted);
        repeatingFireball.setIsIncendiary(true);
        repeatingFireball.setYield(2F);

        fireballs.add(repeatingFireball);

    }

    private static void krakenVisualEffectLoop(Squid kraken) {

        new BukkitRunnable() {

            final UUID uuid = kraken.getUniqueId();

            @Override
            public void run() {

                Squid localKraken = kraken;

                if (!kraken.isValid())
                    if (Bukkit.getEntity(uuid) != null)
                        localKraken = (Squid) Bukkit.getEntity(uuid);

                if (kraken.isDead()) {

                    cancel();
                    return;

                }

                localKraken.getWorld().spawnParticle(Particle.FLAME, localKraken.getLocation(), 10, 0.1, 0.1, 0.1, 0.05);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    @EventHandler
    public void onSuffocate(EntityDamageEvent event) {

        if (event.isCancelled()) return;
        if (!event.getEntity().getType().equals(EntityType.SQUID)) return;
        if (!isKraken((Squid) event.getEntity())) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING) &&
                !event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)) return;

        event.setCancelled(true);

    }

    @EventHandler
    public void onSelfDamage(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Fireball && ((Fireball) event.getDamager()).getShooter() != null &&
                ((Fireball) event.getDamager()).getShooter() instanceof Squid) {
            event.setCancelled(true);
            removeFireball((Fireball) event.getDamager());
        }

    }

    @EventHandler
    public void fireballDamage(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (!event.getDamager().getType().equals(EntityType.FIREBALL)) return;
        if (!isFireball((Fireball) event.getDamager())) return;

        for (EntityDamageByEntityEvent.DamageModifier modifier : EntityDamageByEntityEvent.DamageModifier.values())
            if (event.isApplicable(modifier))
                event.setDamage(modifier, 0);

        event.setDamage(EntityDamageEvent.DamageModifier.BASE, 2);

        removeFireball((Fireball) event.getDamager());

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!event.getEntity().getType().equals(EntityType.SQUID)) return;
        if (!isKraken((Squid) event.getEntity())) return;

        //event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), CustomItem.getCustomItem("depths_seeker.yml").generateItemStack(10));
        removeKraken((Squid) event.getEntity());

    }

}


