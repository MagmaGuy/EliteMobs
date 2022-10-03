package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptZoneBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.Target;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.shapes.Cylinder;
import com.magmaguy.elitemobs.utils.shapes.Dome;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import com.magmaguy.elitemobs.utils.shapes.Sphere;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptZone {

    private final ScriptZoneBlueprint zoneBlueprint;
    private final ScriptTargets scriptTargets;
    @Getter
    private boolean isValid;

    public ScriptZone(ScriptZoneBlueprint zoneBlueprint, EliteScript eliteScript) {
        this.zoneBlueprint = zoneBlueprint;
        this.scriptTargets = new ScriptTargets(zoneBlueprint.getScriptTargetsBlueprint(), eliteScript);
        isValid = zoneBlueprint.getScriptTargetsBlueprint() != null;
    }

    //Used for tracking, allows locations to be picked and set to be consistent throughout
    public List<Shape> precacheShapes(EliteEntity eliteEntity, LivingEntity directTarget) {
        return generateShapes(eliteEntity, directTarget);
    }

    //Get living entities in zone
    protected Collection<? extends LivingEntity> getEffectTargets(EliteEntity eliteEntity,
                                                                  LivingEntity directTarget,
                                                                  ScriptTargetsBlueprint blueprintFromRequestingTarget,
                                                                  List<Shape> precachedShapes) {
        //Generate shapes for the zone
        List<Shape> shapes;
        if (precachedShapes != null) shapes = precachedShapes;
        else shapes = generateShapes(eliteEntity, directTarget);

        //Get the entities from those zones
        switch (blueprintFromRequestingTarget.getTargetType()) {
            case ZONE_FULL, ZONE_BORDER:
                return getEntitiesInArea(shapes, scriptTargets.getTargetBlueprint().getTargetType());
            default: {
                new WarningMessage("Couldn't parse target " + scriptTargets.getTargetBlueprint().getTargetType() + " in script ");
                return new ArrayList<>();
            }
        }
    }

    //Get locations in zone
    protected Collection<Location> getEffectLocationTargets(EliteEntity eliteEntity,
                                                            LivingEntity directTarget,
                                                            ScriptTargets actionTarget,
                                                            List<Shape> precachedShapes) {
        //Generate shapes for the zone
        List<Shape> shapes;
        if (precachedShapes != null) shapes = precachedShapes;
        else shapes = generateShapes(eliteEntity, directTarget);

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
    private List<Shape> generateShapes(EliteEntity eliteEntity, LivingEntity directTarget) {
        List<Shape> shapes = new ArrayList<>();
        //Get the shapes from the targets of the zone
        for (Location shapeTargetLocation : scriptTargets.getZoneLocationTargets(eliteEntity, directTarget)) {
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
                if (target.equals(Target.ZONE_FULL))
                    return !shape.contains(livingEntity.getLocation());
                else if (target.equals(Target.ZONE_BORDER))
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
