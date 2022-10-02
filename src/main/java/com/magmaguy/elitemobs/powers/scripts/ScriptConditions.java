package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptConditionsBlueprint;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ScriptConditions {

    private ScriptConditionsBlueprint conditionsBlueprint;

    public ScriptConditions(ScriptConditionsBlueprint scriptConditionsBlueprint) {
        this.conditionsBlueprint = scriptConditionsBlueprint;
    }


    private boolean bossIsAliveCheck(EliteEntity eliteEntity) {
        if (conditionsBlueprint.getBossIsAlive() == null) return true;
        return eliteEntity.exists() == conditionsBlueprint.getBossIsAlive();
    }

    private boolean isAirCheck(EliteEntity eliteEntity, Location targetLocation) {
        if (conditionsBlueprint.getLocationIsAir() == null) return true;
        return targetLocation.getBlock().getType().isAir();
    }

    public boolean meetsConditions(EliteEntity eliteEntity, LivingEntity directTarget) {
        return bossIsAliveCheck(eliteEntity);
    }

    public boolean meetsConditions(EliteEntity eliteEntity, Location location) {
        if (location == null) return true;
        return isAirCheck(eliteEntity, location);
    }


    //Removes the locations that do not meet the conditions
    protected Collection<Location> validateLocations(@NotNull EliteEntity eliteEntity,
                                                     @NotNull Collection<Location> originalLocations) {
        originalLocations.removeIf(targetLocation -> !meetsConditions(eliteEntity, targetLocation));
        return originalLocations;
    }

    //Removes entities that do not meet the conditions
    protected Collection<? extends LivingEntity> validateEntities(@NotNull EliteEntity eliteEntity,
                                                                  @NotNull Collection<? extends LivingEntity> originalEntities) {

        originalEntities.removeIf(targetEntity -> !meetsConditions(eliteEntity, targetEntity));
        return originalEntities;
    }

}
