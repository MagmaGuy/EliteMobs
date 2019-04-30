package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.ActionDynamicBossLevelConstructor;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.BossMobDeathCountdown;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.items.uniqueitems.DwarvenGreed;
import com.magmaguy.elitemobs.mobconstructor.ActionBossMobEntity;
import com.magmaguy.elitemobs.mobconstructor.TrashMobEntity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;

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

        int eliteLevel = ActionDynamicBossLevelConstructor.determineDynamicBossLevel(location);
        ActionBossMobEntity bossMobEntity = new ActionBossMobEntity(EntityType.SILVERFISH, location, eliteLevel, ConfigValues.eventsConfig.getString(EventsConfig.BALROG_NAME), CreatureSpawnEvent.SpawnReason.NATURAL);
        balrogList.add(bossMobEntity.getLivingEntity());

        balrogVisualEffectLoop((Silverfish) bossMobEntity.getLivingEntity());

        BossMobDeathCountdown.startDeathCountdown(bossMobEntity.getLivingEntity());

    }

    private static void balrogVisualEffectLoop(Silverfish balrog) {

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!balrog.isValid()) {

                    cancel();
                    return;

                }

                balrog.getWorld().spawnParticle(Particle.FLAME, balrog.getLocation(), 2, 0.1, 0.1, 0.1, 0.05);
                balrog.getWorld().spawnParticle(Particle.SMOKE_LARGE, balrog.getLocation(), 4, 0.1, 0.1, 0.1, 0.05);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static HashSet<Entity> balrogList = new HashSet<>();

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!balrogList.contains(event.getEntity())) return;

        balrogList.remove(event.getEntity());

        if (!ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_GREED)) return;

        DwarvenGreed dwarvenGreed = new DwarvenGreed();
        ItemStack dwarvenGreedItemStack = dwarvenGreed.constructItemStack((int) MobTierCalculator.findMobTier(EntityTracker.getEliteMobEntity(event.getEntity()).getLevel()));

        event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), dwarvenGreedItemStack);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageEvent event) {

        if (event.isCancelled()) return;
        if (!balrogList.contains(event.getEntity())) return;
        if (event.getFinalDamage() < 2) return;

        spawnTrashMobs((Silverfish) event.getEntity());
        spawnTrashMobs((Silverfish) event.getEntity());

    }

    private static void spawnTrashMobs(Silverfish balrog) {

        TrashMobEntity trashMobEntity = new TrashMobEntity(EntityType.SILVERFISH, balrog.getLocation(),
                ChatColorConverter.convert(ConfigValues.eventsConfig.getString(EventsConfig.BALROG_TRASH_MOB_NAME)));

        trashMobVisualEffect((Silverfish) trashMobEntity.getLivingEntity());

    }

    private static void trashMobVisualEffect(Silverfish raug) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!raug.isValid() || raug.isDead()) {
                    cancel();
                    return;
                }

                raug.getWorld().spawnParticle(Particle.SMOKE_LARGE, raug.getLocation(), 1, 0.1, 0.1, 0.1, 0.05);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);

    }

}
