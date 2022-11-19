package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptConditionsBlueprint;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ScriptConditions {

    private ScriptConditionsBlueprint conditionsBlueprint;

    public ScriptConditions(ScriptConditionsBlueprint scriptConditionsBlueprint) {
        this.conditionsBlueprint = scriptConditionsBlueprint;
    }

    private boolean isAliveCheck(LivingEntity livingEntity) {
        if (conditionsBlueprint.getIsAlive() == null) return true;
        return livingEntity.isValid() == conditionsBlueprint.getIsAlive();
    }

    private boolean hasTagsCheck(LivingEntity target) {
        if (conditionsBlueprint.getHasTags() == null) return true;
        return checkTags(target, conditionsBlueprint.getHasTags());
    }

    private boolean doesNotHaveTags(LivingEntity target) {
        if (conditionsBlueprint.getDoesNotHaveTags() == null) return true;
        return checkTags(target, conditionsBlueprint.getDoesNotHaveTags());
    }

    private boolean checkTags(LivingEntity target, List<String> blueprintTags) {
        List<String> entityTags = null;
        if (target instanceof Player player && ElitePlayerInventory.getPlayer(player) != null)
            entityTags = ElitePlayerInventory.getPlayer(player).getTags().stream().toList();
        else {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
            if (eliteEntity != null)
                entityTags = eliteEntity.getTags().stream().toList();
        }

        if (entityTags == null) return false;

        for (String tag : blueprintTags)
            if (!entityTags.contains(tag)) return false;
        return true;
    }

    private boolean isAirCheck(Location targetLocation) {
        if (conditionsBlueprint.getLocationIsAir() == null) return true;
        return conditionsBlueprint.getLocationIsAir() == targetLocation.getBlock().getType().isAir();
    }

    private boolean isOnFloor(Location targetLocation) {
        if (conditionsBlueprint.getIsOnFloor() == null) return true;
        Block currentBlock = targetLocation.getBlock();
        Block floorBlock = targetLocation.clone().subtract(0, 1, 0).getBlock();
        return conditionsBlueprint.getIsOnFloor() == !currentBlock.getType().isSolid() && floorBlock.getType().isSolid();
    }

    public boolean meetsConditions(EliteEntity eliteEntity, LivingEntity directTarget) {
        LivingEntity conditionTarget;
        if (conditionsBlueprint.getConditionTarget() == ScriptConditionsBlueprint.ConditionTarget.SELF)
            conditionTarget = eliteEntity.getUnsyncedLivingEntity();
        else
            conditionTarget = directTarget;
        if (conditionTarget == null) return true;
        if (!isAliveCheck(conditionTarget)) return false;
        if (!hasTagsCheck(conditionTarget)) return false;
        if (!doesNotHaveTags(conditionTarget)) return false;
        return true;
    }

    public boolean meetsConditions(Location location) {
        if (location == null) return true;
        return isAirCheck(location) &&
                isOnFloor(location);
    }


    //Removes the locations that do not meet the conditions
    protected Collection<Location> validateLocations(@NotNull EliteEntity eliteEntity,
                                                     @NotNull Collection<Location> originalLocations) {
        originalLocations.removeIf(targetLocation -> !meetsConditions(targetLocation));
        return originalLocations;
    }

    //Removes entities that do not meet the conditions
    protected Collection<? extends LivingEntity> validateEntities(@NotNull EliteEntity eliteEntity,
                                                                  @NotNull Collection<? extends LivingEntity> originalEntities) {

        originalEntities.removeIf(targetEntity -> !meetsConditions(eliteEntity, targetEntity));
        return originalEntities;
    }

}
