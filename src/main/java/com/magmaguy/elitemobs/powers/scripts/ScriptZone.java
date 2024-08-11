package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.ScriptZoneEnterEvent;
import com.magmaguy.elitemobs.api.ScriptZoneLeaveEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptZoneBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.shapes.*;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptZone {

    @Getter
    private final ScriptZoneBlueprint zoneBlueprint;
    private final ScriptTargets targets;
    @Getter
    private final boolean isValid;
    private ScriptTargets finalTargets = null;
    private ScriptTargets targets2 = null;
    private ScriptTargets finalTargets2 = null;
    @Setter
    private boolean zoneListener = false;
    //Used to do zone enter and leave events
    private Collection<LivingEntity> entitiesInZone;

    public ScriptZone(ScriptZoneBlueprint zoneBlueprint, EliteScript eliteScript) {
        this.zoneBlueprint = zoneBlueprint;
        this.targets = new ScriptTargets(zoneBlueprint.getTarget(), eliteScript);
        if (zoneBlueprint.getFinalTarget() != null)
            finalTargets = new ScriptTargets(zoneBlueprint.getFinalTarget(), eliteScript);
        if (zoneBlueprint.getTarget2() != null) targets2 = new ScriptTargets(zoneBlueprint.getTarget2(), eliteScript);
        if (zoneBlueprint.getFinalTarget2() != null)
            finalTargets2 = new ScriptTargets(zoneBlueprint.getFinalTarget2(), eliteScript);
        isValid = zoneBlueprint.getTarget() != null;
    }

    //todo: urgent: at a scale this will cause problems because it does not unschedule the task when a custom boss gets unloaded. Should be cancelling correctly though
    public void startZoneListener(EliteEntity eliteEntity) {
        if (!zoneListener) return;
        entitiesInZone = new HashSet<>();
        ScriptActionData scriptActionData = new ScriptActionData(eliteEntity, targets, this);
        new ZoneListenerTask(eliteEntity, scriptActionData).runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    public void ZoneEnterEvent(EliteEntity eliteEntity, LivingEntity livingEntity) {
        new EventCaller(new ScriptZoneEnterEvent(eliteEntity, livingEntity));
    }

    public void ZoneLeaveEvent(EliteEntity eliteEntity, LivingEntity livingEntity) {
        new EventCaller(new ScriptZoneLeaveEvent(eliteEntity, livingEntity));
    }

    //Get living entities in zone
    protected Collection<LivingEntity> getZoneEntities(ScriptActionData scriptActionData, ScriptTargetsBlueprint blueprintFromRequestingTarget) {
        //Get the entities from those zones
        switch (blueprintFromRequestingTarget.getTargetType()) {
            case ZONE_FULL, ZONE_BORDER:
                return getEntitiesInArea(generateShapes(scriptActionData, false), blueprintFromRequestingTarget.getTargetType());
            case INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER:
                return getEntitiesInArea(generateShapes(scriptActionData.getInheritedScriptActionData(), false), blueprintFromRequestingTarget.getTargetType());
            default: {
                Logger.warn("Couldn't parse target " + targets.getTargetBlueprint().getTargetType() + " in script ");
                return new ArrayList<>();
            }
        }
    }

    //Get locations in zone
    protected Collection<Location> getZoneLocations(ScriptActionData scriptActionData, ScriptTargets actionTarget) {
        //Get the locations from those zones
        return switch (actionTarget.getTargetBlueprint().getTargetType()) {
            case ZONE_FULL ->
                    consolidateLists(generateShapes(scriptActionData, false).stream().map(Shape::getLocations).collect(Collectors.toSet()));
            case ZONE_BORDER ->
                    consolidateLists(generateShapes(scriptActionData, false).stream().map(Shape::getEdgeLocations).collect(Collectors.toSet()));
            case INHERIT_SCRIPT_ZONE_FULL ->
                    consolidateLists(generateShapes(scriptActionData.getInheritedScriptActionData(), false).stream().map(Shape::getLocations).collect(Collectors.toSet()));
            case INHERIT_SCRIPT_ZONE_BORDER ->
                    consolidateLists(generateShapes(scriptActionData.getInheritedScriptActionData(), false).stream().map(Shape::getEdgeLocations).collect(Collectors.toSet()));
            default -> new ArrayList<>();
        };
    }

    //Consolidates lists of lists of locations -> lists of locations
    private Collection<Location> consolidateLists(Collection<Collection<Location>> originalLocations) {
        Collection<Location> parsedLocations = new ArrayList<>();
        originalLocations.forEach(parsedLocations::addAll);
        return parsedLocations;
    }

    //Generate shapes that define the zone, force is for caching strategies that require generating fresh (this is due to animated zones)
    public List<Shape> generateShapes(ScriptActionData scriptActionData, boolean force) {
        //for cached shapes
        if (!force && scriptActionData.getShapesChachedByTarget() != null) {
            try {
                return scriptActionData.getShapesChachedByTarget();
            } catch (Exception ex) {
                Logger.warn("Failed to get list of shapes!");
                return new ArrayList<>();
            }
        }

        //for non-cached shapes
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
                        Logger.warn("Script for boss " + scriptActionData.getEliteEntity().getName() + " has a static ray but no set target2 for the ray!");
                        break;
                    }
                    for (Location location : targets2.getTargetLocations(scriptActionData))
                        if (rayLocationValidator(shapeTargetLocation, location))
                            shapes.add(new StaticRay(zoneBlueprint.isIgnoresSolidBlocks(), zoneBlueprint.getPointRadius(), shapeTargetLocation, location));
                    break;
                case ROTATING_RAY:
                    if (targets2 == null) {
                        Logger.warn("Script for boss " + scriptActionData.getEliteEntity().getName() + " has a static ray but no set target2 for the ray!");
                        break;
                    }
                    for (Location target2Location : targets2.getTargetLocations(scriptActionData))
                        if (rayLocationValidator(shapeTargetLocation, target2Location))
                            shapes.add(new RotatingRay(zoneBlueprint.isIgnoresSolidBlocks(), zoneBlueprint.getPointRadius(), shapeTargetLocation, target2Location, zoneBlueprint.getPitchPreRotation(), zoneBlueprint.getYawPreRotation(), zoneBlueprint.getPitchRotation(), zoneBlueprint.getYawRotation(), zoneBlueprint.getAnimationDuration()));
                    break;
                case TRANSLATING_RAY:
                    if (targets2 == null) {
                        Logger.warn("Script for boss " + scriptActionData.getEliteEntity().getName() + " has a static ray but no set target2 for the ray!");
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
                        if (rayLocationValidator(shapeTargetLocation, location))
                            shapes.add(new TranslatingRay(zoneBlueprint.isIgnoresSolidBlocks(), zoneBlueprint.getPointRadius(), shapeTargetLocation, targetLocationEnd, location, target2LocationEnd, zoneBlueprint.getAnimationDuration()));
                    break;
                case CUBOID:
                    shapes.add(new Cuboid(zoneBlueprint.getX(), zoneBlueprint.getY(), zoneBlueprint.getZ(), zoneBlueprint.getXBorder(), zoneBlueprint.getYBorder(), zoneBlueprint.getZBorder(), shapeTargetLocation));
                    break;
                default:
                    continue;
            }
        }
        return shapes;
    }

    private boolean rayLocationValidator(Location location1, Location location2) {
        return location1 != null && location2 != null && location1.getWorld().equals(location2.getWorld());
    }

    //Get entities in an area based on a filter
    private Collection<LivingEntity> getEntitiesInArea(List<Shape> shapes, TargetType targetType) {
        //Get entities in the world
        Collection<? extends LivingEntity> livingEntities = new ArrayList<>();
        Collection<LivingEntity> validatedEntities = new ArrayList<>();
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

            for (LivingEntity livingEntity : livingEntities) {
                if (targetType.equals(TargetType.ZONE_FULL)) {
                    if (shape.contains(livingEntity)) validatedEntities.add(livingEntity);
                } else if (targetType.equals(TargetType.ZONE_BORDER)) {
                    if (shape.borderContains(livingEntity.getLocation())) validatedEntities.add(livingEntity);
                }
            }

        }

        return validatedEntities;
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

    private class ZoneListenerTask extends BukkitRunnable {
        private final EliteEntity eliteEntity;
        private final ScriptActionData scriptActionData;

        public ZoneListenerTask(EliteEntity eliteEntity, ScriptActionData scriptActionData) {
            this.eliteEntity = eliteEntity;
            this.scriptActionData = scriptActionData;
        }

        @Override
        public void run() {
            if (eliteEntity.getLivingEntity() == null || !eliteEntity.getLivingEntity().isValid()) {
                if (eliteEntity instanceof CustomBossEntity customBossEntity) {
                    if (customBossEntity.getHealth() <= 0)
                        cancel();
                    if (customBossEntity instanceof InstancedBossEntity instancedBossEntity)
                        if (instancedBossEntity.isRemoved())
                            //todo: check if this covers all cases
                            cancel();
                } else
                    //If it's not a custom entity there's no scenario where it should be able to survive an unload here
                    cancel();
                return;
            }
            Collection<LivingEntity> newEntities = getEntitiesInArea(generateShapes(scriptActionData, false), TargetType.ZONE_FULL);
            newEntities.forEach(livingEntity -> {
                if (!entitiesInZone.contains(livingEntity)) ZoneEnterEvent(eliteEntity, livingEntity);
            });
            entitiesInZone.forEach(livingEntity -> {
                if (!newEntities.contains(livingEntity)) ZoneLeaveEvent(eliteEntity, livingEntity);
            });
            entitiesInZone = newEntities;
        }
    }

}
