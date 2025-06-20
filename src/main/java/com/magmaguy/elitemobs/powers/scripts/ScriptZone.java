package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.ScriptZoneEnterEvent;
import com.magmaguy.elitemobs.api.ScriptZoneLeaveEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptZoneBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.shapes.*;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a zone defined in an EliteScript, handling shape generation, entity retrieval,
 * and triggering events when entities enter or leave the zone.
 */
public class ScriptZone {

    /** The blueprint defining this script zone. */
    @Getter
    private final ScriptZoneBlueprint zoneBlueprint;

    /** The primary targets of this zone, derived from the blueprint. */
    private final ScriptTargets targets;

    /** Indicates whether the zone is valid (i.e., has at least one target). */
    @Getter
    private final boolean isValid;

    /** The final targets of this zone, if specified in the blueprint. */
    private ScriptTargets finalTargets = null;

    /** Secondary targets, if specified (used for rays and other shapes needing two targets). */
    private ScriptTargets targets2 = null;

    /** The final secondary targets, if specified. */
    private ScriptTargets finalTargets2 = null;

    /** Whether this zone should listen for entities entering and leaving. */
    @Setter
    private boolean zoneListener = false;

    /** The entities currently within the zone, used to track enter and leave events. */
    private Set<LivingEntity> entitiesInZone;

    /**
     * Constructs a new ScriptZone based on the given blueprint and script.
     *
     * @param zoneBlueprint The blueprint defining the zone's properties.
     * @param eliteScript   The script associated with this zone.
     */
    public ScriptZone(ScriptZoneBlueprint zoneBlueprint, EliteScript eliteScript) {
        this.zoneBlueprint = zoneBlueprint;
        this.targets = new ScriptTargets(zoneBlueprint.getTarget(), eliteScript);

        if (zoneBlueprint.getFinalTarget() != null) {
            this.finalTargets = new ScriptTargets(zoneBlueprint.getFinalTarget(), eliteScript);
        }

        if (zoneBlueprint.getTarget2() != null) {
            this.targets2 = new ScriptTargets(zoneBlueprint.getTarget2(), eliteScript);
        }

        if (zoneBlueprint.getFinalTarget2() != null) {
            this.finalTargets2 = new ScriptTargets(zoneBlueprint.getFinalTarget2(), eliteScript);
        }

        this.isValid = zoneBlueprint.getTarget() != null;
    }

    private ZoneListenerTask zoneListenerTask = null;

    /**
     * Starts a zone listener that monitors entities entering or leaving the zone.
     *
     * @param eliteEntity The EliteEntity associated with this zone.
     */
    public void startZoneListener(EliteEntity eliteEntity) {
        if (!zoneListener) return;
        if (zoneListenerTask != null) zoneListenerTask.cancel();

        entitiesInZone = new HashSet<>();
        ScriptActionData scriptActionData = new ScriptActionData(eliteEntity, targets, this);
        zoneListenerTask = new ZoneListenerTask(eliteEntity, scriptActionData);
        zoneListenerTask.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
    }

    /**
     * Triggers a ScriptZoneEnterEvent when a living entity enters the zone.
     *
     * @param eliteEntity  The EliteEntity associated with the zone.
     * @param livingEntity The living entity that entered the zone.
     */
    public void zoneEnterEvent(EliteEntity eliteEntity, LivingEntity livingEntity) {
        new EventCaller(new ScriptZoneEnterEvent(eliteEntity, livingEntity));
    }

    /**
     * Triggers a ScriptZoneLeaveEvent when a living entity leaves the zone.
     *
     * @param eliteEntity  The EliteEntity associated with the zone.
     * @param livingEntity The living entity that left the zone.
     */
    public void zoneLeaveEvent(EliteEntity eliteEntity, LivingEntity livingEntity) {
        new EventCaller(new ScriptZoneLeaveEvent(eliteEntity, livingEntity));
    }

    /**
     * Retrieves living entities within the zone based on the provided script action data and target blueprint.
     *
     * @param scriptActionData              The data for the current script action.
     * @param blueprintFromRequestingTarget The target blueprint requesting entities.
     * @return A collection of living entities within the zone.
     */
    protected Collection<LivingEntity> getZoneEntities(ScriptActionData scriptActionData, ScriptTargetsBlueprint blueprintFromRequestingTarget) {
        try {
            switch (blueprintFromRequestingTarget.getTargetType()) {
                case ZONE_FULL, ZONE_BORDER:
                    return getEntitiesInArea(generateShapes(scriptActionData, false), blueprintFromRequestingTarget.getTargetType());
                case INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER:
                    return getEntitiesInArea(generateShapes(scriptActionData.getInheritedScriptActionData(), false), blueprintFromRequestingTarget.getTargetType());
                default:
                    Logger.warn("Couldn't parse target type '" + blueprintFromRequestingTarget.getTargetType() + "' in script zone.");
                    return Collections.emptyList();
            }
        } catch (Exception e) {
            Logger.warn("Error retrieving zone entities: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves locations within the zone based on the provided script action data and target.
     *
     * @param scriptActionData The data for the current script action.
     * @param actionTarget     The action target requesting locations.
     * @return A collection of locations within the zone.
     */
    protected Collection<Location> getZoneLocations(ScriptActionData scriptActionData, ScriptTargets actionTarget) {
        try {
            switch (actionTarget.getTargetBlueprint().getTargetType()) {
                case ZONE_FULL:
                    return consolidateLists(generateShapes(scriptActionData, false).stream().map(Shape::getLocations).collect(Collectors.toSet()));
                case ZONE_BORDER:
                    return consolidateLists(generateShapes(scriptActionData, false).stream().map(Shape::getEdgeLocations).collect(Collectors.toSet()));
                case INHERIT_SCRIPT_ZONE_FULL:
                    return consolidateLists(generateShapes(scriptActionData.getInheritedScriptActionData(), false).stream().map(Shape::getLocations).collect(Collectors.toSet()));
                case INHERIT_SCRIPT_ZONE_BORDER:
                    return consolidateLists(generateShapes(scriptActionData.getInheritedScriptActionData(), false).stream().map(Shape::getEdgeLocations).collect(Collectors.toSet()));
                default:
                    Logger.warn("Couldn't parse target type '" + actionTarget.getTargetBlueprint().getTargetType() + "' in script zone.");
                    return Collections.emptyList();
            }
        } catch (Exception e) {
            Logger.warn("Error retrieving zone locations: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Consolidates a collection of location collections into a single collection.
     *
     * @param originalLocations The original collection of location collections.
     * @return A consolidated collection of locations.
     */
    private Collection<Location> consolidateLists(Collection<Collection<Location>> originalLocations) {
        Collection<Location> parsedLocations = new ArrayList<>();
        originalLocations.forEach(parsedLocations::addAll);
        return parsedLocations;
    }

    /**
     * Generates shapes that define the zone based on the script action data.
     *
     * @param scriptActionData The data for the current script action.
     * @param force            If true, forces regeneration of shapes (used for animated zones).
     * @return A list of generated shapes.
     */
    public List<Shape> generateShapes(ScriptActionData scriptActionData, boolean force) {
        try {
            // Use cached shapes if available and not forcing regeneration
            if (!force && scriptActionData.getShapesCachedByTarget() != null) {
                return scriptActionData.getShapesCachedByTarget();
            }

            List<Shape> shapes = new ArrayList<>();

            // Generate shapes based on the targets of the zone
            for (Location shapeTargetLocation : targets.getTargetLocations(scriptActionData)) {
                switch (zoneBlueprint.getShapeTypeEnum()) {
                    case CYLINDER -> shapes.add(new Cylinder(shapeTargetLocation, zoneBlueprint.getRadius().getValue(), zoneBlueprint.getHeight().getValue(), zoneBlueprint.getBorderRadius().getValue()));
                    case SPHERE -> shapes.add(new Sphere(zoneBlueprint.getRadius().getValue(), shapeTargetLocation, zoneBlueprint.getBorderRadius().getValue()));
                    case DOME -> shapes.add(new Dome(zoneBlueprint.getRadius().getValue(), shapeTargetLocation, zoneBlueprint.getBorderRadius().getValue()));
                    case CONE -> {
                        if (targets2 == null) {
                            Logger.warn("Script for boss '"
                                    + scriptActionData.getEliteEntity().getName()
                                    + "' has a CONE but no set target2 for the cone!");
                            continue;
                        }

                        List<Location> target2Locations = new ArrayList<>(targets2.getTargetLocations(scriptActionData));
                        if (target2Locations.isEmpty()) {
                            continue;
                        }

                        Location secondPoint = target2Locations.get(0);
                        shapes.add(new Cone(
                                shapeTargetLocation,
                                secondPoint,
                                zoneBlueprint.getRadius().getValue(),
                                zoneBlueprint.getBorderRadius().getValue()
                        ));
                    }

                    case STATIC_RAY -> {
                        if (targets2 == null) {
                            Logger.warn("Script for boss '" + scriptActionData.getEliteEntity().getName() + "' has a STATIC_RAY but no set target2 for the ray!");
                            continue;
                        }
                        for (Location location : targets2.getTargetLocations(scriptActionData)) {
                            if (rayLocationValidator(shapeTargetLocation, location)) {
                                shapes.add(new StaticRay(zoneBlueprint.isIgnoresSolidBlocks(), zoneBlueprint.getPointRadius().getValue(), shapeTargetLocation, location));
                            }
                        }
                    }
                    case ROTATING_RAY -> {
                        if (targets2 == null) {
                            Logger.warn("Script for boss '" + scriptActionData.getEliteEntity().getName() + "' has a ROTATING_RAY but no set target2 for the ray!");
                            continue;
                        }
                        for (Location target2Location : targets2.getTargetLocations(scriptActionData)) {
                            if (rayLocationValidator(shapeTargetLocation, target2Location)) {
                                shapes.add(new RotatingRay(
                                        zoneBlueprint.isIgnoresSolidBlocks(),
                                        zoneBlueprint.getPointRadius().getValue(),
                                        shapeTargetLocation,
                                        target2Location,
                                        zoneBlueprint.getPitchPreRotation().getValue(),
                                        zoneBlueprint.getYawPreRotation().getValue(),
                                        zoneBlueprint.getPitchRotation().getValue(),
                                        zoneBlueprint.getYawRotation().getValue(),
                                        zoneBlueprint.getAnimationDuration().getValue()
                                ));
                            }
                        }
                    }
                    case TRANSLATING_RAY -> {
                        if (targets2 == null) {
                            Logger.warn("Script for boss '" + scriptActionData.getEliteEntity().getName() + "' has a TRANSLATING_RAY but no set target2 for the ray!");
                            continue;
                        }
                        Location targetLocationEnd = null;
                        if (finalTargets != null) {
                            List<Location> finalTargetsList = new ArrayList<>(finalTargets.getTargetLocations(scriptActionData));
                            if (!finalTargetsList.isEmpty()) {
                                targetLocationEnd = finalTargetsList.get(0);
                            }
                        }
                        Location target2LocationEnd = null;
                        if (finalTargets2 != null) {
                            List<Location> finalTargetsList = new ArrayList<>(finalTargets2.getTargetLocations(scriptActionData));
                            if (!finalTargetsList.isEmpty()) {
                                target2LocationEnd = finalTargetsList.get(0);
                            }
                        }
                        for (Location location : targets2.getTargetLocations(scriptActionData)) {
                            if (rayLocationValidator(shapeTargetLocation, location)) {
                                shapes.add(new TranslatingRay(
                                        zoneBlueprint.isIgnoresSolidBlocks(),
                                        zoneBlueprint.getPointRadius().getValue(),
                                        shapeTargetLocation,
                                        targetLocationEnd,
                                        location,
                                        target2LocationEnd,
                                        zoneBlueprint.getAnimationDuration().getValue()
                                ));
                            }
                        }
                    }
                    case CUBOID -> shapes.add(new Cuboid(
                            zoneBlueprint.getX().getValue(),
                            zoneBlueprint.getY().getValue(),
                            zoneBlueprint.getZ().getValue(),
                            zoneBlueprint.getXBorder().getValue(),
                            zoneBlueprint.getYBorder().getValue(),
                            zoneBlueprint.getZBorder().getValue(),
                            shapeTargetLocation
                    ));
                    default -> Logger.warn("Unsupported shape type '" + zoneBlueprint.getShapeTypeEnum() + "' in script zone.");
                }
            }
            return shapes;
        } catch (Exception e) {
            Logger.warn("Error generating shapes: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Validates that two locations are suitable for creating a ray shape.
     *
     * @param location1 The starting location.
     * @param location2 The ending location.
     * @return True if both locations are valid and in the same world; false otherwise.
     */
    private boolean rayLocationValidator(Location location1, Location location2) {
        return location1 != null && location2 != null && location1.getWorld().equals(location2.getWorld());
    }

    /**
     * Retrieves living entities within the given shapes based on the target type.
     *
     * @param shapes     The shapes defining the zone.
     * @param targetType The target type (ZONE_FULL or ZONE_BORDER).
     * @return A collection of living entities within the zone.
     */
    private Collection<LivingEntity> getEntitiesInArea(List<Shape> shapes, TargetType targetType) {
        Set<LivingEntity> validatedEntities = new HashSet<>();

        for (Shape shape : shapes) {
            Collection<? extends LivingEntity> livingEntities = zoneBlueprint.getFilter() != null ? switch (zoneBlueprint.getFilter()) {
                case PLAYER -> filterByPlayer(shape.getCenter());
                case ELITE -> filterByElite(shape.getCenter());
                case LIVING -> filterByLiving(shape.getCenter());
            } : filterByLiving(shape.getCenter());

            for (LivingEntity livingEntity : livingEntities) {
                boolean contains = targetType.equals(TargetType.ZONE_FULL) ? shape.contains(livingEntity) : shape.borderContains(livingEntity.getLocation());
                if (contains) {
                    validatedEntities.add(livingEntity);
                }
            }
        }
        return validatedEntities;
    }

    /**
     * Filters players in the world at the given location.
     *
     * @param center The center location.
     * @return A collection of players.
     */
    private Collection<LivingEntity> filterByPlayer(Location center) {
        World world = center.getWorld();
        if (world != null) {
            return new ArrayList<>(world.getPlayers());
        } else {
            Logger.warn("World is null in filterByPlayer.");
            return Collections.emptyList();
        }
    }

    /**
     * Filters elite entities in the world at the given location.
     *
     * @param center The center location.
     * @return A collection of elite entities.
     */
    private Collection<LivingEntity> filterByElite(Location center) {
        World world = center.getWorld();
        if (world != null) {
            return world.getEntities().stream()
                    .filter(entity -> entity instanceof LivingEntity)
                    .filter(entity -> EntityTracker.getEliteMobEntity(entity) != null)
                    .map(entity -> (LivingEntity) entity)
                    .collect(Collectors.toList());
        } else {
            Logger.warn("World is null in filterByElite.");
            return Collections.emptyList();
        }
    }

    /**
     * Filters all living entities in the world at the given location.
     *
     * @param center The center location.
     * @return A collection of living entities.
     */
    private Collection<LivingEntity> filterByLiving(Location center) {
        World world = center.getWorld();
        if (world != null) {
            return world.getLivingEntities();
        } else {
            Logger.warn("World is null in filterByLiving.");
            return Collections.emptyList();
        }
    }

    /**
     * A task that listens for entities entering or leaving the zone.
     */
    private class ZoneListenerTask extends BukkitRunnable {
        private final EliteEntity eliteEntity;
        private final ScriptActionData scriptActionData;

        public ZoneListenerTask(EliteEntity eliteEntity, ScriptActionData scriptActionData) {
            this.eliteEntity = eliteEntity;
            this.scriptActionData = scriptActionData;
        }

        @Override
        public void run() {
            try {
                if (eliteEntity.getLivingEntity() == null || !eliteEntity.getLivingEntity().isValid()) {
                    // Cancel task if the elite entity is no longer valid
                    cancel();
                    return;
                }

                // Retrieve current entities in the zone
                Collection<LivingEntity> newEntities = getEntitiesInArea(generateShapes(scriptActionData, false), TargetType.ZONE_FULL);

                // Trigger enter events for entities that have just entered
                for (LivingEntity livingEntity : newEntities) {
                    if (!entitiesInZone.contains(livingEntity)) {
                        zoneEnterEvent(eliteEntity, livingEntity);
                    }
                }

                // Trigger leave events for entities that have just left
                for (LivingEntity livingEntity : entitiesInZone) {
                    if (!newEntities.contains(livingEntity)) {
                        zoneLeaveEvent(eliteEntity, livingEntity);
                    }
                }

                // Update the set of entities currently in the zone
                entitiesInZone = new HashSet<>(newEntities);

            } catch (Exception e) {
                Logger.warn("Error in ZoneListenerTask: " + e.getMessage());
                cancel();
            }
        }
    }
}
