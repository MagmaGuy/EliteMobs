package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.ActionDynamicBossLevelConstructor;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.BossMobDeathCountdown;
import com.magmaguy.elitemobs.items.UniqueItemConstructor;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;
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

        MetadataHandler.registerMetadata(fireFae, MetadataHandler.ATTACK_FIRE_MD, true);
        MetadataHandler.registerMetadata(frostFae, MetadataHandler.ATTACK_FREEZE_MD, true);

        faeConstructor(lightningFae);
        faeConstructor(fireFae);
        faeConstructor(frostFae);

        faeVisualEffect(lightningFae, faeType.LIGHTING_FAE);
        faeVisualEffect(fireFae, faeType.FIRE_FAE);
        faeVisualEffect(frostFae, faeType.FROST_FAE);

        initializeSmiteAttack(lightningFae);

    }

    private static void faeConstructor(Vex fae) {

        MetadataHandler.registerMetadata(fae, MetadataHandler.ELITE_MOB_MD, ActionDynamicBossLevelConstructor.determineDynamicBossLevel(fae)/3);
        MetadataHandler.registerMetadata(fae, MetadataHandler.FAE, true);
        MetadataHandler.registerMetadata(fae, MetadataHandler.EVENT_CREATURE, true);
        MetadataHandler.registerMetadata(fae, MetadataHandler.CUSTOM_ARMOR, true);
        MetadataHandler.registerMetadata(fae, MetadataHandler.CUSTOM_POWERS_MD, true);
        MetadataHandler.registerMetadata(fae, MetadataHandler.CUSTOM_STACK, true);
        MetadataHandler.registerMetadata(fae, MetadataHandler.PERSISTENT_ENTITY, true);

        fae.setRemoveWhenFarAway(false);

        NameHandler.customUniqueNameAssigner(fae, ConfigValues.eventsConfig.getString(EventsConfig.FAE_NAME));
        AggressiveEliteMobConstructor.constructAggressiveEliteMob(fae);

        BossMobDeathCountdown.startDeathCountdown(fae);

    }

    private enum faeType {

        LIGHTING_FAE,
        FIRE_FAE,
        FROST_FAE

    }

    private static void faeVisualEffect(Vex fae, faeType faeType) {

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

            UUID uuid = fae.getUniqueId();

            @Override
            public void run() {

                Vex localFae = fae;

                if (!localFae.isValid())
                    if (Bukkit.getEntity(uuid) != null && Bukkit.getEntity(uuid).isValid())
                        localFae = (Vex) Bukkit.getEntity(uuid);

                if (localFae.isDead()) {

                    cancel();
                    return;

                }

                switch (finalParticle) {

                    case SPELL:
                        localFae.getWorld().spawnParticle(finalParticle, fae.getLocation().add(new Vector(0, 0.25, 0)), 3, 0.1, 0.1, 0.1, 0.02);
                        break;
                    case FLAME:
                        localFae.getWorld().spawnParticle(finalParticle, fae.getLocation().add(new Vector(0, 0.25, 0)), 3, 0.1, 0.1, 0.1, 0.01);
                        break;
                    case WATER_DROP:
                        localFae.getWorld().spawnParticle(finalParticle, fae.getLocation().add(new Vector(0, 0.25, 0)), 5, 0.1, 0.1, 0.1, 1);
                        break;

                }


            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);

    }

    private static void initializeSmiteAttack(Vex lightingFae) {

        new BukkitRunnable() {

            UUID uuid = lightingFae.getUniqueId();

            @Override
            public void run() {

                Vex localFae = lightingFae;

                if (!lightingFae.isValid())
                    if (Bukkit.getEntity(uuid) != null && Bukkit.getEntity(uuid).isValid())
                        localFae = (Vex) Bukkit.getEntity(uuid);

                if (localFae.isDead()) {

                    cancel();
                    return;

                }

                for (Entity entity : localFae.getNearbyEntities(16, 16, 16)) {

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
