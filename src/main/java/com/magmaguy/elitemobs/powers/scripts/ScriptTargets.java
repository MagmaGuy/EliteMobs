package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.elitemobs.powers.scripts.enums.Target;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.shapes.Shape;
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
import java.util.stream.Collectors;

public class ScriptTargets {

    @Getter
    private final ScriptTargetsBlueprint targetBlueprint;
    private final EliteScript eliteScript;
    private Collection<? extends LivingEntity> livingEntities = null;
    private Collection<Location> locations = null;

    public ScriptTargets(ScriptTargetsBlueprint targetBlueprint, EliteScript eliteScript) {
        this.targetBlueprint = targetBlueprint;
        this.eliteScript = eliteScript;
    }

    //Parse all string-based configuration locations
    public static Location processLocationFromString(EliteEntity eliteEntity, String locationString, String scriptName, String filename, Vector offset) {
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

    List<Shape> cachedShapes = null;

    protected void cacheTargets(EliteEntity eliteEntity, LivingEntity directTarget, ActionType actionType) {
        //Only cache if tracking
        if (!getTargetBlueprint().isTrack()) return;
        //Only cache locations - caching living entities would probably be very confusing
        if (!actionType.isRequiresLivingEntity()) {
            locations = null;
            locations = getZoneLocationTargets(eliteEntity, directTarget);
        } else if (eliteScript.getScriptZone().isValid()) {
            cachedShapes = eliteScript.getScriptZone().precacheShapes(eliteEntity, directTarget);
        }
    }

    //Get living entity targets. New array lists so they are not immutable.
    protected Collection<? extends LivingEntity> getTargets(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (livingEntities != null) return livingEntities;
        //If a script zone exists, it overrides the check entirely to expose zone-based fields
        Location eliteEntityLocation = eliteEntity.getLocation();
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
                return new ArrayList<>(List.of(directTarget));
            case SELF:
                return new ArrayList<>(List.of(eliteEntity.getUnsyncedLivingEntity()));
            case ZONE_FULL, ZONE_BORDER:
                return eliteScript.getScriptZone().getEffectTargets(eliteEntity, directTarget, targetBlueprint, cachedShapes);
            default:
                new WarningMessage("Could not find default target for script in " + eliteScript.getFileName());
                return new ArrayList<>();
        }
    }

    /**
     * Obtains the target locations for a script. Some scripts require locations instead of living entities, and this
     * method obtains those locations from the potential targets.
     *
     * @param eliteEntity  Elite Entity running the script
     * @param directTarget Direct target
     * @return Validated location for the script behavior
     */
    protected Collection<Location> getZoneLocationTargets(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (locations != null) return locations;
        if (targetBlueprint.getTargetType() == Target.ALL_PLAYERS ||
                targetBlueprint.getTargetType() == Target.WORLD_PLAYERS ||
                targetBlueprint.getTargetType() == Target.NEARBY_PLAYERS ||
                targetBlueprint.getTargetType() == Target.DIRECT_TARGET ||
                targetBlueprint.getTargetType() == Target.SELF)
            return getTargets(eliteEntity, directTarget).stream().map(targetEntity -> targetEntity.getLocation().add(targetBlueprint.getOffset())).collect(Collectors.toSet());
        switch (targetBlueprint.getTargetType()) {
            case LOCATION:
                return List.of(getLocation(eliteEntity));
            case LOCATIONS:
                return getLocations(eliteEntity);
            case ZONE_FULL, ZONE_BORDER:
                if (eliteScript.getScriptZone() != null) {
                    if (targetBlueprint.getOffset().equals(new Vector(0, 0, 0)))
                        return eliteScript.getScriptZone().getEffectLocationTargets(eliteEntity, directTarget, this, cachedShapes);
                    else
                        return (new ArrayList<>(eliteScript.getScriptZone().getEffectLocationTargets(eliteEntity, directTarget, this, cachedShapes))).stream().map(iteratedLocation -> iteratedLocation.clone().add(targetBlueprint.getOffset())).collect(Collectors.toSet());
                } else {
                    new WarningMessage("Your script " + targetBlueprint.getScriptName() + " uses " + targetBlueprint.getTargetType().toString() + " but does not have a valid Zone defined!");
                    return new ArrayList<>();
                }
            default:
                return new ArrayList<>();
        }

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
