package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptConditionsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ConditionType;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the conditions specified in script blueprints for EliteMobs.
 * This class evaluates whether certain conditions are met before or during the execution of script actions.
 */
public class ScriptConditions {

    /** The blueprint containing the conditions to be checked. */
    private final ScriptConditionsBlueprint conditionsBlueprint;

    /** The EliteScript associated with these conditions. */
    private final EliteScript eliteScript;

    /** The targets specified in the conditions, if any. */
    private final ScriptTargets scriptTargets;

    /**
     * Constructs a new ScriptConditions instance.
     *
     * @param scriptConditionsBlueprint The blueprint defining the conditions.
     * @param eliteScript               The script associated with these conditions.
     * @param actionCondition           True if the condition is for an action; false otherwise.
     */
    public ScriptConditions(ScriptConditionsBlueprint scriptConditionsBlueprint, EliteScript eliteScript, boolean actionCondition) {
        this.conditionsBlueprint = scriptConditionsBlueprint;
        this.eliteScript = eliteScript;

        // Initialize script targets if conditions are set
        if (conditionsBlueprint.getScriptTargets() != null) {
            this.scriptTargets = new ScriptTargets(conditionsBlueprint.getScriptTargets(), eliteScript);
        } else {
            this.scriptTargets = null;
        }

        // Set the default condition type if not specified
        if (conditionsBlueprint.getConditionType() == null) {
            if (actionCondition) {
                conditionsBlueprint.setConditionType(ConditionType.FILTERING);
            } else {
                conditionsBlueprint.setConditionType(ConditionType.BLOCKING);
            }
        }
    }

    /**
     * Checks if a living entity's alive status matches the condition.
     *
     * @param livingEntity The living entity to check.
     * @return True if the condition is met; false otherwise.
     */
    private boolean isAliveCheck(LivingEntity livingEntity) {
        if (conditionsBlueprint.getIsAlive() == null) return true;
        if (livingEntity == null) return false;
        return livingEntity.isValid() == conditionsBlueprint.getIsAlive();
    }

    /**
     * Checks if a living entity has all the required tags.
     *
     * @param target The living entity to check.
     * @return True if the entity has all the required tags; false otherwise.
     */
    private boolean hasTagsCheck(LivingEntity target) {
        if (conditionsBlueprint.getHasTags() == null) return true;
        return checkTags(target, conditionsBlueprint.getHasTags());
    }

    /**
     * Checks if a living entity does not have any of the disallowed tags.
     *
     * @param target The living entity to check.
     * @return True if the entity does not have any disallowed tags; false otherwise.
     */
    private boolean doesNotHaveTags(LivingEntity target) {
        if (conditionsBlueprint.getDoesNotHaveTags() == null) return true;
        return !checkTags(target, conditionsBlueprint.getDoesNotHaveTags());
    }

    /**
     * Checks if a random chance condition is met.
     *
     * @return True if the random chance condition is met; false otherwise.
     */
    private boolean checkRandomizer() {
        if (conditionsBlueprint.getRandomChance() == null) return true;
        double random = ThreadLocalRandom.current().nextDouble();
        return random < conditionsBlueprint.getRandomChance();
    }

    /**
     * Checks if a living entity has the specified tags.
     *
     * @param target        The living entity to check.
     * @param blueprintTags The list of tags to check for.
     * @return True if the entity has all the specified tags; false otherwise.
     */
    private boolean checkTags(LivingEntity target, List<String> blueprintTags) {
        List<String> entityTags = null;

        try {
            if (target instanceof Player player) {
                ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                if (playerInventory != null) {
                    entityTags = new ArrayList<>(playerInventory.getTags());
                }
            } else {
                EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
                if (eliteEntity != null) {
                    entityTags = new ArrayList<>(eliteEntity.getTags());
                }
            }
        } catch (Exception e) {
            Logger.warn("Failed to retrieve tags for entity '" + target.getName() + "': " + e.getMessage());
            return false;
        }

        if (entityTags == null) return false;

        return entityTags.containsAll(blueprintTags);
    }

    /**
     * Checks if a location is air or not, based on the condition.
     *
     * @param targetLocation The location to check.
     * @return True if the condition is met; false otherwise.
     */
    private boolean isAirCheck(Location targetLocation) {
        if (conditionsBlueprint.getLocationIsAir() == null) return true;
        try {
            boolean isAir = targetLocation.getBlock().getType().isAir();
            return conditionsBlueprint.getLocationIsAir() == isAir;
        } catch (Exception e) {
            Logger.warn("Failed to check if location is air at '" + targetLocation + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a location is on the floor (i.e., not in the air), based on the condition.
     *
     * @param targetLocation The location to check.
     * @return True if the condition is met; false otherwise.
     */
    private boolean isOnFloor(Location targetLocation) {
        if (conditionsBlueprint.getIsOnFloor() == null) return true;
        try {
            Block currentBlock = targetLocation.getBlock();
            Block floorBlock = targetLocation.clone().subtract(0, 1, 0).getBlock();
            boolean isOnFloor = !currentBlock.getType().isSolid() && floorBlock.getType().isSolid();
            return conditionsBlueprint.getIsOnFloor() == isOnFloor;
        } catch (Exception e) {
            Logger.warn("Failed to check if location is on floor at '" + targetLocation + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a location is standing on a specific material.
     *
     * @param targetLocation The location to check.
     * @return True if the condition is met; false otherwise.
     */
    private boolean isStandingOn(Location targetLocation) {
        if (conditionsBlueprint.getIsStandingOnMaterial() == null) return true;
        try {
            Block floorBlock = targetLocation.clone().subtract(0, 1, 0).getBlock();
            return floorBlock.getType() == conditionsBlueprint.getIsStandingOnMaterial();
        } catch (Exception e) {
            Logger.warn("Failed to check if location is standing on material at '" + targetLocation + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Determines if pre-action conditions are met for a script.
     *
     * @param eliteEntity  The EliteEntity executing the script.
     * @param directTarget The direct target from the event that triggered the script.
     * @return True if all pre-action conditions are met; false otherwise.
     */
    public boolean meetsPreActionConditions(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (scriptTargets == null) return true;
        ScriptActionData scriptActionData = new ScriptActionData(eliteEntity, directTarget, scriptTargets, null);

        for (LivingEntity livingEntity : scriptTargets.getTargetEntities(scriptActionData)) {
            if (!checkConditions(livingEntity)) return false;
        }

        for (Location location : scriptTargets.getTargetLocations(scriptActionData)) {
            if (!checkConditions(location)) return false;
        }

        return checkRandomizer();
    }

    /**
     * Determines if action conditions are met during the execution of a script action.
     * This method only cares about blocking conditions; the action is either entirely valid or not at all.
     *
     * @param scriptActionData The data for the current script action.
     * @return True if all action conditions are met; false otherwise.
     */
    public boolean meetsActionConditions(ScriptActionData scriptActionData) {
        if (scriptTargets == null) return true;
        if (scriptActionData == null) {
            Logger.warn("ScriptActionData is null in meetsActionConditions.");
            return false;
        }

        // Special case: if the target is SELF and the condition isAlive is not met
        if (scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.SELF) &&
                !isAliveCheck(scriptActionData.getEliteEntity().getLivingEntity())) {
            return false;
        }

        // Only proceed if the condition type is BLOCKING
        if (!conditionsBlueprint.getConditionType().equals(ConditionType.BLOCKING)) return true;

        for (LivingEntity livingEntity : scriptTargets.getTargetEntities(scriptActionData)) {
            if (!checkConditions(livingEntity)) return false;
        }

        for (Location location : scriptTargets.getTargetLocations(scriptActionData)) {
            if (!checkConditions(location)) return false;
        }

        return checkRandomizer();
    }

    /**
     * Checks all conditions for a living entity.
     *
     * @param livingEntity The living entity to check.
     * @return True if all conditions are met; false otherwise.
     */
    private boolean checkConditions(LivingEntity livingEntity) {
        if (scriptTargets == null) return true;
        if (livingEntity == null) return false;

        if (!isAliveCheck(livingEntity)) return false;
        if (!hasTagsCheck(livingEntity)) return false;
        if (!checkConditions(livingEntity.getLocation())) return false;
        if (!doesNotHaveTags(livingEntity)) return false;

        return true;
    }

    /**
     * Checks all conditions for a location.
     *
     * @param location The location to check.
     * @return True if all conditions are met; false otherwise.
     */
    private boolean checkConditions(Location location) {
        if (scriptTargets == null) return true;
        if (location == null) return true;

        return isAirCheck(location) &&
                isOnFloor(location) &&
                isStandingOn(location);
    }

    /**
     * Validates and filters locations based on the conditions.
     * Removes locations that do not meet the conditions.
     *
     * @param scriptActionData  The data for the current script action.
     * @param originalLocations The original collection of locations.
     * @return A collection of locations that meet the conditions.
     */
    protected Collection<Location> validateLocations(ScriptActionData scriptActionData,
                                                     @NotNull Collection<Location> originalLocations) {
        if (scriptTargets == null) return originalLocations;
        if (scriptActionData == null) {
            Logger.warn("ScriptActionData is null in validateLocations.");
            return originalLocations;
        }

        if (scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.ACTION_TARGET)) {
            // Exclude non-conforming locations, but the script will still run
            originalLocations.removeIf(targetLocation -> !checkConditions(targetLocation));
        } else {
            // If the condition has a different target, cancel all effects if conditions are not met
            Collection<Location> conditionTargetLocations = scriptTargets.getTargetLocations(scriptActionData);
            for (Location location : conditionTargetLocations) {
                if (!checkConditions(location)) return new ArrayList<>();
            }
        }

        return originalLocations;
    }

    /**
     * Validates and filters entities based on the conditions.
     * Removes entities that do not meet the conditions.
     *
     * @param scriptActionData  The data for the current script action.
     * @param originalEntities  The original collection of entities.
     * @return A collection of entities that meet the conditions.
     */
    protected Collection<LivingEntity> validateEntities(ScriptActionData scriptActionData,
                                                        @NotNull Collection<LivingEntity> originalEntities) {
        if (scriptTargets == null) return originalEntities;
        if (scriptActionData == null) {
            Logger.warn("ScriptActionData is null in validateEntities.");
            return originalEntities;
        }

        if (scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.ACTION_TARGET)) {
            // Exclude non-conforming entities, but the script will still run
            originalEntities.removeIf(targetEntity -> !checkConditions(targetEntity));
        } else {
            // If the condition has a different target, cancel all effects if conditions are not met
            Collection<LivingEntity> conditionTargetEntities = scriptTargets.getTargetEntities(scriptActionData);
            for (LivingEntity livingEntity : conditionTargetEntities) {
                if (!checkConditions(livingEntity)) return new ArrayList<>();
            }
        }

        return originalEntities;
    }
}
