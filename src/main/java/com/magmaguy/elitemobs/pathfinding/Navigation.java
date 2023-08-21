package com.magmaguy.elitemobs.pathfinding;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointStartEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Navigation implements Listener {

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

    @EventHandler(ignoreCancelled = true)
    public void makeReturningBossesInvulnerable(WanderBackToPointStartEvent event) {
        if (!event.isHardObjective()) return;
        if (event.getLivingEntity() == null) return;
        if (event.getLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;
        event.getLivingEntity().setInvulnerable(true);
        eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(regionalBossEntity.getCustomBossesConfigFields().getLeashRadius() * 1.5);
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
