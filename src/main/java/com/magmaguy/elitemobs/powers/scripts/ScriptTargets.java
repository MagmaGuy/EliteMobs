package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.Target;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ScriptTargets {

    @Getter
    private final ScriptTargetsBlueprint targetBlueprint;
    private final EliteScript eliteScript;
    private Collection<? extends LivingEntity> livingEntities = null;

    public ScriptTargets(ScriptTargetsBlueprint targetBlueprint, EliteScript eliteScript) {
        this.targetBlueprint = targetBlueprint;
        this.eliteScript = eliteScript;
    }

    //Parse all string-based configuration locations
    public static Location processLocationFromString(EliteEntity eliteEntity,
                                                     String locationString,
                                                     String scriptName,
                                                     String filename,
                                                     Vector offset) {
        if (locationString == null) {
            new WarningMessage("Failed to get location target in script " + scriptName + " in " + filename);
            return null;
        }
        Location parsedLocation = ConfigurationLocation.serialize(locationString);
        if (parsedLocation.getWorld() == null && locationString.split(",")[0].equalsIgnoreCase("same_as_boss"))
            parsedLocation.setWorld(eliteEntity.getLocation().getWorld());
        parsedLocation.add(offset);
        return parsedLocation;
    }

    protected void cacheTargets(ScriptActionData scriptActionData) {
        //Only cache if tracking
        if (getTargetBlueprint().isTrack()) return;
        //Only cache locations - caching living entities would probably be very confusing
        //if (actionType.isRequiresLivingEntity()) return;
        boolean animatedScriptZone = false;
        if (eliteScript.getScriptZone().isValid()) {
            scriptActionData.setCachedShapes(eliteScript.getScriptZone().precacheShapes(scriptActionData));
            if (eliteScript.getScriptZone().getZoneBlueprint().getAnimationDuration() > 0) animatedScriptZone = true;
        }
        if (!animatedScriptZone)
            scriptActionData.setLocations(new ArrayList<>(getTargetLocations(scriptActionData)));
    }

    //Get living entity targets. New array lists so they are not immutable.
    protected Collection<? extends LivingEntity> getTargetEntities(ScriptActionData scriptActionData) {
        if (livingEntities != null) return livingEntities;
        //If a script zone exists, it overrides the check entirely to expose zone-based fields
        Location eliteEntityLocation = scriptActionData.getEliteEntity().getLocation();
        switch (targetBlueprint.getTargetType()) {
            case ALL_PLAYERS:
                return new ArrayList<>(Bukkit.getOnlinePlayers());
            case WORLD_PLAYERS:
                return new ArrayList<>(eliteEntityLocation.getWorld().getPlayers());
            case NEARBY_PLAYERS:
                return eliteEntityLocation.getWorld()
                        .getNearbyEntities(
                                eliteEntityLocation,
                                targetBlueprint.getRange(),
                                targetBlueprint.getRange(),
                                targetBlueprint.getRange(),
                                (entity -> entity.getType() == EntityType.PLAYER))
                        .stream().map(Player.class::cast).collect(Collectors.toSet());
            case DIRECT_TARGET:
                return new ArrayList<>(List.of(scriptActionData.getDirectTarget()));
            case SELF:
            case SELF_SPAWN:
                return new ArrayList<>(List.of(scriptActionData.getEliteEntity().getUnsyncedLivingEntity()));
            case ZONE_FULL, ZONE_BORDER:
                return eliteScript.getScriptZone().getEffectTargets(scriptActionData, targetBlueprint);
            default:
                new WarningMessage("Could not find default target for script in " + eliteScript.getFileName());
                return new ArrayList<>();
        }
    }

    /**
     * Obtains the target locations for a script. Some scripts require locations instead of living entities, and this
     * method obtains those locations from the potential targets.
     *
     * @return Validated location for the script behavior
     */
    protected Collection<Location> getTargetLocations(ScriptActionData scriptActionData) {
        Collection<Location> newLocations = null;
        if (scriptActionData.getLocations() != null) newLocations = new ArrayList<>(scriptActionData.getLocations());
        else {
            if (targetBlueprint.getTargetType() == Target.ALL_PLAYERS ||
                    targetBlueprint.getTargetType() == Target.WORLD_PLAYERS ||
                    targetBlueprint.getTargetType() == Target.NEARBY_PLAYERS ||
                    targetBlueprint.getTargetType() == Target.DIRECT_TARGET ||
                    targetBlueprint.getTargetType() == Target.SELF)
                newLocations = getTargetEntities(scriptActionData).stream().map(targetEntity -> targetEntity.getLocation().add(targetBlueprint.getOffset())).collect(Collectors.toSet());
            else if (targetBlueprint.getTargetType() == Target.SELF_SPAWN)
                newLocations = new ArrayList<>(List.of(scriptActionData.getEliteEntity().getSpawnLocation().clone().add(targetBlueprint.getOffset())));
            if (newLocations == null)
                switch (targetBlueprint.getTargetType()) {
                    case LOCATION:
                        newLocations = new ArrayList<>(List.of(getLocation(scriptActionData.getEliteEntity())));
                        break;
                    case LOCATIONS:
                        newLocations = getLocations(scriptActionData.getEliteEntity());
                        break;
                    case LANDING_LOCATION:
                        newLocations = new ArrayList<>(List.of(scriptActionData.getLandingLocation()));
                        break;
                    case ZONE_FULL, ZONE_BORDER:
                        if (eliteScript.getScriptZone() != null) {
                            if (targetBlueprint.getOffset().equals(new Vector(0, 0, 0))) {
                                newLocations = eliteScript.getScriptZone().getEffectLocationTargets(scriptActionData, this);
                                break;
                            } else {
                                newLocations = (new ArrayList<>(eliteScript.getScriptZone().getEffectLocationTargets(scriptActionData, this))).stream().map(iteratedLocation -> iteratedLocation.clone().add(targetBlueprint.getOffset())).collect(Collectors.toSet());
                                break;
                            }
                        } else {
                            new WarningMessage("Your script " + targetBlueprint.getScriptName() + " uses " + targetBlueprint.getTargetType().toString() + " but does not have a valid Zone defined!");
                            {
                                newLocations = new ArrayList<>();
                                break;
                            }
                        }
                    default:
                        newLocations = new ArrayList<>();
                }
        }

        if (targetBlueprint.getCoverage() > 0)
            newLocations.removeIf(targetLocation -> ThreadLocalRandom.current().nextDouble() > targetBlueprint.getCoverage());

        return newLocations;
    }

    //Parse the locations key
    private Collection<Location> getLocations(EliteEntity eliteEntity) {
        return targetBlueprint.getLocations().stream().map(rawLocation -> processLocationFromString(
                eliteEntity,
                rawLocation,
                getTargetBlueprint().getScriptName(),
                eliteScript.getFileName(),
                targetBlueprint.getOffset())).collect(Collectors.toSet());
    }

    //Parse the location key
    private Location getLocation(EliteEntity eliteEntity) {
        return processLocationFromString(
                eliteEntity,
                targetBlueprint.getLocation(),
                getTargetBlueprint().getScriptName(),
                eliteScript.getFileName(),
                getTargetBlueprint().getOffset());
    }
}
