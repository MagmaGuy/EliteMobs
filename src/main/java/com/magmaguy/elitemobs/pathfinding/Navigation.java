package com.magmaguy.elitemobs.pathfinding;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointStartEvent;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.combatsystem.displays.LeashReturnDamageIndicator;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class Navigation implements Listener {

    public static void stopMoving(LivingEntity livingEntity){
        NMSManager.getAdapter().doNotMove(livingEntity);
    }

    private static final HashMap<CustomBossEntity, BukkitTask> currentlyNavigating = new HashMap<>();
    private static final LeashReturnTracker activeLeashReturns = new LeashReturnTracker();

    public static void addSoftLeashAI(RegionalBossEntity regionalBossEntity) {
        if (NMSManager.getAdapter() == null) return;
        if (regionalBossEntity.getUnsyncedLivingEntity() != null &&
                regionalBossEntity.getUnsyncedLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        if (regionalBossEntity.getLivingEntity() instanceof Creature)
            NMSManager.getAdapter().wanderBackToPoint(
                            regionalBossEntity.getLivingEntity(),
                            regionalBossEntity.getSpawnLocation(),
                            regionalBossEntity.getLeashRadius() / 2D,
                            20 * 5)
                    .setSpeed(1.2f)
                    .setStopReturnDistance(1)
                    .setGoalRefreshCooldownTicks(20 * 3)
                    .setHardObjective(false)
                    .setReturnDuringCombat(true)
                    .setTeleportOnFail(true)
                    .setStartWithCooldown(true)
                    .register();
    }

    public static void addHardLeashAI(RegionalBossEntity regionalBossEntity) {
        if (NMSManager.getAdapter() == null) return;
        if (regionalBossEntity.getUnsyncedLivingEntity() != null &&
                regionalBossEntity.getUnsyncedLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        NMSManager.getAdapter().wanderBackToPoint(
                        regionalBossEntity.getLivingEntity(),
                        regionalBossEntity.getSpawnLocation(),
                        regionalBossEntity.getLeashRadius(),
                        20 * 5)
                .setSpeed(2f)
                .setStopReturnDistance(0)
                .setGoalRefreshCooldownTicks(20 * 3)
                .setHardObjective(true)
                .setTeleportOnFail(true)
                .setStartWithCooldown(true)
                .register();
    }

    public static void shutdown() {
        currentlyNavigating.values().forEach(BukkitTask::cancel);
        currentlyNavigating.clear();
        activeLeashReturns.clear();
        LeashReturnDamageIndicator.shutdown();
    }

    public static void navigateTo(CustomBossEntity customBossEntity, Double speed, Location destination, boolean force, int duration) {
        if (duration == 0) duration = 20 * 5;
        if (customBossEntity.getLivingEntity() == null) return;
        if (destination == null || destination.getWorld() == null) return;
        if (speed == null)
            speed = AttributeManager.getAttributeBaseValue(customBossEntity.getLivingEntity(), "generic_movement_speed");
        Double finalSpeed = speed;
        if (currentlyNavigating.get(customBossEntity) != null) currentlyNavigating.get(customBossEntity).cancel();
        int finalDuration = duration;
        currentlyNavigating.put(customBossEntity, new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter >= finalDuration ||
                        !customBossEntity.exists() ||
                        customBossEntity.getLivingEntity() == null ||
                        !customBossEntity.getLivingEntity().getWorld().equals(destination.getWorld()) ||
                        customBossEntity.getLivingEntity() != null && customBossEntity.getLivingEntity().getLocation().distanceSquared(destination) < Math.pow(1, 2)) {
                    if (customBossEntity.exists() && counter >= finalDuration && force) {
                        customBossEntity.getLivingEntity().teleport(destination);
                    }
                    cancel();
                    currentlyNavigating.remove(customBossEntity);
                    return;
                }
                NMSManager.getAdapter().move(customBossEntity.getLivingEntity(), finalSpeed.floatValue(), destination);
                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void beginLeashReturn(WanderBackToPointStartEvent event) {
        if (event.getLivingEntity() == null) return;
        if (event.getLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;

        activeLeashReturns.begin(event.getLivingEntity().getUniqueId(), event.isHardObjective());

        if (!event.isHardObjective()) return;
        AttributeManager.setAttribute(event.getLivingEntity(), "generic_follow_range", regionalBossEntity.getCustomBossesConfigFields().getLeashRadius() * 1.5);
    }

    @EventHandler(ignoreCancelled = true)
    public void endLeashReturn(WanderBackToPointEndEvent event) {
        if (event.getLivingEntity() == null) return;
        activeLeashReturns.end(event.getLivingEntity().getUniqueId(), event.isHardObjective());

        if (event.getLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;

        if (event.isHardObjective()) {
            if (regionalBossEntity.getCustomBossesConfigFields().getFollowDistance() != 0)
                AttributeManager.setAttribute(event.getLivingEntity(), "generic_follow_range", regionalBossEntity.getCustomBossesConfigFields().getFollowDistance());
            else
                AttributeManager.setAttribute(event.getLivingEntity(), "generic_follow_range", AttributeManager.getAttributeDefaultValue(regionalBossEntity.getLivingEntity(), "generic_follow_range"));
        }

        // Walking home and teleporting home are both leash resets. In either case,
        // restore the boss just as the legacy hard-leash return did.
        regionalBossEntity.fullHeal();
    }

    /**
     * Cancels damage at the Bukkit event boundary instead of toggling the entity's
     * invulnerable flag. This keeps leash immunity independent from powers and other
     * systems which also own that flag, and prevents downstream EliteMobs combat
     * listeners from applying side effects for an immune hit.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void protectReturningBoss(EntityDamageEvent event) {
        if (activeLeashReturns.isEmpty()) return;
        if (!activeLeashReturns.isReturning(event.getEntity().getUniqueId())) return;

        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) {
            activeLeashReturns.clear(event.getEntity().getUniqueId());
            return;
        }

        event.setCancelled(true);

        if (!(event instanceof EntityDamageByEntityEvent damageByEntityEvent)) return;
        LivingEntity realDamager = EntityFinder.filterRangedDamagers(damageByEntityEvent.getDamager());
        if (realDamager instanceof Player player)
            LeashReturnDamageIndicator.show(regionalBossEntity, player);
    }

    @EventHandler
    public void clearLeashReturnOnDeath(EntityDeathEvent event) {
        activeLeashReturns.clear(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void clearLeashReturnOnRemoval(EliteMobRemoveEvent event) {
        if (event.getEntity() != null)
            activeLeashReturns.clear(event.getEntity().getUniqueId());
    }
}
