package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptConditionsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.Target;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScriptConditions {

    private ScriptConditionsBlueprint conditionsBlueprint;
    private ScriptTargets scriptTargets = null;

    public ScriptConditions(ScriptConditionsBlueprint scriptConditionsBlueprint, EliteScript eliteScript) {
        this.conditionsBlueprint = scriptConditionsBlueprint;
        //This is null if no conditions are set
        if (conditionsBlueprint.getScriptTargets() != null)
            this.scriptTargets = new ScriptTargets(conditionsBlueprint.getScriptTargets(), eliteScript);
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

    public boolean meetsConditionsOutsideOfAction(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (scriptTargets == null) return true;
        ScriptActionData scriptActionData = new ScriptActionData(eliteEntity, directTarget);

        for (LivingEntity livingEntity : scriptTargets.getTargetEntities(scriptActionData))
            if (!meetsConditions(livingEntity)) return false;

        for (Location location : scriptTargets.getTargetLocations(scriptActionData))
            if (!meetsConditions(location)) return false;

        return true;
    }

    public boolean meetsConditions(LivingEntity livingEntity) {
        if (scriptTargets == null) return true;

        if (livingEntity == null) return false;
        if (!isAliveCheck(livingEntity)) return false;
        if (!hasTagsCheck(livingEntity)) return false;
        if (!doesNotHaveTags(livingEntity)) return false;
        return true;
    }

    public boolean meetsConditions(Location location) {
        if (scriptTargets == null) return true;

        if (location == null) return true;
        return isAirCheck(location) &&
                isOnFloor(location);
    }


    //Removes the locations that do not meet the conditions
    protected Collection<Location> validateLocations(ScriptActionData scriptActionData,
                                                     @NotNull Collection<Location> originalLocations) {
        if (scriptTargets == null) return originalLocations;

        if (scriptTargets.getTargetBlueprint().getTargetType().equals(Target.ACTION_TARGET)) {
            //Specific case for when the locations to be validated are the ones from the action target.
            //Note that non-conforming locations are excluded, but the script will still run.
            originalLocations.removeIf(targetLocation -> !meetsConditions(targetLocation));
        } else {
            //If the condition has a target other than the one inherited from ACTION_TARGET, it cancels all effects.
            Collection<Location> conditionTargetLocations = scriptTargets.getTargetLocations(scriptActionData);
            for (Location location : conditionTargetLocations)
                if (!meetsConditions(location)) return new ArrayList<>();
        }

        return originalLocations;
    }

    //Removes entities that do not meet the conditions
    protected Collection<? extends LivingEntity> validateEntities(ScriptActionData scriptActionData,
                                                                  @NotNull Collection<? extends LivingEntity> originalEntities) {
        if (scriptTargets == null) return originalEntities;

        if (scriptTargets.getTargetBlueprint().getTargetType().equals(Target.ACTION_TARGET)) {
            //Specific case for when the entities to be validated are the ones from the action target.
            //Note that non-conforming entities are excluded, but the script will still run.
            originalEntities.removeIf(targetEntity -> !meetsConditions(targetEntity));
        } else {
            //If the condition has a target other than the one inherited from ACTION_TARGET, it cancels all effects.
            Collection<? extends LivingEntity> conditionTargetLocations = scriptTargets.getTargetEntities(scriptActionData);
            for (LivingEntity livingEntity : conditionTargetLocations)
                if (!meetsConditions(livingEntity)) return new ArrayList<>();
        }

        return originalEntities;
    }

}
