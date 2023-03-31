package com.magmaguy.elitemobs.pathfinding;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointStartEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Navigation implements Listener {
    public static void navigate(Mob mob, Location destination, int speed) {
        NMSManager.getAdapter().move(mob, speed, destination);
    }

    public static void backToSpawn(RegionalBossEntity regionalBossEntity) {
        getThere((Mob) regionalBossEntity.getLivingEntity(), regionalBossEntity.getSpawnLocation().clone(), false);
    }

    public static void addSoftLeashAI(RegionalBossEntity regionalBossEntity) {
        if (regionalBossEntity.getLivingEntity() instanceof Creature)
            NMSManager.getAdapter().returnToPointWhenOutOfCombatBehavior(
                    regionalBossEntity.getLivingEntity(),
                    1.2f,
                    regionalBossEntity.getSpawnLocation(),
                    regionalBossEntity.getCustomBossesConfigFields().getLeashRadius() / 2D,
                    1,
                    20 * 5,
                    20 * 3,
                    false,
                    true);
    }

    public static void addHardLeashAI(RegionalBossEntity regionalBossEntity) {
            NMSManager.getAdapter().returnToPointWhenOutOfCombatBehavior(
                regionalBossEntity.getLivingEntity(),
                2f,
                regionalBossEntity.getSpawnLocation(),
                regionalBossEntity.getCustomBossesConfigFields().getLeashRadius(),
                0,
                20 * 5,
                20 * 3,
                true,
                true);
    }

    private static void getThere(Mob livingEntity, Location location, boolean force) {
        if (livingEntity == null || location == null) return;
    }

    @EventHandler(ignoreCancelled = true)
    public void makeReturningBossesInvulnerable(WanderBackToPointStartEvent event) {
        //if (!event.isHardObjective()) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;
        event.getLivingEntity().setInvulnerable(true);
        eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(regionalBossEntity.getCustomBossesConfigFields().getLeashRadius() * 1.5);
    }

    @EventHandler(ignoreCancelled = true)
    public void makeReturnedBossesVulnerable(WanderBackToPointEndEvent event) {
        //if (!event.isHardObjective()) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;
        event.getLivingEntity().setInvulnerable(false);

        if (regionalBossEntity.getCustomBossesConfigFields().getFollowDistance() != 0)
            eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(regionalBossEntity.getCustomBossesConfigFields().getFollowDistance());
        else
            eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getDefaultValue());
        regionalBossEntity.fullHeal();
    }
}
