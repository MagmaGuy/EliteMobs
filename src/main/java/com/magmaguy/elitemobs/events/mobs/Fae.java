package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.items.UniqueItemConstructor;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class Fae implements Listener {

    public static void spawnFae(Location location) {

        vexSpawnEffect(location);

    }

    private static void vexSpawnEffect(Location location) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > 20 * 3) {

                    initializeVex(location);
                    cancel();
                    return;

                }

                location.getWorld().spawnParticle(Particle.WATER_DROP, location, 5, 0.1, 0.1, 0.1, 1);
                location.getWorld().spawnParticle(Particle.FLAME, location, 3, 0.1, 0.1, 0.1, 0.01);
                location.getWorld().spawnParticle(Particle.SPELL, location, 3, 0.1, 0.1, 0.1, 0.02);

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static void initializeVex(Location location) {

        Vex lightningFae = (Vex) location.getWorld().spawnEntity(location, EntityType.VEX);
        Vex fireFae = (Vex) location.getWorld().spawnEntity(location, EntityType.VEX);
        Vex frostFae = (Vex) location.getWorld().spawnEntity(location, EntityType.VEX);

        fireFae.setMetadata(MetadataHandler.ATTACK_FIRE_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        frostFae.setMetadata(MetadataHandler.ATTACK_FREEZE_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));

        faeConstructor(lightningFae);
        faeConstructor(fireFae);
        faeConstructor(frostFae);

        faeVisualEffect(lightningFae, faeType.LIGHTING_FAE);
        faeVisualEffect(fireFae, faeType.FIRE_FAE);
        faeVisualEffect(frostFae, faeType.FROST_FAE);

        initializeSmiteAttack(lightningFae);

    }

    private static void faeConstructor(Vex fae) {

        fae.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, ConfigValues.eventsConfig.getInt(EventsConfig.FAE_LEVEL)));
        fae.setMetadata(MetadataHandler.FAE, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        fae.setMetadata(MetadataHandler.EVENT_CREATURE, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        fae.setMetadata(MetadataHandler.CUSTOM_ARMOR, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        fae.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        fae.setMetadata(MetadataHandler.CUSTOM_STACK, new FixedMetadataValue(MetadataHandler.PLUGIN, true));

        fae.setRemoveWhenFarAway(false);

        NameHandler.customUniqueNameAssigner(fae, ConfigValues.eventsConfig.getString(EventsConfig.FAE_NAME));
        AggressiveEliteMobConstructor.constructAggressiveEliteMob(fae);

    }

    private enum faeType {

        LIGHTING_FAE,
        FIRE_FAE,
        FROST_FAE

    }

    private static void faeVisualEffect(Vex vex, faeType faeType) {

        Particle particle = Particle.SMOKE_LARGE;

        switch (faeType) {

            case LIGHTING_FAE:
                particle = Particle.SPELL;
                break;
            case FIRE_FAE:
                particle = Particle.FLAME;
                break;
            case FROST_FAE:
                particle = Particle.WATER_DROP;
                break;

        }

        final Particle finalParticle = particle;

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!vex.isValid() || vex.isDead()) {

                    cancel();
                    return;

                }

                switch (finalParticle) {

                    case SPELL:
                        vex.getWorld().spawnParticle(finalParticle, vex.getLocation().add(new Vector(0, 0.25, 0)), 3, 0.1, 0.1, 0.1, 0.02);
                        break;
                    case FLAME:
                        vex.getWorld().spawnParticle(finalParticle, vex.getLocation().add(new Vector(0, 0.25, 0)), 3, 0.1, 0.1, 0.1, 0.01);
                        break;
                    case WATER_DROP:
                        vex.getWorld().spawnParticle(finalParticle, vex.getLocation().add(new Vector(0, 0.25, 0)), 5, 0.1, 0.1, 0.1, 1);
                        break;

                }


            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);

    }

    private static void initializeSmiteAttack(Vex lightingFae) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (lightingFae.isDead() || !lightingFae.isValid()) {

                    cancel();
                    return;

                }

                for (Entity entity : lightingFae.getNearbyEntities(16, 16, 16)) {

                    if (entity instanceof Player) {

                        entity.getWorld().strikeLightning(entity.getLocation());

                    }

                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 20, 20 * 20);

    }

    @EventHandler
    public void onFaeDeath(EntityDeathEvent event) {

        if (!event.getEntity().hasMetadata(MetadataHandler.FAE)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.33) return;

        event.getEntity().getWorld().dropItem(event.getEntity().getLocation(),
                UniqueItemConstructor.uniqueItems.get(UniqueItemConstructor.THE_FELLER));

    }

}
