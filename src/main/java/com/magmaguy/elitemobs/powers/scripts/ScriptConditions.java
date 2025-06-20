package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptConditionsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ConditionType;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the conditions specified in script blueprints for EliteMobs.
 * This class evaluates whether certain conditions are met before or during the execution of script actions.
 */
public class ScriptConditions {

    private final ScriptConditionsBlueprint conditionsBlueprint;
    private final EliteScript eliteScript;
    private final ScriptTargets scriptTargets;

    public ScriptConditions(ScriptConditionsBlueprint scriptConditionsBlueprint, EliteScript eliteScript, boolean actionCondition) {
        this.conditionsBlueprint = scriptConditionsBlueprint;
        this.eliteScript = eliteScript;

        if (conditionsBlueprint.getScriptTargets() != null) {
            this.scriptTargets = new ScriptTargets(conditionsBlueprint.getScriptTargets(), eliteScript);
        } else {
            this.scriptTargets = null;
        }

        if (conditionsBlueprint.getConditionType() == null) {
            if (actionCondition) {
                conditionsBlueprint.setConditionType(ConditionType.FILTERING);
            } else {
                conditionsBlueprint.setConditionType(ConditionType.BLOCKING);
            }
        }
    }

    /**
     * Generic evaluation helper: compares an actual boolean against an expected value,
     * then optionally inverts based on runIf flag.
     */
    private boolean eval(boolean actual, Boolean expected, Boolean runIf) {
        if (expected == null) return true;
        boolean matches = actual == expected;
        return runIf == null ? matches : (matches == runIf);
    }

    /**
     * Checks if a living entity's alive status matches the condition.
     */
    private boolean isAliveCheck(LivingEntity livingEntity) {
        Boolean expected = conditionsBlueprint.getIsAlive();
        if (expected == null) return true;
        if (livingEntity == null) return false;
        boolean actual = livingEntity.isValid();
        return eval(actual, expected, conditionsBlueprint.getRunIfIsAliveIs());
    }

    /**
     * Checks if a living entity has all the required tags, honoring inversion.
     */
    private boolean hasTagsCheck(LivingEntity target) {
        List<String> tags = conditionsBlueprint.getHasTags();
        if (tags == null) return true;
        boolean actual = tagsUninverted(target, tags);
        return eval(actual, Boolean.TRUE, conditionsBlueprint.getRunIfHasTagIs());
    }

    /**
     * Checks if a living entity does not have any of the disallowed tags, honoring inversion.
     */
    private boolean doesNotHaveTags(LivingEntity target) {
        List<String> tags = conditionsBlueprint.getDoesNotHaveTags();
        if (tags == null) return true;
        boolean actual = tagsUninverted(target, tags);
        return eval(actual, Boolean.FALSE, conditionsBlueprint.getRunIfDoesNotHaveTagIs());
    }

    /**
     * Helper: fetches entity's tags and returns true if all blueprintTags are present.
     */
    private boolean tagsUninverted(LivingEntity target, List<String> blueprintTags) {
        List<String> entityTags = null;
        try {
            if (target instanceof Player player) {
                ElitePlayerInventory inv = ElitePlayerInventory.getPlayer(player);
                if (inv != null) entityTags = new ArrayList<>(inv.getTags());
            } else {
                EliteEntity mob = EntityTracker.getEliteMobEntity(target);
                if (mob != null) entityTags = new ArrayList<>(mob.getTags());
            }
        } catch (Exception e) {
            Logger.warn("Failed to retrieve tags for entity '" + target.getName() + "': " + e.getMessage());
            return false;
        }
        if (entityTags == null) return false;
        return entityTags.containsAll(blueprintTags);
    }

    private boolean targetCountLowerThan(int targetCount) {
        Integer threshold = conditionsBlueprint.getTargetCountLowerThan();
        if (threshold == null) return true;
        boolean actual = targetCount < threshold;
        return eval(actual, Boolean.TRUE, conditionsBlueprint.getRunIfTargetCountLowerThanIs());
    }

    private boolean targetCountGreaterThan(int targetCount) {
        Integer threshold = conditionsBlueprint.getTargetCountGreaterThan();
        if (threshold == null) return true;
        boolean actual = targetCount > threshold;
        return eval(actual, Boolean.TRUE, conditionsBlueprint.getRunIfTargetCountGreaterThanIs());
    }

    private boolean checkRandomizer() {
        Double chance = conditionsBlueprint.getRandomChance();
        if (chance == null) return true;
        boolean actual = ThreadLocalRandom.current().nextDouble() < chance;
        return eval(actual, Boolean.TRUE, conditionsBlueprint.getRunIfRandomChanceIs());
    }

    private boolean isAirCheck(Location targetLocation) {
        Boolean expected = conditionsBlueprint.getLocationIsAir();
        if (expected == null) return true;
        try {
            boolean actual = targetLocation.getBlock().getType().isAir();
            return eval(actual, expected, conditionsBlueprint.getRunIfLocationIsAirIs());
        } catch (Exception e) {
            Logger.warn("Failed to check if location is air at '" + targetLocation + "': " + e.getMessage());
            return false;
        }
    }

    private boolean isOnFloor(Location targetLocation) {
        Boolean expected = conditionsBlueprint.getIsOnFloor();
        if (expected == null) return true;
        try {
            Block current = targetLocation.getBlock();
            Block below = targetLocation.clone().subtract(0, 1, 0).getBlock();
            boolean actual = !current.getType().isSolid() && below.getType().isSolid();
            return eval(actual, expected, conditionsBlueprint.getRunIfIsOnFloorIs());
        } catch (Exception e) {
            Logger.warn("Failed to check if location is on floor at '" + targetLocation + "': " + e.getMessage());
            return false;
        }
    }

    private boolean isStandingOn(Location targetLocation) {
        Material mat = conditionsBlueprint.getIsStandingOnMaterial();
        if (mat == null) return true;
        try {
            boolean actual = targetLocation.clone().subtract(0, 1, 0).getBlock().getType() == mat;
            return eval(actual, Boolean.TRUE, conditionsBlueprint.getRunIfIsStandingOnMaterialIs());
        } catch (Exception e) {
            Logger.warn("Failed to check if location is standing on material at '" + targetLocation + "': " + e.getMessage());
            return false;
        }
    }

    public boolean meetsPreActionConditions(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (scriptTargets == null) return true;
        ScriptActionData data = new ScriptActionData(eliteEntity, directTarget, scriptTargets, null);
        Collection<LivingEntity> entities = scriptTargets.getTargetEntities(data);
        int count = entities.size();

        if (!targetCountLowerThan(count) || !targetCountGreaterThan(count)) return false;
        for (LivingEntity e : entities) if (!checkConditions(e)) return false;
        for (Location loc : scriptTargets.getTargetLocations(data)) if (!checkConditions(loc)) return false;
        return checkRandomizer();
    }

    public boolean meetsActionConditions(ScriptActionData data) {
        if (scriptTargets == null) return true;
        if (data == null) { Logger.warn("ScriptActionData is null in meetsActionConditions."); return false; }

        if (scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.SELF)
                && !isAliveCheck(data.getEliteEntity().getLivingEntity())) {
            return false;
        }
        if (!conditionsBlueprint.getConditionType().equals(ConditionType.BLOCKING)) return true;

        Collection<LivingEntity> entities = scriptTargets.getTargetEntities(data);
        int count = entities.size();
        if (!targetCountLowerThan(count) || !targetCountGreaterThan(count)) return false;
        for (LivingEntity e : entities) if (!checkConditions(e)) return false;
        if (scriptTargets.getTargetLocations(data) != null)
            for (Location loc : scriptTargets.getTargetLocations(data)) if (!checkConditions(loc)) return false;
        return checkRandomizer();
    }

    private boolean checkConditions(LivingEntity livingEntity) {
        if (scriptTargets == null) return true;
        if (livingEntity == null) return false;
        return isAliveCheck(livingEntity)
                && hasTagsCheck(livingEntity)
                && checkConditions(livingEntity.getLocation())
                && doesNotHaveTags(livingEntity);
    }

    private boolean checkConditions(Location location) {
        if (scriptTargets == null) return true;
        if (location == null) return true;
        return isAirCheck(location)
                && isOnFloor(location)
                && isStandingOn(location);
    }

    protected Collection<Location> validateLocations(ScriptActionData data, @NotNull Collection<Location> original) {
        if (scriptTargets == null) {
            return original;
        }
        if (data == null) {
            Logger.warn("ScriptActionData is null in validateLocations.");
            return original;
        }

        Collection<Location> targets = scriptTargets.getTargetLocations(data);

        if (scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.ACTION_TARGET)) {
            original.removeIf(loc -> !checkConditions(loc));
            return original;
        }

        if (targets == null) {
            // no target locations → skip action
            return Collections.emptyList();
        }

        for (Location loc : targets) {
            if (!checkConditions(loc)) {
                return Collections.emptyList();
            }
        }

        return original;
    }

    protected Collection<LivingEntity> validateEntities(ScriptActionData data, @NotNull Collection<LivingEntity> original) {
        if (scriptTargets == null) return original;
        if (data == null) { Logger.warn("ScriptActionData is null in validateEntities."); return original; }
        if (scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.ACTION_TARGET)) {
            original.removeIf(ent -> !checkConditions(ent));
        } else {
            for (LivingEntity e : scriptTargets.getTargetEntities(data)) if (!checkConditions(e)) return new ArrayList<>();
        }
        return original;
    }
}
