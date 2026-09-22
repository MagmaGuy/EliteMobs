package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScriptTargets {

    @Getter
    private final ScriptTargetsBlueprint targetBlueprint;
    @Getter
    private final ScriptRuntimeOwner runtimeOwner;

    public ScriptTargets(ScriptTargetsBlueprint targetBlueprint, ScriptRuntimeOwner runtimeOwner) {
        this.targetBlueprint = targetBlueprint;
        this.runtimeOwner = runtimeOwner;
    }

    public List<?> getAnonymousTargets(boolean locations, ScriptActionData scriptActionData) {
        List<?> inherited = scriptActionData.inheritedTargets(this);
        if (inherited != null) {
            return inherited;
        } else if (locations) {
            return getTargetLocations(scriptActionData).stream().toList();
        } else {
            return getTargetEntities(scriptActionData).stream().toList();
        }
    }

    public void setAnonymousTargets(List<?> targets, ScriptActionData data) {
        if (!targetBlueprint.isTrack() && !animatedZone(data)) data.captureInheritedTargets(this, targets);
    }

    private boolean animatedZone(ScriptActionData data) {
        ScriptZone zone = targetBlueprint.isZoneTarget() ? resolveZoneData(data).getScriptZone() : getScriptZone();
        return zone != null && zone.isValid() && zone.getZoneBlueprint().getAnimationDuration().getValue() > 0;
    }

    //Parse all string-based configuration locations
    public Location processLocationFromString(EliteEntity eliteEntity,
                                              String locationString,
                                              ScriptActionData scriptActionData) {
        if (locationString == null) {
            Logger.warn("Failed to get location target in script " + targetBlueprint.getScriptName() + " in " + runtimeOwner.getFileName());
            return null;
        }
        Location parsedLocation = ConfigurationLocation.serialize(locationString);
        if (parsedLocation.getWorld() == null && locationString.split(",")[0].equalsIgnoreCase("same_as_boss")) {
            parsedLocation.setWorld(eliteEntity.getLocation().getWorld());
        }
        if (parsedLocation.getWorld() == null && eliteEntity.getLocation() != null && eliteEntity.getLocation().getWorld() != null) {
            //Instanced dungeons clone the blueprint world under "<blueprintWorldName>_<number>", so a
            //configured world name that isn't loaded must still resolve when the boss is inside an
            //instance of that blueprint world - otherwise teleports and zone origins get a null world.
            World bossWorld = eliteEntity.getLocation().getWorld();
            String configuredWorldName = ConfigurationLocation.worldName(locationString);
            if (configuredWorldName != null && !configuredWorldName.isBlank() &&
                    bossWorld.getName().matches(Pattern.quote(configuredWorldName) + "_\\d+"))
                parsedLocation.setWorld(bossWorld);
        }

        return addOffsets(parsedLocation, scriptActionData);
    }

    protected void cacheTargets(ScriptActionData data) {
        boolean animated = animatedZone(data);
        if (targetBlueprint.isTrack() && !animated) return;
        ScriptZone zone = getScriptZone();
        if (zone != null && zone.isValid() && data.getShapesCachedByTarget() == null)
            data.setShapesCachedByTarget(zone.generateShapes(data, true));
        if (!animated) data.captureLocations(this, getTargetLocations(data));
    }

    //Get living entity targets. New array lists so they are not immutable.
    public Collection<LivingEntity> getTargetEntities(ScriptActionData scriptActionData) {
        //If a script zone exists, it overrides the check entirely to expose zone-based fields
        Location eliteEntityLocation = scriptActionData.getEliteEntity().getLocation();

        if (targetBlueprint == null) {
            Logger.warn("An action tried to run with an invalid target! Check which on it is by reading the startup logs and fix it! No target will be acquired for now.");
            return new ArrayList<>();
        }

        //A script task can outlive its elite: removals, phase swaps and world unloads can leave
        //the elite without a living entity or usable location while a repeating action still
        //ticks. Returning no targets lets conditions fail cleanly and repeating actions cancel,
        //instead of throwing a NullPointerException every tick forever.
        LivingEntity selfEntity = scriptActionData.getEliteEntity().getUnsyncedLivingEntity();
        java.util.UUID selfUUID = selfEntity == null ? null : selfEntity.getUniqueId();
        boolean eliteLocationGone = eliteEntityLocation == null || eliteEntityLocation.getWorld() == null;

        switch (targetBlueprint.getTargetType()) {
            case ALL_PLAYERS:
                return new ArrayList<>(Bukkit.getOnlinePlayers());
            case WORLD_PLAYERS:
                if (eliteLocationGone) return new ArrayList<>();
                return new ArrayList<>(eliteEntityLocation.getWorld().getPlayers());
            case NEARBY_PLAYERS:
                if (eliteLocationGone) return new ArrayList<>();
                return eliteEntityLocation.getWorld()
                        .getNearbyEntities(
                                eliteEntityLocation,
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                (entity -> entity.getType() == EntityType.PLAYER))
                        .stream().map(Player.class::cast).collect(Collectors.toSet());
            case NEARBY_MOBS:
                if (eliteLocationGone) return new ArrayList<>();
                return eliteEntityLocation.getWorld()
                        .getNearbyEntities(
                                eliteEntityLocation,
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                (entity -> entity.getType() != EntityType.PLAYER && entity instanceof LivingEntity &&
                                        (selfUUID == null || !entity.getUniqueId().equals(selfUUID))))
                        .stream().map(LivingEntity.class::cast).collect(Collectors.toSet());
            case NEARBY_ELITES:
                if (eliteLocationGone) return new ArrayList<>();
                return eliteEntityLocation.getWorld()
                        .getNearbyEntities(
                                eliteEntityLocation,
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                targetBlueprint.getRange().getValue(),
                                entity -> EntityTracker.isEliteMob(entity) &&
                                        (selfUUID == null || !entity.getUniqueId().equals(selfUUID)))
                        .stream()
                        .map(LivingEntity.class::cast)
                        .collect(Collectors.toSet());
            case DIRECT_TARGET:
                return scriptActionData.getDirectTarget() == null ? new ArrayList<>()
                        : new ArrayList<>(List.of(scriptActionData.getDirectTarget()));
            case SELF:
            case SELF_SPAWN:
                if (selfEntity == null) return new ArrayList<>();
                return new ArrayList<>(List.of(selfEntity));
            case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER:
                ScriptActionData zoneData = resolveZoneData(scriptActionData);
                return zoneData.getScriptZone().getZoneEntities(zoneData, zoneTargetType());
            case INHERIT_SCRIPT_TARGET:
                ScriptActionData parent = requireParent(scriptActionData);
                List<?> inherited = parent.getScriptTargets().getAnonymousTargets(false, parent);
                List<LivingEntity> entities = new ArrayList<>();
                for (Object target : inherited) {
                    if (!(target instanceof LivingEntity entity))
                        throw new IllegalStateException("INHERIT_SCRIPT_TARGET requires entities in " + targetBlueprint.getScriptName());
                    entities.add(entity);
                }
                return entities;
            case LOCATION, LOCATIONS, LANDING_LOCATION, ACTION_TARGET:
                return new ArrayList<>();
            default:
                throw new IllegalStateException("Invalid target type in " + targetBlueprint.getScriptName());
        }
    }

    /**
     * Obtains the target locations for a script. Some scripts require locations instead of living entities, and this
     * method obtains those locations from the potential targets.
     *
     * @return Validated location for the script behavior
     */
    public Collection<Location> getTargetLocations(ScriptActionData scriptActionData) {
        return getTargetLocations(scriptActionData, null);
    }

    // A condition evaluation can reuse the entities it already resolved without freezing later actions.
    Collection<Location> getTargetLocations(ScriptActionData scriptActionData, Collection<LivingEntity> entities) {
        List<Location> frozen = scriptActionData.locationSnapshot(this);
        if (frozen != null) return frozen;
        Collection<Location> newLocations;

        switch (this.getTargetBlueprint().getTargetType()) {
            case ALL_PLAYERS, WORLD_PLAYERS, NEARBY_PLAYERS, DIRECT_TARGET, SELF, NEARBY_MOBS, NEARBY_ELITES:
                return (entities == null ? getTargetEntities(scriptActionData) : entities).stream()
                        .map(targetEntity -> addOffsets(targetEntity.getLocation(), scriptActionData)).collect(Collectors.toSet());
            case SELF_SPAWN:
                return new ArrayList<>(List.of(addOffsets(scriptActionData.getEliteEntity().getSpawnLocation(), scriptActionData)));
            case LOCATION:
                return new ArrayList<>(List.of(getLocation(scriptActionData.getEliteEntity(), scriptActionData)));
            case LOCATIONS:
                return getLocations(scriptActionData.getEliteEntity(), scriptActionData);
            case LANDING_LOCATION:
                return new ArrayList<>(List.of(scriptActionData.getLandingLocation().clone()));
            case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER:
                newLocations = getLocationFromZone(scriptActionData);
                break;
            case INHERIT_SCRIPT_TARGET:
                ScriptActionData parent = requireParent(scriptActionData);
                List<?> inherited = parent.getScriptTargets().getAnonymousTargets(true, parent);
                List<Location> locations = new ArrayList<>();
                for (Object target : inherited) {
                    if (target instanceof Location location) locations.add(location.clone());
                    else if (target instanceof LivingEntity entity) locations.add(entity.getLocation());
                    else throw new IllegalStateException("Invalid inherited target in " + targetBlueprint.getScriptName());
                }
                return locations;
            case ACTION_TARGET:
                //This is an edge case of action target, if there were no nearby targets it can't assume a value so it passes action target as a fallback. That just means there aren't valid targets.
                return new ArrayList<>();
            default: {
                throw new IllegalStateException("Invalid target type in " + targetBlueprint.getScriptName());
            }
        }

        if (targetBlueprint.getCoverage().getValue() < 1)
            newLocations.removeIf(targetLocation -> ThreadLocalRandom.current().nextDouble() > targetBlueprint.getCoverage().getValue());

        return newLocations;
    }

    private Collection<Location> getLocationFromZone(ScriptActionData data) {
        ScriptActionData zoneData = resolveZoneData(data);
        return addOffsets(zoneData.getScriptZone().getZoneLocations(zoneData, zoneTargetType()), data);
    }

    ScriptActionData resolveZoneData(ScriptActionData data) {
        ScriptActionData resolved = switch (targetBlueprint.getTargetType()) {
            case INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER -> requireParent(data);
            default -> data;
        };
        if (resolved.getScriptZone() == null || !resolved.getScriptZone().isValid())
            throw new IllegalStateException("No valid zone for " + targetBlueprint.getTargetType()
                    + " in script " + targetBlueprint.getScriptName());
        return resolved;
    }

    private ScriptActionData requireParent(ScriptActionData data) {
        ScriptActionData parent = data.getInheritedScriptActionData();
        if (parent == null) throw new IllegalStateException("No parent invocation for "
                + targetBlueprint.getTargetType() + " in script " + targetBlueprint.getScriptName());
        return parent;
    }

    private TargetType zoneTargetType() {
        return switch (targetBlueprint.getTargetType()) {
            case ZONE_FULL, INHERIT_SCRIPT_ZONE_FULL -> TargetType.ZONE_FULL;
            case ZONE_BORDER, INHERIT_SCRIPT_ZONE_BORDER -> TargetType.ZONE_BORDER;
            default -> throw new IllegalStateException("Not a zone target");
        };
    }

    private ScriptZone getScriptZone() {
        return runtimeOwner.getScriptZone();
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
            location.add(new ScriptRelativeVector(targetBlueprint.getScriptRelativeVectorBlueprint(), runtimeOwner, location)
                    .getVector(scriptActionData));

        return location;
    }

    private Collection<Location> addOffsets(Collection<Location> locations, ScriptActionData scriptActionData) {
        return locations.stream().map(location -> addOffsets(location, scriptActionData))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
