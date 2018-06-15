package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.elitedrops.UniqueItemConstructor;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Balrog implements Listener {

    public static void spawnBalrog(Location location) {

        balrogSpawnEffect(location.add(new Vector(0.5, 0.5, 0.5)));

    }

    private static void balrogSpawnEffect(Location location) {

        new BukkitRunnable() {

            int i = 0;

            @Override
            public void run() {

                if (i > 20 * 3) {

                    intializeBalrog(location);
                    cancel();
                    return;

                }

                location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 4, 0.1, 0.1, 0.1, 0.05);
                location.getWorld().spawnParticle(Particle.FLAME, location, 2, 0.1, 0.1, 0.1, 0.05);

                i++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static void intializeBalrog(Location location) {

        Silverfish balrog = (Silverfish) location.getWorld().spawnEntity(location, EntityType.SILVERFISH);
        balrog.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, ConfigValues.eventsConfig.getDouble(EventsConfig.BALROG_LEVEL)));
        balrog.setMetadata(MetadataHandler.BALROG, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        balrog.setMetadata(MetadataHandler.EVENT_CREATURE, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        balrog.setMetadata(MetadataHandler.CUSTOM_ARMOR, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        balrog.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        balrog.setMetadata(MetadataHandler.CUSTOM_STACK, new FixedMetadataValue(MetadataHandler.PLUGIN, true));

        balrog.setRemoveWhenFarAway(false);

        NameHandler.customUniqueNameAssigner(balrog, ConfigValues.eventsConfig.getString(EventsConfig.BALROG_NAME));
        AggressiveEliteMobConstructor.constructAggressiveEliteMob(balrog);

        balrogVisualEffectLoop(balrog);

    }

    private static void balrogVisualEffectLoop(Silverfish balrog) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (balrog.isDead() || !balrog.isValid()) {

                    cancel();
                    return;

                }

                balrog.getWorld().spawnParticle(Particle.FLAME, balrog.getLocation(), 2, 0.1, 0.1, 0.1, 0.05);
                balrog.getWorld().spawnParticle(Particle.SMOKE_LARGE, balrog.getLocation(), 4, 0.1, 0.1, 0.1, 0.05);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!event.getEntity().hasMetadata(MetadataHandler.BALROG)) return;

        event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), UniqueItemConstructor.uniqueItems.get(UniqueItemConstructor.GREED));

    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {

        if (!event.getEntity().hasMetadata(MetadataHandler.BALROG)) return;

        spawnTrashMobs((Silverfish) event.getEntity());
        spawnTrashMobs((Silverfish) event.getEntity());

    }

    private static void spawnTrashMobs(Silverfish balrog) {

        Silverfish trashMob = (Silverfish) balrog.getLocation().getWorld().spawnEntity(balrog.getLocation(), EntityType.SILVERFISH);

        trashMob.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, 1));
        trashMob.setMetadata(MetadataHandler.CUSTOM_ARMOR, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        trashMob.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        trashMob.setMetadata(MetadataHandler.CUSTOM_STACK, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        trashMob.setMetadata(MetadataHandler.CUSTOM_HEALTH, new FixedMetadataValue(MetadataHandler.PLUGIN, true));

        trashMob.setMaxHealth(1);
        trashMob.setHealth(1);

        NameHandler.customUniqueNameAssigner(trashMob, ConfigValues.eventsConfig.getString(EventsConfig.BALROG_TRASH_MOB_NAME));

        trashMobVisualEffect(trashMob);

    }

    private static void trashMobVisualEffect(Silverfish raug) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!raug.isValid() || raug.isDead()) {

                    cancel();
                    return;

                }

                raug.getWorld().spawnParticle(Particle.FLAME, raug.getLocation(), 1, 0.1, 0.1, 0.1, 0.05);
                raug.getWorld().spawnParticle(Particle.SMOKE_LARGE, raug.getLocation(), 1, 0.1, 0.1, 0.1, 0.05);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);

    }

}
