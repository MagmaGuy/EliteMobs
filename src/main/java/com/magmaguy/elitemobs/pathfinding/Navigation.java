package com.magmaguy.elitemobs.pathfinding;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointStartEvent;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class Navigation implements Listener {

    public static void stopMoving(LivingEntity livingEntity){
        NMSManager.getAdapter().doNotMove(livingEntity);
    }

    private static final HashMap<CustomBossEntity, BukkitTask> currentlyNavigating = new HashMap();

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
    }

    public static void navigateTo(CustomBossEntity customBossEntity, Double speed, Location destination, boolean force, int duration) {
        if (duration == 0) duration = 20 * 5;
        if (customBossEntity.getLivingEntity() == null) return;
        if (destination == null || destination.getWorld() == null) return;
        if (speed == null)
            speed = customBossEntity.getLivingEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
        Double finalSpeed = speed;
        if (currentlyNavigating.get(customBossEntity) != null) currentlyNavigating.get(customBossEntity).cancel();
        int finalDuration = duration;
        currentlyNavigating.put(customBossEntity, new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter >= finalDuration ||
                        !customBossEntity.exists() ||
                        customBossEntity.getLivingEntity().getLocation().distanceSquared(destination) < Math.pow(1, 2)) {
                    if (counter >= finalDuration && force) {
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

    @EventHandler(ignoreCancelled = true)
    public void makeReturningBossesInvulnerable(WanderBackToPointStartEvent event) {
        if (!event.isHardObjective()) return;
        if (event.getLivingEntity() == null) return;
        if (event.getLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;
        event.getLivingEntity().setInvulnerable(true);
        event.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(regionalBossEntity.getCustomBossesConfigFields().getLeashRadius() * 1.5);
    }

    @EventHandler(ignoreCancelled = true)
    public void makeReturnedBossesVulnerable(WanderBackToPointEndEvent event) {
        if (!event.isHardObjective()) return;
        if (event.getLivingEntity() == null) return;
        if (event.getLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;
        event.getLivingEntity().setInvulnerable(false);

        if (regionalBossEntity.getCustomBossesConfigFields().getFollowDistance() != 0)
            eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(regionalBossEntity.getCustomBossesConfigFields().getFollowDistance());
        else
            eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getDefaultValue());
        regionalBossEntity.fullHeal();
    }
}
