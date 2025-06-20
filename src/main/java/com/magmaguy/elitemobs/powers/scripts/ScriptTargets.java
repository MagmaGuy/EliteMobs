package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ScriptTargets {

    @Getter
    private final ScriptTargetsBlueprint targetBlueprint;
    @Getter
    private final EliteScript eliteScript;
    //raw zone from the elite script
    @Getter
    private final ScriptZone scriptZone;
    //collection of targets, can be shapes, entities or locations
    private List anonymousTargets = null;
    @Getter
    private ScriptRelativeVector scriptRelativeVector = null;

    public ScriptTargets(ScriptTargetsBlueprint targetBlueprint, EliteScript eliteScript) {
        this.targetBlueprint = targetBlueprint;
        this.eliteScript = eliteScript;
        this.scriptZone = eliteScript.getScriptZone();
    }

    public List getAnonymousTargets(boolean locations, ScriptActionData scriptActionData) {
        if (anonymousTargets != null) {
            return anonymousTargets;
        } else if (locations) {
            return getTargetLocations(scriptActionData).stream().toList();
        } else {
            return getTargetEntities(scriptActionData).stream().toList();
        }
    }

    public void setAnonymousTargets(List anonymousTargets) {
        //Animated zones can't be cached!
        if (getTargetBlueprint().isTrack() || eliteScript.getScriptZone().getZoneBlueprint().getAnimationDuration().getValue() > 1)
            return;
        //Non-animated zones must be cached for script inheritance and such
        this.anonymousTargets = anonymousTargets;
    }

    //Parse all string-based configuration locations
    public Location processLocationFromString(EliteEntity eliteEntity,
                                              String locationString,
                                              ScriptActionData scriptActionData) {
        if (locationString == null) {
            Logger.warn("Failed to get location target in script " + targetBlueprint.getScriptName() + " in " + eliteScript.getFileName());
            return null;
        }
        Location parsedLocation = ConfigurationLocation.serialize(locationString);
        if (parsedLocation.getWorld() == null && locationString.split(",")[0].equalsIgnoreCase("same_as_boss")) {
            parsedLocation.setWorld(eliteEntity.getLocation().getWorld());
        }

        addOffsets(parsedLocation, scriptActionData);

        return parsedLocation;
    }

    protected void cacheTargets(ScriptActionData scriptActionData) {
        if (getTargetBlueprint().isTrack()) {
            //Zones that animate independently can not be set to track, as this causes confusion. This is forced to make it easier on scripters.
            if (eliteScript.getScriptZone().isValid() && eliteScript.getScriptZone().getZoneBlueprint().getAnimationDuration().getValue() > 0)
                getTargetBlueprint().setTrack(false);
            else return;
        }
        //Only cache locations - caching living entities would probably be very confusing
        //if (actionType.isRequiresLivingEntity()) return;
        boolean animatedScriptZone = false;
        if (eliteScript.getScriptZone().isValid()) {
            scriptActionData.setShapesCachedByTarget(eliteScript.getScriptZone().generateShapes(scriptActionData, true));
            if (eliteScript.getScriptZone().getZoneBlueprint().getAnimationDuration().getValue() > 0) animatedScriptZone = true;
            anonymousTargets = null;
        }
        if (!animatedScriptZone) {
            anonymousTargets = new ArrayList<>(getTargetLocations(scriptActionData));
        }
        if (!getTargetBlueprint().isTrack() && targetBlueprint.getScriptRelativeVectorBlueprint() != null) {
            scriptRelativeVector = new ScriptRelativeVector(targetBlueprint.getScriptRelativeVectorBlueprint(), eliteScript, null);
            scriptRelativeVector.cacheVector(scriptActionData);
        }
    }

    //Get living entity targets. New array lists so they are not immutable.
    protected Collection<LivingEntity> getTargetEntities(ScriptActionData scriptActionData) {
        if (getTargetBlueprint().isTrack() && anonymousTargets != null &&
                (anonymousTargets.isEmpty() || anonymousTargets.get(0) instanceof LivingEntity)) {
            return (List<LivingEntity>) anonymousTargets;
        }

        //If a script zone exists, it overrides the check entirely to expose zone-based fields
        Location eliteEntityLocation = scriptActionData.getEliteEntity().getLocation();

        if (targetBlueprint == null) {
            Logger.warn("An action tried to run with an invalid target! Check which on it is by reading the startup logs and fix it! No target will be acquired for now.");
            return new ArrayList<>();
        }

        switch (targetBlueprint.getTargetType()) {
            case ALL_PLAYERS:
                return new ArrayList<>(Bukkit.getOnlinePlayers());
            case WORLD_PLAYERS:
                return new ArrayList<>(eliteEntityLocation.getWorld().getPlayers());
            case NEARBY_PLAYERS:
                return eliteEntityLocation.getWorld()
                        .getNearbyEntities(
                                eliteEntityLocation,
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                (entity -> entity.getType() == EntityType.PLAYER))
                        .stream().map(Player.class::cast).collect(Collectors.toSet());
            case NEARBY_MOBS:
                return eliteEntityLocation.getWorld()
                        .getNearbyEntities(
                                eliteEntityLocation,
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                (entity -> entity.getType() != EntityType.PLAYER && entity instanceof LivingEntity &&
                                        !entity.getUniqueId().equals(scriptActionData.getEliteEntity().getUnsyncedLivingEntity().getUniqueId())))
                        .stream().map(LivingEntity.class::cast).collect(Collectors.toSet());
            case NEARBY_ELITES:
                return eliteEntityLocation.getWorld()
                        .getNearbyEntities(
                                eliteEntityLocation,
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                entity -> EntityTracker.isEliteMob(entity) &&
                                        !entity.getUniqueId().equals(scriptActionData.getEliteEntity().getUnsyncedLivingEntity().getUniqueId()))
                        .stream()
                        .map(LivingEntity.class::cast)
                        .collect(Collectors.toSet());
            case DIRECT_TARGET:
                return new ArrayList<>(List.of(scriptActionData.getDirectTarget()));
            case SELF:
            case SELF_SPAWN:
                return new ArrayList<>(List.of(scriptActionData.getEliteEntity().getUnsyncedLivingEntity()));
            case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER:
                return eliteScript.getScriptZone().getZoneEntities(scriptActionData, targetBlueprint);
            case INHERIT_SCRIPT_TARGET:
                if (scriptActionData.getInheritedScriptActionData() != null) {
                    try {
                        return (List<LivingEntity>) scriptActionData.getInheritedScriptActionData().getScriptTargets().getAnonymousTargets(false, scriptActionData.getInheritedScriptActionData());
                    } catch (Exception Ex) {
                        Logger.warn("Failed to get entity from INHERIT_SCRIPT_TARGET because the script inherits a location, not an entity");
                    }
                } else {
                    Logger.warn("Failed to get INHERIT_SCRIPT_TARGET because the script is not called by another script!");
                    return new ArrayList<>();
                }

            default:
                Logger.warn("Could not find default target for script in " + eliteScript.getFileName());
                return null;
        }
    }

    /**
     * Obtains the target locations for a script. Some scripts require locations instead of living entities, and this
     * method obtains those locations from the potential targets.
     *
     * @return Validated location for the script behavior
     */
    protected Collection<Location> getTargetLocations(ScriptActionData scriptActionData) {
        if (anonymousTargets != null && !anonymousTargets.isEmpty() && anonymousTargets.get(0) instanceof Location location) {
            return (List<Location>) anonymousTargets;
        }

        Collection<Location> newLocations = null;

        switch (this.getTargetBlueprint().getTargetType()) {
            case ALL_PLAYERS, WORLD_PLAYERS, NEARBY_PLAYERS, DIRECT_TARGET, SELF, NEARBY_MOBS, NEARBY_ELITES:
                return getTargetEntities(scriptActionData).stream().map(targetEntity -> addOffsets(targetEntity.getLocation(), scriptActionData)).collect(Collectors.toSet());
            case SELF_SPAWN:
                return new ArrayList<>(List.of(addOffsets(scriptActionData.getEliteEntity().getSpawnLocation(), scriptActionData)));
            case LOCATION:
                return new ArrayList<>(List.of(getLocation(scriptActionData.getEliteEntity(), scriptActionData)));
            case LOCATIONS:
                return getLocations(scriptActionData.getEliteEntity(), scriptActionData);
            case LANDING_LOCATION:
                return new ArrayList<>(List.of(scriptActionData.getLandingLocation().clone()));
            case ZONE_FULL, ZONE_BORDER:
                newLocations = getLocationFromZone(scriptActionData);
                break;
            case INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER:
                newLocations = getLocationFromZone(scriptActionData.getInheritedScriptActionData());
                break;
            case INHERIT_SCRIPT_TARGET:
                return scriptActionData.getInheritedScriptActionData().getScriptTargets().getAnonymousTargets(
                        true, scriptActionData.getInheritedScriptActionData());
            default:
                Logger.warn("Failed to get target type in script " + getTargetBlueprint().getScriptName() + " !");
        }

        if (targetBlueprint.getCoverage().getValue() < 1)
            newLocations.removeIf(targetLocation -> ThreadLocalRandom.current().nextDouble() > targetBlueprint.getCoverage().getValue());

        return newLocations;
    }

    private Collection<Location> getLocationFromZone(ScriptActionData scriptActionData) {
        if (scriptActionData.getScriptZone() == null) {
            Logger.warn("Your script " + targetBlueprint.getScriptName() + " uses " + targetBlueprint.getTargetType().toString() + " but does not have a valid Zone defined!");
            return new ArrayList<>();
        }
        return addOffsets(eliteScript.getScriptZone().getZoneLocations(scriptActionData, this), scriptActionData);
    }

    //Parse the locations key
    private Collection<Location> getLocations(EliteEntity eliteEntity, ScriptActionData scriptActionData) {
        return targetBlueprint.getLocations().stream().map(rawLocation -> processLocationFromString(
                eliteEntity,
                rawLocation,
                scriptActionData)).collect(Collectors.toSet());
    }

    //Parse the location key
    private Location getLocation(EliteEntity eliteEntity, ScriptActionData scriptActionData) {
        return processLocationFromString(eliteEntity, targetBlueprint.getLocation(), scriptActionData);
    }

    private Location addOffsets(Location originalLocation, ScriptActionData scriptActionData) {
        Location location = originalLocation.clone().add(targetBlueprint.getOffset().getValue());
        if (targetBlueprint.getScriptRelativeVectorBlueprint() != null)
            scriptRelativeVector = new ScriptRelativeVector(targetBlueprint.getScriptRelativeVectorBlueprint(), eliteScript, location);
        else
            return location;

        location.add(scriptRelativeVector.getVector(scriptActionData));

        return location;
    }

    private Collection<Location> addOffsets(Collection<Location> locations, ScriptActionData scriptActionData) {
        if (targetBlueprint.getOffset().getValue().length() == 0 && scriptRelativeVector == null) return locations;
        locations.forEach(entry -> addOffsets(entry, scriptActionData));
        return locations;
    }
}
