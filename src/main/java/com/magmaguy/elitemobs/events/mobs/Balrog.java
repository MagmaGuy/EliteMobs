package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.ActionDynamicBossLevelConstructor;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.BossMobDeathCountdown;
import com.magmaguy.elitemobs.items.uniqueitems.DwarvenGreed;
import com.magmaguy.elitemobs.mobconstructor.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.mobconstructor.NameHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

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
        MetadataHandler.registerMetadata(balrog, MetadataHandler.ELITE_MOB_MD, ActionDynamicBossLevelConstructor.determineDynamicBossLevel(balrog));
        MetadataHandler.registerMetadata(balrog, MetadataHandler.BALROG, true);
        MetadataHandler.registerMetadata(balrog, MetadataHandler.EVENT_CREATURE, true);
        MetadataHandler.registerMetadata(balrog, MetadataHandler.CUSTOM_ARMOR, true);
        MetadataHandler.registerMetadata(balrog, MetadataHandler.CUSTOM_POWERS_MD, true);
        MetadataHandler.registerMetadata(balrog, MetadataHandler.CUSTOM_STACK, true);
        MetadataHandler.registerMetadata(balrog, MetadataHandler.PERSISTENT_ENTITY, true);

        NameHandler.customUniqueNameAssigner(balrog, ConfigValues.eventsConfig.getString(EventsConfig.BALROG_NAME));
        AggressiveEliteMobConstructor.constructAggressiveEliteMob(balrog);

        balrogVisualEffectLoop(balrog);

        BossMobDeathCountdown.startDeathCountdown(balrog);

    }

    private static void balrogVisualEffectLoop(Silverfish balrog) {

        new BukkitRunnable() {

            UUID uuid = balrog.getUniqueId();

            @Override
            public void run() {

                Silverfish localBalrog = balrog;

                if (!balrog.isValid())
                    if (Bukkit.getEntity(uuid) != null)
                        localBalrog = (Silverfish) Bukkit.getEntity(uuid);

                if (balrog.isDead()) {

                    cancel();
                    return;

                }

                localBalrog.getWorld().spawnParticle(Particle.FLAME, localBalrog.getLocation(), 2, 0.1, 0.1, 0.1, 0.05);
                localBalrog.getWorld().spawnParticle(Particle.SMOKE_LARGE, localBalrog.getLocation(), 4, 0.1, 0.1, 0.1, 0.05);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!event.getEntity().hasMetadata(MetadataHandler.BALROG)) return;

        DwarvenGreed dwarvenGreed = new DwarvenGreed();
        ItemStack dwarvenGreedItemStack = dwarvenGreed.constructItemStack();

        event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), dwarvenGreedItemStack);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageEvent event) {

        if (event.isCancelled()) return;
        if (!event.getEntity().hasMetadata(MetadataHandler.BALROG)) return;
        if (event.getFinalDamage() < 2) return;

        spawnTrashMobs((Silverfish) event.getEntity());
        spawnTrashMobs((Silverfish) event.getEntity());

    }

    private static void spawnTrashMobs(Silverfish balrog) {

        Silverfish trashMob = (Silverfish) balrog.getLocation().getWorld().spawnEntity(balrog.getLocation(), EntityType.SILVERFISH);

        MetadataHandler.registerMetadata(trashMob, MetadataHandler.ELITE_MOB_MD, 1);
        MetadataHandler.registerMetadata(trashMob, MetadataHandler.CUSTOM_ARMOR, true);
        MetadataHandler.registerMetadata(trashMob, MetadataHandler.CUSTOM_POWERS_MD, true);
        MetadataHandler.registerMetadata(trashMob, MetadataHandler.CUSTOM_STACK, true);
        MetadataHandler.registerMetadata(trashMob, MetadataHandler.CUSTOM_HEALTH, true);

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
