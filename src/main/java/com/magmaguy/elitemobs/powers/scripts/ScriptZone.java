package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptZoneBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.Target;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.shapes.*;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptZone {

    @Getter
    private final ScriptZoneBlueprint zoneBlueprint;
    private final ScriptTargets targets;
    private ScriptTargets finalTargets = null;
    private ScriptTargets targets2 = null;
    private ScriptTargets finalTargets2 = null;
    @Getter
    private boolean isValid;

    public ScriptZone(ScriptZoneBlueprint zoneBlueprint, EliteScript eliteScript) {
        this.zoneBlueprint = zoneBlueprint;
        this.targets = new ScriptTargets(zoneBlueprint.getTarget(), eliteScript);
        if (zoneBlueprint.getFinalTarget() != null)
            finalTargets = new ScriptTargets(zoneBlueprint.getFinalTarget(), eliteScript);
        if (zoneBlueprint.getTarget2() != null)
            targets2 = new ScriptTargets(zoneBlueprint.getTarget2(), eliteScript);
        if (zoneBlueprint.getFinalTarget2() != null)
            finalTargets2 = new ScriptTargets(zoneBlueprint.getFinalTarget2(), eliteScript);
        isValid = zoneBlueprint.getTarget() != null;
    }

    //Used for tracking, allows locations to be picked and set to be consistent throughout
    public List<Shape> precacheShapes(ScriptActionData scriptActionData) {
        return generateShapes(scriptActionData);
    }

    //Get living entities in zone
    protected Collection<? extends LivingEntity> getEffectTargets(ScriptActionData scriptActionData,
                                                                  ScriptTargetsBlueprint blueprintFromRequestingTarget) {
        //Generate shapes for the zone
        List<Shape> shapes;
        if (scriptActionData.getCachedShapes() != null) {
            shapes = scriptActionData.getCachedShapes();
        } else {
            shapes = generateShapes(scriptActionData);
        }

        //Get the entities from those zones
        switch (blueprintFromRequestingTarget.getTargetType()) {
            case ZONE_FULL, ZONE_BORDER:
                return getEntitiesInArea(shapes, blueprintFromRequestingTarget.getTargetType());
            default: {
                new WarningMessage("Couldn't parse target " + targets.getTargetBlueprint().getTargetType() + " in script ");
                return new ArrayList<>();
            }
        }
    }

    //Get locations in zone
    protected Collection<Location> getEffectLocationTargets(ScriptActionData scriptActionData,
                                                            ScriptTargets actionTarget) {
        //Generate shapes for the zone
        List<Shape> shapes;
        if (scriptActionData.getCachedShapes() != null) {
            shapes = scriptActionData.getCachedShapes();
        } else {
            shapes = generateShapes(scriptActionData);
        }

        //Get the locations from those zones
        return switch (actionTarget.getTargetBlueprint().getTargetType()) {
            case ZONE_FULL -> consolidateLists(shapes.stream().map(Shape::getLocations).collect(Collectors.toSet()));
            case ZONE_BORDER ->
                    consolidateLists(shapes.stream().map(Shape::getEdgeLocations).collect(Collectors.toSet()));
            default -> new ArrayList<>();
        };
    }

    //Consolidates lists of lists of locations -> lists of locations
    private Collection<Location> consolidateLists(Collection<Collection<Location>> originalLocations) {
        Collection<Location> parsedLocations = new ArrayList<>();
        originalLocations.forEach(parsedLocations::addAll);
        return parsedLocations;
    }

    //Generate shapes that define the zone
    private List<Shape> generateShapes(ScriptActionData scriptActionData) {
        List<Shape> shapes = new ArrayList<>();
        //Get the shapes from the targets of the zone
        for (Location shapeTargetLocation : targets.getTargetLocations(scriptActionData)) {
            switch (zoneBlueprint.getShapeTypeEnum()) {
                case CYLINDER:
                    shapes.add(new Cylinder(shapeTargetLocation, zoneBlueprint.getRadius(), zoneBlueprint.getHeight(), zoneBlueprint.getBorderRadius()));
                    break;
                case SPHERE:
                    shapes.add(new Sphere(zoneBlueprint.getRadius(), shapeTargetLocation, zoneBlueprint.getBorderRadius()));
                    break;
                case DOME:
                    shapes.add(new Dome(zoneBlueprint.getRadius(), shapeTargetLocation, zoneBlueprint.getBorderRadius()));
                    break;
                case STATIC_RAY:
                    if (targets2 == null) {
                        new WarningMessage("Script for boss " + scriptActionData.getEliteEntity().getName() + " has a static ray but no set target2 for the ray!");
                        break;
                    }
                    for (Location location : targets2.getTargetLocations(scriptActionData))
                        shapes.add(
                                new StaticRay(
                                        zoneBlueprint.isIgnoresSolidBlocks(),
                                        zoneBlueprint.getPointRadius(),
                                        shapeTargetLocation,
                                        location));
                    break;
                case ROTATING_RAY:
                    if (targets2 == null) {
                        new WarningMessage("Script for boss " + scriptActionData.getEliteEntity().getName() + " has a static ray but no set target2 for the ray!");
                        break;
                    }
                    Location finalTargetLocation = null;
                    if (finalTargets != null) {
                        List<Location> finalTargetsList = finalTargets.getTargetLocations(scriptActionData).stream().toList();
                        if (!finalTargetsList.isEmpty()) finalTargetLocation = finalTargetsList.get(0);
                    }
                    for (Location location : targets2.getTargetLocations(scriptActionData)) {
                        //Developer.message("adding shape");
                        shapes.add(
                                new RotatingRay(
                                        zoneBlueprint.isIgnoresSolidBlocks(),
                                        zoneBlueprint.getPointRadius(),
                                        shapeTargetLocation,
                                        finalTargetLocation,
                                        location,
                                        zoneBlueprint.getPitchPreRotation(),
                                        zoneBlueprint.getYawPreRotation(),
                                        zoneBlueprint.getPitchRotation(),
                                        zoneBlueprint.getYawRotation(),
                                        zoneBlueprint.getAnimationDuration()));
                    }
                    break;
                case TRANSLATING_RAY:
                    if (targets2 == null) {
                        new WarningMessage("Script for boss " + scriptActionData.getEliteEntity().getName() + " has a static ray but no set target2 for the ray!");
                        break;
                    }
                    Location targetLocationEnd = null;
                    if (finalTargets != null) {
                        List<Location> finalTargetsList = finalTargets.getTargetLocations(scriptActionData).stream().toList();
                        if (!finalTargetsList.isEmpty()) targetLocationEnd = finalTargetsList.get(0);
                    }
                    Location target2LocationEnd = null;
                    if (finalTargets2 != null) {
                        List<Location> finalTargetsList = finalTargets2.getTargetLocations(scriptActionData).stream().toList();
                        if (!finalTargetsList.isEmpty()) target2LocationEnd = finalTargetsList.get(0);
                    }
                    for (Location location : targets2.getTargetLocations(scriptActionData))
                        shapes.add(
                                new TranslatingRay(
                                        zoneBlueprint.isIgnoresSolidBlocks(),
                                        zoneBlueprint.getPointRadius(),
                                        shapeTargetLocation,
                                        targetLocationEnd,
                                        location,
                                        target2LocationEnd,
                                        zoneBlueprint.getAnimationDuration()));
                    break;
                case CUBOID:
                    shapes.add(new Cuboid(
                            zoneBlueprint.getX(),
                            zoneBlueprint.getY(),
                            zoneBlueprint.getZ(),
                            zoneBlueprint.getXBorder(),
                            zoneBlueprint.getYBorder(),
                            zoneBlueprint.getZBorder(),
                            shapeTargetLocation));
                    break;
                default:
                    continue;
            }
        }
        return shapes;
    }


    //Get entities in an area based on a filter
    private Collection<? extends LivingEntity> getEntitiesInArea(List<Shape> shapes, Target target) {
        //Get entities in the world
        Collection<? extends LivingEntity> livingEntities = new ArrayList<>();
        for (Shape shape : shapes) {
            if (zoneBlueprint.getFilter() == null) livingEntities = filterByLiving(shape.getCenter());
            else switch (zoneBlueprint.getFilter()) {
                case PLAYER:
                    livingEntities = filterByPlayer(shape.getCenter());
                    break;
                case ELITE:
                    livingEntities = filterByElite(shape.getCenter());
                    break;
                case LIVING:
                    livingEntities = filterByLiving(shape.getCenter());
                    break;
                default:
                    return new ArrayList<>();
            }


            //Check if entities are in the relevant area
            livingEntities.removeIf(livingEntity -> {
                if (target.equals(Target.ZONE_FULL)) {
                    return !shape.contains(livingEntity.getLocation());
                } else if (target.equals(Target.ZONE_BORDER))
                    return !shape.borderContains(livingEntity.getLocation());
                return false;
            });
        }
        return livingEntities;
    }

    //Filter by player
    private Collection<? extends LivingEntity> filterByPlayer(Location center) {
        return center.getWorld().getPlayers();
    }

    //Filter by elite entity
    private Collection<? extends LivingEntity> filterByElite(Location center) {
        Collection<LivingEntity> entities = new ArrayList<>();
        center.getWorld().getEntities().forEach(entity -> {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
            if (eliteEntity != null) entities.add((LivingEntity) entity);
        });
        return entities;
    }

    //Filter by any kind of living entity
    private Collection<? extends LivingEntity> filterByLiving(Location center) {
        Collection<LivingEntity> entities = new ArrayList<>();
        center.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) entities.add(livingEntity);
        });
        return entities;
    }

}
