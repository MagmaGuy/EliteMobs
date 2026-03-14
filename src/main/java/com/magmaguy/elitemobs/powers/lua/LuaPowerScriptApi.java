package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import com.magmaguy.magmacore.util.Logger;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

final class LuaPowerScriptApi {

    private static final String HANDLE_KEY = "__lua_script_handle";

    interface OwnedTaskController {
        int runLater(int ticks, Runnable runnable);

        int runRepeating(int initialDelayTicks, int intervalTicks, Runnable runnable);

        void cancel(int taskId);
    }

    interface CallbackInvoker {
        void invoke(String failureContext, LuaFunction callback, LuaValue... args);
    }

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;
    private final LuaPowerEntityTables entityTables;
    private final OwnedTaskController taskController;
    private final CallbackInvoker callbackInvoker;

    LuaPowerScriptApi(LuaPowerDefinition definition,
                      EliteEntity eliteEntity,
                      LuaPowerSupport support,
                      LuaPowerEntityTables entityTables,
                      OwnedTaskController taskController,
                      CallbackInvoker callbackInvoker) {
        this.definition = definition;
        this.eliteEntity = eliteEntity;
        this.support = support;
        this.entityTables = entityTables;
        this.taskController = taskController;
        this.callbackInvoker = callbackInvoker;
    }

    LuaTable createTable(Event event, LivingEntity directTarget) {
        LuaTable scripts = new LuaTable();
        scripts.set("target", method(scripts, args -> createTargetHandle(args.checktable(1), directTarget, null)));
        scripts.set("zone", method(scripts, args -> createZoneHandle(args.checktable(1))));
        scripts.set("relative_vector", method(scripts, args -> {
            LuaZoneHandle zoneHandle = args.narg() >= 3 ? extractHandle(args.arg(3), LuaZoneHandle.class) : null;
            LuaValue actionLocation = args.narg() >= 2 ? args.arg(2) : LuaValue.NIL;
            return createRelativeVectorHandle(args.checktable(1), actionLocation, directTarget, zoneHandle);
        }));
        scripts.set("damage", method(scripts, args -> {
            double amount = args.narg() >= 2 ? args.arg(2).optdouble(0.0) : 0.0;
            double multiplier = args.narg() >= 3 ? args.arg(3).optdouble(1.0) : 1.0;
            damageTargets(resolveTargets(args.arg1()), amount, multiplier);
            return LuaValue.NIL;
        }));
        scripts.set("push", method(scripts, args -> {
            boolean additive = args.narg() >= 3 && args.arg(3).optboolean(false);
            pushTargets(resolveTargets(args.arg1()), resolveVector(args.arg(2)), additive);
            return LuaValue.NIL;
        }));
        scripts.set("set_facing", method(scripts, args -> {
            setFacing(resolveTargets(args.arg1()), resolveVector(args.arg(2)));
            return LuaValue.NIL;
        }));
        scripts.set("spawn_particles", method(scripts, args -> {
            spawnParticles(args.arg1(), args.arg(2));
            return LuaValue.NIL;
        }));
        return scripts;
    }

    // ── Handle extraction ─────────────────────────────────────────────

    private <T> T extractHandle(LuaValue value, Class<T> type) {
        if (value == null || value.isnil() || !value.istable()) {
            return null;
        }
        LuaValue userdata = value.checktable().rawget(HANDLE_KEY);
        if (userdata.isnil()) {
            return null;
        }
        try {
            return type.cast(userdata.checkuserdata(type));
        } catch (Exception ignored) {
            return null;
        }
    }

    // ── Target handles ────────────────────────────────────────────────

    private LuaTable createTargetHandle(LuaTable specTable,
                                        LivingEntity directTarget,
                                        LuaZoneHandle zoneHandle) {
        LuaTargetResolver resolver = buildTargetResolver(specTable, directTarget, zoneHandle);
        LuaTargetHandle handle = new LuaTargetHandle(resolver);
        LuaTable table = new LuaTable();
        table.rawset(HANDLE_KEY, LuaValue.userdataOf(handle));
        table.set("entities", method(table, args -> toEntityArray(resolver.resolveEntities())));
        table.set("locations", method(table, args -> toLocationArray(resolver.resolveLocations())));
        table.set("first_entity", method(table, args -> {
            Collection<LivingEntity> entities = resolver.resolveEntities();
            return entities.isEmpty() ? LuaValue.NIL : entityTables.createEntityTable(entities.iterator().next());
        }));
        table.set("first_location", method(table, args -> {
            Collection<Location> locations = resolver.resolveLocations();
            return locations.isEmpty() ? LuaValue.NIL : support.toLocationTable(locations.iterator().next());
        }));
        return table;
    }

    // ── Zone handles ──────────────────────────────────────────────────

    private LuaTable createZoneHandle(LuaTable specTable) {
        LuaZoneHandle handle = buildZoneHandle(specTable);
        LuaTable table = new LuaTable();
        table.rawset(HANDLE_KEY, LuaValue.userdataOf(handle));
        table.set("full_target", method(table, args -> {
            double coverage = args.narg() >= 1 && args.arg1().isnumber() ? args.arg1().todouble() : 1.0;
            return zoneLocationsAsTargetHandle(handle, false, coverage);
        }));
        table.set("border_target", method(table, args -> {
            double coverage = args.narg() >= 1 && args.arg1().isnumber() ? args.arg1().todouble() : 1.0;
            return zoneLocationsAsTargetHandle(handle, true, coverage);
        }));
        table.set("full_locations", method(table, args -> {
            double coverage = args.narg() >= 1 && args.arg1().isnumber() ? args.arg1().todouble() : 1.0;
            return toLocationArray(handle.resolveLocations(false, coverage));
        }));
        table.set("border_locations", method(table, args -> {
            double coverage = args.narg() >= 1 && args.arg1().isnumber() ? args.arg1().todouble() : 1.0;
            return toLocationArray(handle.resolveLocations(true, coverage));
        }));
        table.set("full_entities", method(table, args -> toEntityArray(handle.resolveEntities(false))));
        table.set("border_entities", method(table, args -> toEntityArray(handle.resolveEntities(true))));
        table.set("contains", method(table, args -> {
            String mode = args.narg() >= 2 ? args.arg(2).optjstring("full") : "full";
            return LuaValue.valueOf(handle.contains(args.arg1(), mode));
        }));
        table.set("watch", method(table, args -> {
            LuaTable callbacks = args.checktable(1);
            String mode = args.narg() >= 2 ? args.arg(2).optjstring("full") : "full";
            return LuaValue.valueOf(watchZone(handle, callbacks, mode));
        }));
        return table;
    }

    private LuaTable zoneLocationsAsTargetHandle(LuaZoneHandle zoneHandle, boolean border, double coverage) {
        LuaTargetResolver resolver = new LuaTargetResolver() {
            @Override
            Collection<LivingEntity> resolveEntities() {
                return zoneHandle.resolveEntities(border);
            }

            @Override
            Collection<Location> resolveLocations() {
                return zoneHandle.resolveLocations(border, coverage);
            }

            @Override
            boolean needsCentering() {
                return true;
            }
        };
        LuaTargetHandle handle = new LuaTargetHandle(resolver);
        LuaTable table = new LuaTable();
        table.rawset(HANDLE_KEY, LuaValue.userdataOf(handle));
        table.set("entities", method(table, args -> toEntityArray(resolver.resolveEntities())));
        table.set("locations", method(table, args -> toLocationArray(resolver.resolveLocations())));
        table.set("first_entity", method(table, args -> {
            Collection<LivingEntity> entities = resolver.resolveEntities();
            return entities.isEmpty() ? LuaValue.NIL : entityTables.createEntityTable(entities.iterator().next());
        }));
        table.set("first_location", method(table, args -> {
            Collection<Location> locations = resolver.resolveLocations();
            return locations.isEmpty() ? LuaValue.NIL : support.toLocationTable(locations.iterator().next());
        }));
        return table;
    }

    // ── Relative vector handles ───────────────────────────────────────

    private LuaTable createRelativeVectorHandle(LuaTable specTable,
                                                LuaValue actionLocationValue,
                                                LivingEntity directTarget,
                                                LuaZoneHandle zoneHandle) {
        LuaRelativeVectorHandle handle = buildRelativeVectorHandle(specTable, actionLocationValue, directTarget, zoneHandle);
        LuaTable table = new LuaTable();
        table.rawset(HANDLE_KEY, LuaValue.userdataOf(handle));
        table.set("resolve", method(table, args -> support.toVectorTable(handle.resolve())));
        return table;
    }

    // ── Zone watching ─────────────────────────────────────────────────

    private int watchZone(LuaZoneHandle handle, LuaTable callbacks, String mode) {
        LuaValue onEnter = callbacks.get("on_enter");
        LuaValue onLeave = callbacks.get("on_leave");
        Map<UUID, LivingEntity> inside = new HashMap<>();
        boolean border = "border".equalsIgnoreCase(mode);
        final int[] taskId = new int[1];
        taskId[0] = taskController.runRepeating(1, 1, () -> {
            if (!eliteEntity.exists() || eliteEntity.getLivingEntity() == null || !eliteEntity.getLivingEntity().isValid()) {
                taskController.cancel(taskId[0]);
                return;
            }

            Map<UUID, LivingEntity> current = new LinkedHashMap<>();
            Collection<LivingEntity> entities = handle.resolveEntities(border);

            for (LivingEntity livingEntity : entities) {
                if (livingEntity == null || !livingEntity.isValid()) {
                    continue;
                }
                current.put(livingEntity.getUniqueId(), livingEntity);
                if (!inside.containsKey(livingEntity.getUniqueId()) && onEnter.isfunction()) {
                    callbackInvoker.invoke("a zone enter callback", onEnter.checkfunction(), entityTables.createEntityTable(livingEntity));
                }
            }

            for (Map.Entry<UUID, LivingEntity> entry : inside.entrySet()) {
                if (current.containsKey(entry.getKey()) || entry.getValue() == null || !onLeave.isfunction()) {
                    continue;
                }
                callbackInvoker.invoke("a zone leave callback", onLeave.checkfunction(), entityTables.createEntityTable(entry.getValue()));
            }

            inside.clear();
            inside.putAll(current);
        });
        return taskId[0];
    }

    // ── Target resolution (native, no YAML) ──────────────────────────

    private LuaTargetResolver buildTargetResolver(LuaTable specTable,
                                                  LivingEntity directTarget,
                                                  LuaZoneHandle zoneHandle) {
        String targetType = specTable.get("targetType").optjstring("SELF").toUpperCase(Locale.ROOT);
        double range = specTable.get("range").optdouble(20);
        double coverage = specTable.get("coverage").optdouble(1.0);

        return new LuaTargetResolver() {
            @Override
            Collection<LivingEntity> resolveEntities() {
                Location bossLocation = eliteEntity.getLocation();
                if (bossLocation == null || bossLocation.getWorld() == null) return Collections.emptyList();
                return switch (targetType) {
                    case "ALL_PLAYERS" -> new ArrayList<>(Bukkit.getOnlinePlayers());
                    case "WORLD_PLAYERS" -> new ArrayList<>(bossLocation.getWorld().getPlayers());
                    case "NEARBY_PLAYERS" -> bossLocation.getWorld()
                            .getNearbyEntities(bossLocation, range, range, range, e -> e.getType() == EntityType.PLAYER)
                            .stream().map(Player.class::cast).collect(Collectors.toCollection(ArrayList::new));
                    case "NEARBY_MOBS" -> bossLocation.getWorld()
                            .getNearbyEntities(bossLocation, range, range, range,
                                    e -> e.getType() != EntityType.PLAYER && e instanceof LivingEntity &&
                                            !e.getUniqueId().equals(eliteEntity.getUnsyncedLivingEntity().getUniqueId()))
                            .stream().map(LivingEntity.class::cast).collect(Collectors.toCollection(ArrayList::new));
                    case "NEARBY_ELITES" -> bossLocation.getWorld()
                            .getNearbyEntities(bossLocation, range, range, range,
                                    e -> EntityTracker.isEliteMob(e) &&
                                            !e.getUniqueId().equals(eliteEntity.getUnsyncedLivingEntity().getUniqueId()))
                            .stream().map(LivingEntity.class::cast).collect(Collectors.toCollection(ArrayList::new));
                    case "DIRECT_TARGET" -> directTarget != null ? new ArrayList<>(List.of(directTarget)) : Collections.emptyList();
                    case "SELF", "SELF_SPAWN" -> eliteEntity.getUnsyncedLivingEntity() != null
                            ? new ArrayList<>(List.of(eliteEntity.getUnsyncedLivingEntity()))
                            : Collections.emptyList();
                    case "ZONE_FULL" -> zoneHandle != null ? new ArrayList<>(zoneHandle.resolveEntities(false)) : Collections.emptyList();
                    case "ZONE_BORDER" -> zoneHandle != null ? new ArrayList<>(zoneHandle.resolveEntities(true)) : Collections.emptyList();
                    default -> {
                        Logger.warn("Unknown Lua target type: " + targetType + " in " + definition.getFileName());
                        yield Collections.emptyList();
                    }
                };
            }

            @Override
            Collection<Location> resolveLocations() {
                Location bossLocation = eliteEntity.getLocation();
                return switch (targetType) {
                    case "SELF_SPAWN" -> eliteEntity.getSpawnLocation() != null
                            ? new ArrayList<>(List.of(eliteEntity.getSpawnLocation()))
                            : Collections.emptyList();
                    case "LOCATION" -> {
                        String locationStr = specTable.get("location").optjstring(null);
                        if (locationStr != null) {
                            Location loc = com.magmaguy.elitemobs.utils.ConfigurationLocation.serialize(locationStr);
                            if (loc != null && loc.getWorld() == null && bossLocation != null) {
                                loc.setWorld(bossLocation.getWorld());
                            }
                            yield loc != null ? new ArrayList<>(List.of(loc)) : Collections.emptyList();
                        }
                        yield Collections.emptyList();
                    }
                    case "ZONE_FULL" -> zoneHandle != null ? new ArrayList<>(zoneHandle.resolveLocations(false, coverage)) : Collections.emptyList();
                    case "ZONE_BORDER" -> zoneHandle != null ? new ArrayList<>(zoneHandle.resolveLocations(true, coverage)) : Collections.emptyList();
                    default ->
                        resolveEntities().stream().map(LivingEntity::getLocation).collect(Collectors.toCollection(ArrayList::new));
                };
            }

            @Override
            boolean needsCentering() {
                return switch (targetType) {
                    case "ZONE_FULL", "ZONE_BORDER", "LOCATION", "LOCATIONS" -> true;
                    default -> false;
                };
            }
        };
    }

    // ── Zone handle building (native, no YAML) ───────────────────────

    private LuaZoneHandle buildZoneHandle(LuaTable specTable) {
        String filter = specTable.get("filter").optjstring("player");
        // Store the spec for lazy shape building — zones with track=true re-resolve on each access
        return new LuaZoneHandle(specTable, filter);
    }

    /**
     * Builds shapes from a zone spec table. Called lazily on each zone access
     * so that tracked zones follow the boss.
     */
    private List<Shape> buildShapesFromSpec(LuaTable zoneSpec) {
        Location center = resolveZoneTargetLocation(zoneSpec.get("Target"));
        if (center == null) center = eliteEntity.getLocation();
        if (center == null) return Collections.emptyList();

        Location target2Location = resolveZoneTargetLocation(zoneSpec.get("Target2"));

        // Build a shape definition table compatible with LuaPowerSupport.createShape()
        LuaTable shapeTable = new LuaTable();
        String shapeType = zoneSpec.get("shape").optjstring(zoneSpec.get("Shape").optjstring("CYLINDER")).toLowerCase(Locale.ROOT);
        shapeTable.set("kind", shapeType);
        shapeTable.set("origin", support.toLocationTable(center));

        if (target2Location != null) {
            shapeTable.set("destination", support.toLocationTable(target2Location));
        }

        // Copy zone properties to shape table
        copyIfPresent(zoneSpec, shapeTable, "radius");
        copyIfPresent(zoneSpec, shapeTable, "height");
        copyIfPresent(zoneSpec, shapeTable, "borderRadius", "border_radius");
        copyIfPresent(zoneSpec, shapeTable, "pointRadius", "point_radius");
        copyIfPresent(zoneSpec, shapeTable, "x");
        copyIfPresent(zoneSpec, shapeTable, "y");
        copyIfPresent(zoneSpec, shapeTable, "z");
        copyIfPresent(zoneSpec, shapeTable, "xBorder", "x_border");
        copyIfPresent(zoneSpec, shapeTable, "yBorder", "y_border");
        copyIfPresent(zoneSpec, shapeTable, "zBorder", "z_border");
        copyIfPresent(zoneSpec, shapeTable, "animationDuration", "animation_duration");
        copyIfPresent(zoneSpec, shapeTable, "pitchPreRotation", "pitch_pre_rotation");
        copyIfPresent(zoneSpec, shapeTable, "yawPreRotation", "yaw_pre_rotation");
        copyIfPresent(zoneSpec, shapeTable, "pitchRotation", "pitch_rotation");
        copyIfPresent(zoneSpec, shapeTable, "yawRotation", "yaw_rotation");
        copyIfPresent(zoneSpec, shapeTable, "ignoresSolidBlocks", "ignores_solid_blocks");
        copyIfPresent(zoneSpec, shapeTable, "length");
        copyIfPresent(zoneSpec, shapeTable, "thickness");

        Shape shape = support.createShape(shapeTable);
        return shape != null ? new ArrayList<>(List.of(shape)) : Collections.emptyList();
    }

    private void copyIfPresent(LuaTable source, LuaTable dest, String... keys) {
        for (String key : keys) {
            LuaValue value = source.get(key);
            if (!value.isnil()) {
                dest.set(keys[keys.length - 1], value);
                return;
            }
        }
    }

    /**
     * Resolves a zone Target/Target2 table to a Location, handling:
     * - SELF, SELF_SPAWN, DIRECT_TARGET, NEARBY_PLAYERS, NEARBY_MOBS, NEARBY_ELITES
     * - offset as string "x,y,z"
     * - relativeOffset with nested sourceTarget/destinationTarget
     */
    private Location resolveZoneTargetLocation(LuaValue targetValue) {
        if (targetValue == null || targetValue.isnil()) return null;
        if (!targetValue.istable()) return null;
        LuaTable targetTable = targetValue.checktable();

        // If it has x/y/z numbers, it's a raw location
        if (targetTable.get("x").isnumber() && targetTable.get("y").isnumber() && targetTable.get("z").isnumber()) {
            return support.toLocation(targetTable);
        }

        String type = targetTable.get("targetType").optjstring(null);
        if (type == null) return null;

        Location bossLocation = eliteEntity.getLocation();
        if (bossLocation == null) return null;

        double range = targetTable.get("range").optdouble(20);
        Location base = switch (type.toUpperCase(Locale.ROOT)) {
            case "SELF" -> bossLocation.clone();
            case "SELF_SPAWN" -> eliteEntity.getSpawnLocation() != null ? eliteEntity.getSpawnLocation().clone() : null;
            case "NEARBY_PLAYERS" -> {
                Collection<?> nearby = bossLocation.getWorld().getNearbyEntities(
                        bossLocation, range, range, range, e -> e.getType() == EntityType.PLAYER);
                yield nearby.isEmpty() ? null : ((LivingEntity) nearby.iterator().next()).getLocation().clone();
            }
            case "NEARBY_MOBS" -> {
                Collection<?> nearby = bossLocation.getWorld().getNearbyEntities(
                        bossLocation, range, range, range,
                        e -> e instanceof LivingEntity && e.getType() != EntityType.PLAYER
                                && !e.getUniqueId().equals(eliteEntity.getUnsyncedLivingEntity().getUniqueId()));
                yield nearby.isEmpty() ? null : ((LivingEntity) nearby.iterator().next()).getLocation().clone();
            }
            case "NEARBY_ELITES" -> {
                Collection<?> nearby = bossLocation.getWorld().getNearbyEntities(
                        bossLocation, range, range, range,
                        e -> EntityTracker.isEliteMob(e)
                                && !e.getUniqueId().equals(eliteEntity.getUnsyncedLivingEntity().getUniqueId()));
                yield nearby.isEmpty() ? null : ((LivingEntity) nearby.iterator().next()).getLocation().clone();
            }
            default -> null;
        };

        if (base == null) return null;

        // Apply offset (can be string "x,y,z" or table {x=,y=,z=})
        applyOffset(base, targetTable.get("offset"));

        // Apply relativeOffset if present
        LuaValue relativeOffset = targetTable.get("relativeOffset");
        if (!relativeOffset.isnil() && relativeOffset.istable()) {
            Vector relVec = computeRelativeVectorFromTable(relativeOffset.checktable(), base);
            base.add(relVec);
        }

        return base;
    }

    /**
     * Applies an offset to a location. Handles both string "x,y,z" and table {x=,y=,z=} formats.
     */
    private void applyOffset(Location location, LuaValue offsetValue) {
        if (offsetValue == null || offsetValue.isnil()) return;
        if (offsetValue.isstring()) {
            String[] parts = offsetValue.tojstring().split(",");
            if (parts.length >= 3) {
                try {
                    location.add(Double.parseDouble(parts[0].trim()),
                            Double.parseDouble(parts[1].trim()),
                            Double.parseDouble(parts[2].trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (offsetValue.istable()) {
            LuaTable table = offsetValue.checktable();
            location.add(table.get("x").optdouble(0), table.get("y").optdouble(0), table.get("z").optdouble(0));
        }
    }

    // ── Relative vector building (native, no YAML) ───────────────────

    private LuaRelativeVectorHandle buildRelativeVectorHandle(LuaTable specTable,
                                                              LuaValue actionLocationValue,
                                                              LivingEntity directTarget,
                                                              LuaZoneHandle zoneHandle) {
        Location actionLocation = (actionLocationValue == null || actionLocationValue.isnil())
                ? eliteEntity.getLocation()
                : support.toLocation(actionLocationValue);

        boolean normalize = specTable.get("normalize").optboolean(false);
        double multiplier = specTable.get("multiplier").optdouble(1.0);
        Vector offset = parseOffset(specTable.get("offset"));

        // Resolve source and destination from their target sub-tables
        LuaValue sourceTargetValue = specTable.get("SourceTarget");
        if (sourceTargetValue.isnil()) sourceTargetValue = specTable.get("sourceTarget");
        LuaValue destTargetValue = specTable.get("DestinationTarget");
        if (destTargetValue.isnil()) destTargetValue = specTable.get("destinationTarget");

        LuaValue finalSourceTargetValue = sourceTargetValue;
        LuaValue finalDestTargetValue = destTargetValue;

        return new LuaRelativeVectorHandle(normalize, multiplier, offset,
                () -> resolveRelativeVectorLocation(finalSourceTargetValue, actionLocation, directTarget),
                () -> resolveRelativeVectorLocation(finalDestTargetValue, actionLocation, directTarget));
    }

    /**
     * Parses an offset value that can be a string "x,y,z" or a table {x=,y=,z=}.
     */
    private Vector parseOffset(LuaValue offsetValue) {
        if (offsetValue == null || offsetValue.isnil()) return new Vector(0, 0, 0);
        if (offsetValue.isstring()) {
            String[] parts = offsetValue.tojstring().split(",");
            if (parts.length >= 3) {
                try {
                    return new Vector(
                            Double.parseDouble(parts[0].trim()),
                            Double.parseDouble(parts[1].trim()),
                            Double.parseDouble(parts[2].trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (offsetValue.istable()) {
            LuaTable table = offsetValue.checktable();
            return new Vector(table.get("x").optdouble(0), table.get("y").optdouble(0), table.get("z").optdouble(0));
        }
        return new Vector(0, 0, 0);
    }

    /**
     * Resolves a target location for relative vector source/destination, handling
     * SELF, SELF_SPAWN, DIRECT_TARGET, NEARBY_PLAYERS, ACTION_TARGET, and offset.
     */
    private Location resolveRelativeVectorLocation(LuaValue targetValue, Location actionLocation, LivingEntity directTarget) {
        if (targetValue == null || targetValue.isnil()) return actionLocation;
        if (!targetValue.istable()) return actionLocation;
        LuaTable table = targetValue.checktable();
        String type = table.get("targetType").optjstring("ACTION_TARGET").toUpperCase(Locale.ROOT);
        Location bossLocation = eliteEntity.getLocation();
        double range = table.get("range").optdouble(20);

        Location base = switch (type) {
            case "ACTION_TARGET" -> actionLocation != null ? actionLocation.clone() : null;
            case "SELF" -> bossLocation != null ? bossLocation.clone() : null;
            case "SELF_SPAWN" -> eliteEntity.getSpawnLocation() != null ? eliteEntity.getSpawnLocation().clone() : null;
            case "DIRECT_TARGET" -> directTarget != null ? directTarget.getLocation().clone() : (actionLocation != null ? actionLocation.clone() : null);
            case "NEARBY_PLAYERS" -> {
                if (bossLocation == null || bossLocation.getWorld() == null) yield null;
                Collection<?> nearby = bossLocation.getWorld().getNearbyEntities(
                        bossLocation, range, range, range, e -> e.getType() == EntityType.PLAYER);
                yield nearby.isEmpty() ? null : ((LivingEntity) nearby.iterator().next()).getLocation().clone();
            }
            default -> actionLocation != null ? actionLocation.clone() : null;
        };

        if (base != null) {
            applyOffset(base, table.get("offset"));
        }
        return base;
    }

    // ── Action methods ────────────────────────────────────────────────

    private Collection<LivingEntity> resolveTargets(LuaValue value) {
        LuaTargetHandle handle = extractHandle(value, LuaTargetHandle.class);
        return handle == null ? Collections.emptyList() : handle.resolver.resolveEntities();
    }

    private Vector resolveVector(LuaValue value) {
        LuaRelativeVectorHandle handle = extractHandle(value, LuaRelativeVectorHandle.class);
        if (handle != null) {
            return handle.resolve();
        }
        return support.toVector(value);
    }

    private void damageTargets(Collection<LivingEntity> targets, double amount, double multiplier) {
        for (LivingEntity target : targets) {
            if (target instanceof Player) {
                PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter.setSpecialMultiplier(multiplier);
            }
            if (eliteEntity.getLivingEntity() != null) {
                target.damage(amount, eliteEntity.getLivingEntity());
            } else {
                target.damage(amount);
            }
        }
    }

    private void pushTargets(Collection<LivingEntity> targets, Vector velocity, boolean additive) {
        Vector finalVelocity = velocity == null || !isFiniteVector(velocity) ? new Vector(0, 0, 0) : velocity;
        taskController.runLater(1, () -> {
            for (LivingEntity target : targets) {
                if (!target.isValid()) {
                    continue;
                }
                target.setVelocity(additive ? target.getVelocity().add(finalVelocity) : finalVelocity);
            }
        });
    }

    private void setFacing(Collection<LivingEntity> targets, Vector direction) {
        if (direction == null) {
            return;
        }
        for (LivingEntity target : targets) {
            Location location = target.getLocation();
            location.setDirection(direction);
            target.teleport(location);
        }
    }

    private boolean isFiniteVector(Vector vector) {
        return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
    }

    // ── Particle spawning (native, no YAML) ──────────────────────────

    private void spawnParticles(LuaValue targetValue, LuaValue particlesValue) {
        LuaTargetHandle handle = extractHandle(targetValue, LuaTargetHandle.class);
        if (handle == null || particlesValue == null || particlesValue.isnil()) {
            return;
        }

        List<LuaTable> particleDefs = parseParticleDefs(particlesValue);
        if (particleDefs.isEmpty()) {
            return;
        }

        boolean needsCentering = handle.resolver.needsCentering();

        for (Location location : handle.resolver.resolveLocations()) {
            Location targetLocation = needsCentering ? location.clone().add(0.5, 0, 0.5) : location;
            for (LuaTable particleDef : particleDefs) {
                spawnSingleParticle(targetLocation, particleDef);
            }
        }
    }

    private List<LuaTable> parseParticleDefs(LuaValue particlesValue) {
        List<LuaTable> defs = new ArrayList<>();
        if (particlesValue.istable()) {
            LuaTable table = particlesValue.checktable();
            // Check if it's an array of particle tables or a single particle table
            if (table.get(1).istable()) {
                for (int i = 1; i <= table.length(); i++) {
                    LuaValue entry = table.get(i);
                    if (entry.istable()) {
                        defs.add(entry.checktable());
                    }
                }
            } else if (table.get("particle").isstring()) {
                defs.add(table);
            }
        } else if (particlesValue.isstring()) {
            LuaTable single = new LuaTable();
            single.set("particle", particlesValue.tojstring());
            defs.add(single);
        }
        return defs;
    }

    private void spawnSingleParticle(Location location, LuaTable table) {
        if (location == null || location.getWorld() == null) return;

        String particleKey = table.get("particle").optjstring("");
        if (particleKey.isEmpty()) return;

        try {
            Particle particle = Particle.valueOf(particleKey.toUpperCase(Locale.ROOT));
            int amount = table.get("amount").optint(1);
            double x = table.get("x").optdouble(0.01);
            double y = table.get("y").optdouble(0.01);
            double z = table.get("z").optdouble(0.01);
            double speed = table.get("speed").optdouble(0.01);

            // Handle relativeVector within particles
            LuaValue relativeVector = table.get("relativeVector");
            if (relativeVector.isnil()) relativeVector = table.get("relativevector");
            if (relativeVector.istable()) {
                Vector vec = computeRelativeVectorFromTable(relativeVector.checktable(), location);
                amount = 0;
                x = vec.getX();
                y = vec.getY();
                z = vec.getZ();
            }

            if (particle.equals(Particle.DUST)) {
                location.getWorld().spawnParticle(particle, location, amount, x, y, z, speed,
                        new Particle.DustOptions(
                                Color.fromRGB(
                                        table.get("red").optint(255),
                                        table.get("green").optint(255),
                                        table.get("blue").optint(255)),
                                1));
            } else if (particle.equals(Particle.DUST_COLOR_TRANSITION)) {
                location.getWorld().spawnParticle(particle, location, amount, x, y, z, speed,
                        new Particle.DustTransition(
                                Color.fromRGB(
                                        table.get("red").optint(255),
                                        table.get("green").optint(255),
                                        table.get("blue").optint(255)),
                                Color.fromRGB(
                                        table.get("toRed").optint(table.get("tored").optint(255)),
                                        table.get("toGreen").optint(table.get("togreen").optint(255)),
                                        table.get("toBlue").optint(table.get("toblue").optint(255))),
                                1));
            } else if (particle.equals(Particle.WITCH)) {
                location.getWorld().spawnParticle(particle, x, y, z, amount,
                        table.get("red").optint(255),
                        table.get("green").optint(255),
                        table.get("blue").optint(255));
            } else {
                location.getWorld().spawnParticle(particle, location, amount, x, y, z, speed);
            }
        } catch (Exception exception) {
            Logger.warn("Unknown particle " + particleKey + " in Lua power " + definition.getFileName() + ".");
        }
    }

    private Vector computeRelativeVectorFromTable(LuaTable table, Location actionLocation) {
        LuaValue sourceTargetValue = table.get("SourceTarget");
        if (sourceTargetValue.isnil()) sourceTargetValue = table.get("sourceTarget");
        LuaValue destTargetValue = table.get("DestinationTarget");
        if (destTargetValue.isnil()) destTargetValue = table.get("destinationTarget");

        Location source = resolveRelativeVectorLocation(sourceTargetValue, actionLocation, null);
        Location destination = resolveRelativeVectorLocation(destTargetValue, actionLocation, null);

        if (source == null || destination == null || !source.getWorld().equals(destination.getWorld())) {
            return new Vector(0, 0, 0);
        }

        Vector vector = destination.clone().subtract(source).toVector();
        if (table.get("normalize").optboolean(false)) vector.normalize();
        vector.multiply(table.get("multiplier").optdouble(1.0));

        LuaValue offsetValue = table.get("offset");
        if (offsetValue.istable()) {
            LuaTable offsetTable = offsetValue.checktable();
            vector.add(new Vector(
                    offsetTable.get("x").optdouble(0),
                    offsetTable.get("y").optdouble(0),
                    offsetTable.get("z").optdouble(0)));
        }
        return vector;
    }

    // ── Utility methods ───────────────────────────────────────────────

    private LuaTable toEntityArray(Collection<LivingEntity> entities) {
        LuaTable results = new LuaTable();
        int index = 1;
        for (LivingEntity livingEntity : entities) {
            results.set(index++, entityTables.createEntityTable(livingEntity));
        }
        return results;
    }

    private LuaTable toLocationArray(Collection<Location> locations) {
        LuaTable results = new LuaTable();
        int index = 1;
        for (Location location : locations) {
            results.set(index++, support.toLocationTable(location));
        }
        return results;
    }

    // ── Inner types ───────────────────────────────────────────────────

    @RequiredArgsConstructor
    private static final class LuaTargetHandle {
        private final LuaTargetResolver resolver;
    }

    private final class LuaZoneHandle {
        private final LuaTable specTable;
        private final String filter;
        private final boolean tracked;
        private List<Shape> cachedShapes = null;

        private LuaZoneHandle(LuaTable specTable, String filter) {
            this.specTable = specTable;
            this.filter = filter;
            // Check if any target in the spec has track=true
            LuaValue target = specTable.get("Target");
            this.tracked = target.istable() && target.checktable().get("track").optboolean(false);
        }

        private List<Shape> getShapes() {
            if (tracked || cachedShapes == null) {
                cachedShapes = buildShapesFromSpec(specTable);
            }
            return cachedShapes;
        }

        private Collection<Location> resolveLocations(boolean border, double coverage) {
            List<Location> locations = new ArrayList<>();
            for (Shape shape : getShapes()) {
                Collection<Location> shapeLocations = border ? shape.getEdgeLocations() : shape.getLocations();
                if (coverage < 1.0) {
                    for (Location loc : shapeLocations) {
                        if (ThreadLocalRandom.current().nextDouble() <= coverage) {
                            locations.add(loc);
                        }
                    }
                } else {
                    locations.addAll(shapeLocations);
                }
            }
            return locations;
        }

        private Collection<LivingEntity> resolveEntities(boolean border) {
            if (eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
                return Collections.emptyList();
            }
            Collection<LivingEntity> candidates = support.filterEntities(eliteEntity.getLocation().getWorld(), filter);
            List<LivingEntity> result = new ArrayList<>();
            for (LivingEntity candidate : candidates) {
                if (eliteEntity.getLivingEntity() != null &&
                        candidate.getUniqueId().equals(eliteEntity.getLivingEntity().getUniqueId())) {
                    continue;
                }
                for (Shape shape : getShapes()) {
                    if (border ? shape.borderContains(candidate.getLocation()) : shape.contains(candidate)) {
                        result.add(candidate);
                        break;
                    }
                }
            }
            return result;
        }

        private boolean contains(LuaValue locationValue, String mode) {
            Location location = support.toLocation(locationValue);
            if (location == null) {
                return false;
            }
            boolean border = "border".equalsIgnoreCase(mode);
            for (Shape shape : getShapes()) {
                if (border ? shape.borderContains(location) : shape.contains(location)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class LuaRelativeVectorHandle {
        private final boolean normalize;
        private final double multiplier;
        private final Vector offset;
        private final LocationSupplier sourceSupplier;
        private final LocationSupplier destinationSupplier;

        private LuaRelativeVectorHandle(boolean normalize, double multiplier, Vector offset,
                                        LocationSupplier sourceSupplier, LocationSupplier destinationSupplier) {
            this.normalize = normalize;
            this.multiplier = multiplier;
            this.offset = offset;
            this.sourceSupplier = sourceSupplier;
            this.destinationSupplier = destinationSupplier;
        }

        private Vector resolve() {
            Location source = sourceSupplier.get();
            Location destination = destinationSupplier.get();
            if (source == null || destination == null || !source.getWorld().equals(destination.getWorld())) {
                return new Vector(0, 0, 0);
            }
            Vector vector = destination.clone().subtract(source).toVector();
            if (normalize) vector.normalize();
            vector.multiply(multiplier);
            vector.add(offset);
            return vector;
        }
    }

    private static abstract class LuaTargetResolver {
        abstract Collection<LivingEntity> resolveEntities();

        abstract Collection<Location> resolveLocations();

        boolean needsCentering() {
            return false;
        }
    }

    @FunctionalInterface
    private interface LocationSupplier {
        Location get();
    }

    private VarArgFunction method(LuaTable owner, LuaCallback callback) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return callback.invoke(stripMethodSelf(args, owner));
            }
        };
    }

    private Varargs stripMethodSelf(Varargs args, LuaTable owner) {
        if (args.narg() == 0 || !args.arg1().raweq(owner)) {
            return args;
        }
        LuaValue[] stripped = new LuaValue[Math.max(0, args.narg() - 1)];
        for (int index = 2; index <= args.narg(); index++) {
            stripped[index - 2] = args.arg(index);
        }
        return LuaValue.varargsOf(stripped);
    }

    @FunctionalInterface
    private interface LuaCallback {
        Varargs invoke(Varargs args);
    }
}
