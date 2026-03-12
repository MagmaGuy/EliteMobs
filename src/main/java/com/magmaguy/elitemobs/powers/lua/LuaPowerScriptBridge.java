package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.ScriptActionData;
import com.magmaguy.elitemobs.powers.scripts.ScriptParticles;
import com.magmaguy.elitemobs.powers.scripts.ScriptRelativeVector;
import com.magmaguy.elitemobs.powers.scripts.ScriptTargets;
import com.magmaguy.elitemobs.powers.scripts.ScriptZone;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptParticlesBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptRelativeVectorBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptZoneBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class LuaPowerScriptBridge {

    private static final String HANDLE_KEY = "__lua_script_handle";

    interface OwnedTaskController {
        int runLater(int ticks, Runnable runnable);

        int runRepeating(int ticks, Runnable runnable);

        void cancel(int taskId);
    }

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;
    private final LuaPowerEntityTables entityTables;
    private final OwnedTaskController taskController;

    LuaPowerScriptBridge(LuaPowerDefinition definition,
                         EliteEntity eliteEntity,
                         LuaPowerSupport support,
                         LuaPowerEntityTables entityTables,
                         OwnedTaskController taskController) {
        this.definition = definition;
        this.eliteEntity = eliteEntity;
        this.support = support;
        this.entityTables = entityTables;
        this.taskController = taskController;
    }

    LuaTable createTable(Event event, LivingEntity directTarget) {
        LuaTable scripts = new LuaTable();
        scripts.set("target", method(scripts, args -> createTargetHandle(args.checktable(1), event, directTarget, null)));
        scripts.set("zone", method(scripts, args -> createZoneHandle(args.checktable(1), event, directTarget)));
        scripts.set("relative_vector", method(scripts, args -> {
            LuaZoneHandle zoneHandle = args.narg() >= 3 ? extractHandle(args.arg(3), LuaZoneHandle.class) : null;
            LuaValue actionLocation = args.narg() >= 2 ? args.arg(2) : LuaValue.NIL;
            return createRelativeVectorHandle(args.checktable(1), actionLocation, zoneHandle, event, directTarget);
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

    private LuaTable createTargetHandle(LuaTable specTable,
                                        Event event,
                                        LivingEntity directTarget,
                                        LuaZoneHandle zoneHandle) {
        LuaTargetHandle handle = new LuaTargetHandle(buildTargetResolver(specTable, event, directTarget, zoneHandle));
        LuaTable table = new LuaTable();
        table.rawset(HANDLE_KEY, LuaValue.userdataOf(handle));
        table.set("entities", method(table, args -> toEntityArray(handle.resolver.resolveEntities())));
        table.set("locations", method(table, args -> toLocationArray(handle.resolver.resolveLocations())));
        table.set("first_entity", method(table, args -> {
            Collection<LivingEntity> entities = handle.resolver.resolveEntities();
            return entities.isEmpty() ? LuaValue.NIL : entityTables.createEntityTable(entities.iterator().next());
        }));
        table.set("first_location", method(table, args -> {
            Collection<Location> locations = handle.resolver.resolveLocations();
            return locations.isEmpty() ? LuaValue.NIL : support.toLocationTable(locations.iterator().next());
        }));
        return table;
    }

    private LuaTable createZoneHandle(LuaTable specTable, Event event, LivingEntity directTarget) {
        LuaZoneHandle handle = buildZoneHandle(specTable, event, directTarget);
        LuaTable table = new LuaTable();
        table.rawset(HANDLE_KEY, LuaValue.userdataOf(handle));
        table.set("full_target", method(table, args -> {
            LuaValue coverage = args.narg() >= 1 ? args.arg1() : LuaValue.NIL;
            return createTargetHandle(handle.targetSpec("ZONE_FULL", coverage), event, directTarget, handle);
        }));
        table.set("border_target", method(table, args -> {
            LuaValue coverage = args.narg() >= 1 ? args.arg1() : LuaValue.NIL;
            return createTargetHandle(handle.targetSpec("ZONE_BORDER", coverage), event, directTarget, handle);
        }));
        table.set("full_locations", method(table, args -> {
            LuaValue coverage = args.narg() >= 1 ? args.arg1() : LuaValue.NIL;
            return toLocationArray(handle.resolveLocations("ZONE_FULL", coverage));
        }));
        table.set("border_locations", method(table, args -> {
            LuaValue coverage = args.narg() >= 1 ? args.arg1() : LuaValue.NIL;
            return toLocationArray(handle.resolveLocations("ZONE_BORDER", coverage));
        }));
        table.set("full_entities", method(table, args -> toEntityArray(handle.resolveEntities("ZONE_FULL"))));
        table.set("border_entities", method(table, args -> toEntityArray(handle.resolveEntities("ZONE_BORDER"))));
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

    private LuaTable createRelativeVectorHandle(LuaTable specTable,
                                                LuaValue actionLocationValue,
                                                LuaZoneHandle zoneHandle,
                                                Event event,
                                                LivingEntity directTarget) {
        LuaRelativeVectorHandle handle = buildRelativeVectorHandle(specTable, actionLocationValue, zoneHandle, event, directTarget);
        LuaTable table = new LuaTable();
        table.rawset(HANDLE_KEY, LuaValue.userdataOf(handle));
        table.set("resolve", method(table, args -> support.toVectorTable(handle.resolve())));
        return table;
    }

    private int watchZone(LuaZoneHandle handle, LuaTable callbacks, String mode) {
        LuaValue onEnter = callbacks.get("on_enter");
        LuaValue onLeave = callbacks.get("on_leave");
        Map<UUID, LivingEntity> inside = new HashMap<>();
        final int[] taskId = new int[1];
        taskId[0] = taskController.runRepeating(1, () -> {
            if (!eliteEntity.exists() || eliteEntity.getLivingEntity() == null || !eliteEntity.getLivingEntity().isValid()) {
                taskController.cancel(taskId[0]);
                return;
            }

            Map<UUID, LivingEntity> current = new LinkedHashMap<>();
            Collection<LivingEntity> entities = "border".equalsIgnoreCase(mode)
                    ? handle.resolveEntities("ZONE_BORDER")
                    : handle.resolveEntities("ZONE_FULL");

            for (LivingEntity livingEntity : entities) {
                if (livingEntity == null || !livingEntity.isValid()) {
                    continue;
                }
                current.put(livingEntity.getUniqueId(), livingEntity);
                if (!inside.containsKey(livingEntity.getUniqueId()) && onEnter.isfunction()) {
                    onEnter.checkfunction().call(entityTables.createEntityTable(livingEntity));
                }
            }

            for (Map.Entry<UUID, LivingEntity> entry : inside.entrySet()) {
                if (current.containsKey(entry.getKey()) || entry.getValue() == null || !onLeave.isfunction()) {
                    continue;
                }
                onLeave.checkfunction().call(entityTables.createEntityTable(entry.getValue()));
            }

            inside.clear();
            inside.putAll(current);
        });
        return taskId[0];
    }

    private LuaTargetResolver buildTargetResolver(LuaTable specTable,
                                                  Event event,
                                                  LivingEntity directTarget,
                                                  LuaZoneHandle zoneHandle) {
        @SuppressWarnings("unchecked")
        Map<String, Object> spec = (Map<String, Object>) LuaPowerTableConverter.toJavaObject(specTable);
        LuaScriptRuntimeOwner runtimeOwner = zoneHandle == null ? new LuaScriptRuntimeOwner(definition.getFileName()) : zoneHandle.runtimeOwner;
        ScriptTargets targets = new ScriptTargets(new ScriptTargetsBlueprint(spec, "__lua_target__", definition.getFileName()), runtimeOwner);
        return new LuaTargetResolver(targets,
                () -> zoneHandle == null
                        ? new ScriptActionData(eliteEntity, directTarget, targets, event)
                        : zoneHandle.actionData);
    }

    private LuaZoneHandle buildZoneHandle(LuaTable specTable, Event event, LivingEntity directTarget) {
        @SuppressWarnings("unchecked")
        Map<String, Object> zoneSpec = (Map<String, Object>) LuaPowerTableConverter.toJavaObject(specTable);
        LuaScriptRuntimeOwner runtimeOwner = new LuaScriptRuntimeOwner(definition.getFileName());
        ScriptZone scriptZone = new ScriptZone(new ScriptZoneBlueprint(Map.of("Zone", zoneSpec), "__lua_zone__", definition.getFileName()), runtimeOwner);
        runtimeOwner.setScriptZone(scriptZone);
        ScriptTargets selfTarget = new ScriptTargets(new ScriptTargetsBlueprint(Collections.emptyMap(), "__lua_zone__", definition.getFileName()), runtimeOwner);
        ScriptActionData actionData = new ScriptActionData(eliteEntity, directTarget, selfTarget, scriptZone, event);
        if (scriptZone.getZoneBlueprint().getAnimationDuration().getValue() > 0) {
            actionData.setShapesCachedByTarget(scriptZone.generateShapes(actionData, true));
        }
        return new LuaZoneHandle(runtimeOwner, scriptZone, actionData);
    }

    private LuaRelativeVectorHandle buildRelativeVectorHandle(LuaTable specTable,
                                                              LuaValue actionLocationValue,
                                                              LuaZoneHandle zoneHandle,
                                                              Event event,
                                                              LivingEntity directTarget) {
        @SuppressWarnings("unchecked")
        Map<String, Object> spec = (Map<String, Object>) LuaPowerTableConverter.toJavaObject(specTable);
        LuaScriptRuntimeOwner runtimeOwner = zoneHandle == null ? new LuaScriptRuntimeOwner(definition.getFileName()) : zoneHandle.runtimeOwner;
        Location actionLocation = actionLocationValue == null || actionLocationValue.isnil()
                ? eliteEntity.getLocation()
                : support.toLocation(actionLocationValue);
        ScriptRelativeVector vector = new ScriptRelativeVector(
                new ScriptRelativeVectorBlueprint("__lua_vector__", definition.getFileName(), spec),
                runtimeOwner,
                actionLocation);
        ScriptTargets selfTarget = new ScriptTargets(new ScriptTargetsBlueprint(Collections.emptyMap(), "__lua_vector__", definition.getFileName()), runtimeOwner);
        return new LuaRelativeVectorHandle(vector,
                () -> zoneHandle == null
                        ? new ScriptActionData(eliteEntity, directTarget, selfTarget, event)
                        : zoneHandle.actionData);
    }

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

    private void spawnParticles(LuaValue targetValue, LuaValue particlesValue) {
        LuaTargetHandle handle = extractHandle(targetValue, LuaTargetHandle.class);
        if (handle == null || particlesValue == null || particlesValue.isnil()) {
            return;
        }

        ScriptParticles scriptParticles = buildScriptParticles(particlesValue);
        if (scriptParticles == null) {
            return;
        }

        ScriptActionData actionData = handle.resolver.createActionData();
        boolean needsCentering = switch (handle.resolver.getTargetType()) {
            case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER,
                    LOCATION, LOCATIONS, LANDING_LOCATION -> true;
            default -> false;
        };

        for (Location location : handle.resolver.resolveLocations(actionData)) {
            Location targetLocation = needsCentering ? location.clone().add(0.5, 0, 0.5) : location;
            scriptParticles.visualize(actionData, targetLocation, handle.resolver.getRuntimeOwner());
        }
    }

    private ScriptParticles buildScriptParticles(LuaValue particlesValue) {
        Object rawParticles = LuaPowerTableConverter.toJavaObject(particlesValue);
        List<Map<?, ?>> particleEntries = new java.util.ArrayList<>();

        if (rawParticles instanceof List<?> list) {
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> map) {
                    particleEntries.add(map);
                }
            }
        } else if (rawParticles instanceof Map<?, ?> map) {
            particleEntries.add(map);
        } else if (particlesValue.isstring()) {
            particleEntries.add(Map.of("particle", particlesValue.tojstring()));
        }

        if (particleEntries.isEmpty()) {
            return null;
        }

        return new ScriptParticles(new ScriptParticlesBlueprint(particleEntries, "__lua_particles__", definition.getFileName()));
    }

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

    @RequiredArgsConstructor
    private static final class LuaTargetHandle {
        private final LuaTargetResolver resolver;
    }

    private final class LuaZoneHandle {
        private final LuaScriptRuntimeOwner runtimeOwner;
        private final ScriptZone scriptZone;
        private final ScriptActionData actionData;

        private LuaZoneHandle(LuaScriptRuntimeOwner runtimeOwner, ScriptZone scriptZone, ScriptActionData actionData) {
            this.runtimeOwner = runtimeOwner;
            this.scriptZone = scriptZone;
            this.actionData = actionData;
        }

        private LuaTable targetSpec(String targetType, LuaValue coverageValue) {
            LuaTable spec = new LuaTable();
            spec.set("targetType", targetType);
            if (coverageValue != null && coverageValue.isnumber()) {
                spec.set("coverage", coverageValue);
            }
            return spec;
        }

        private Collection<Location> resolveLocations(String targetType, LuaValue coverageValue) {
            Map<String, Object> targetSpec = new LinkedHashMap<>();
            targetSpec.put("targetType", targetType);
            if (coverageValue != null && coverageValue.isnumber()) {
                targetSpec.put("coverage", coverageValue.todouble());
            }
            ScriptTargets targets = new ScriptTargets(new ScriptTargetsBlueprint(targetSpec, "__lua_zone_target__", runtimeOwner.getFileName()), runtimeOwner);
            return targets.getTargetLocations(actionData);
        }

        private Collection<LivingEntity> resolveEntities(String targetType) {
            Map<String, Object> targetSpec = new LinkedHashMap<>();
            targetSpec.put("targetType", targetType);
            ScriptTargets targets = new ScriptTargets(new ScriptTargetsBlueprint(targetSpec, "__lua_zone_target__", runtimeOwner.getFileName()), runtimeOwner);
            return targets.getTargetEntities(actionData);
        }

        private boolean contains(LuaValue locationValue, String mode) {
            Location location = support.toLocation(locationValue);
            if (location == null) {
                return false;
            }
            boolean border = "border".equalsIgnoreCase(mode);
            boolean staticZone = scriptZone.getZoneBlueprint().getAnimationDuration().getValue() == 0;
            for (Shape shape : scriptZone.generateShapes(actionData, staticZone)) {
                if (border ? shape.borderContains(location) : shape.contains(location)) {
                    return true;
                }
            }
            return false;
        }
    }

    @RequiredArgsConstructor
    private static final class LuaRelativeVectorHandle {
        private final ScriptRelativeVector vector;
        private final ActionDataFactory actionDataFactory;

        private Vector resolve() {
            return vector.getVector(actionDataFactory.create());
        }
    }

    @RequiredArgsConstructor
    private static final class LuaTargetResolver {
        private final ScriptTargets scriptTargets;
        private final ActionDataFactory actionDataFactory;

        private Collection<LivingEntity> resolveEntities() {
            return resolveEntities(createActionData());
        }

        private Collection<LivingEntity> resolveEntities(ScriptActionData actionData) {
            return scriptTargets.getTargetEntities(actionData);
        }

        private Collection<Location> resolveLocations() {
            return resolveLocations(createActionData());
        }

        private Collection<Location> resolveLocations(ScriptActionData actionData) {
            return scriptTargets.getTargetLocations(actionData);
        }

        private ScriptActionData createActionData() {
            return actionDataFactory.create();
        }

        private TargetType getTargetType() {
            return scriptTargets.getTargetBlueprint().getTargetType();
        }

        private LuaScriptRuntimeOwner getRuntimeOwner() {
            return (LuaScriptRuntimeOwner) scriptTargets.getRuntimeOwner();
        }
    }

    @FunctionalInterface
    private interface ActionDataFactory {
        ScriptActionData create();
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
