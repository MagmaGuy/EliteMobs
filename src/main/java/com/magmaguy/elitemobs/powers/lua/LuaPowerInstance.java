package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.ScriptAction;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.utils.shapes.*;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.*;

public class LuaPowerInstance {

    private final LuaPowerDefinition definition;
    @Getter
    private final EliteEntity eliteEntity;
    private final LuaTable scriptTable;
    private final LuaTable stateTable = new LuaTable();
    private final LuaPowerSupport support;
    private final LuaPowerEntityTables entityTables;
    private final LuaPowerContextTables contextTables;
    private final LuaPowerScriptBridge scriptBridge;
    private final Map<Integer, OwnedTask> ownedTasks = new LinkedHashMap<>();
    private final List<ZoneWatch> zoneWatches = new ArrayList<>();
    private Integer clockTaskId = null;
    private boolean closed = false;

    public LuaPowerInstance(LuaPowerDefinition definition, EliteEntity eliteEntity) {
        this.definition = definition;
        this.eliteEntity = eliteEntity;
        this.support = new LuaPowerSupport(definition, eliteEntity);
        this.entityTables = new LuaPowerEntityTables(definition, eliteEntity, support);
        this.contextTables = new LuaPowerContextTables(definition, eliteEntity, support, entityTables);
        this.scriptBridge = new LuaPowerScriptBridge(definition, eliteEntity, support, entityTables, new LuaPowerScriptBridge.OwnedTaskController() {
            @Override
            public int runLater(int ticks, Runnable runnable) {
                return ownLaterTask(ticks, runnable);
            }

            @Override
            public int runRepeating(int ticks, Runnable runnable) {
                return ownRepeatingTask(ticks, runnable);
            }

            @Override
            public void cancel(int taskId) {
                cancelOwnedTask(taskId);
            }
        });
        this.scriptTable = definition.instantiate();
        updateClockRegistration();
    }

    public boolean isClosed() {
        return closed;
    }

    public void shutdown() {
        if (closed) return;
        closed = true;
        if (clockTaskId != null) {
            GameClock.cancel(clockTaskId);
            clockTaskId = null;
        }
        for (OwnedTask task : new ArrayList<>(ownedTasks.values())) {
            task.cancel();
        }
        ownedTasks.clear();
        zoneWatches.clear();
    }

    public void onTick() {
        if (closed) return;
        if (!eliteEntity.exists()) {
            shutdown();
            return;
        }
        tickZoneWatches();
        if (definition.supportsHook(LuaPowerHook.ON_TICK)) {
            handleEvent(LuaPowerHook.ON_TICK, null, null, null);
        }
    }

    public void handleEvent(LuaPowerHook hook, Event event, LivingEntity directTarget, LivingEntity eventActor) {
        if (closed || hook == null || !definition.getHooks().contains(hook)) return;

        LuaValue function = scriptTable.get(hook.getKey());
        if (!function.isfunction()) return;

        long startNanos = System.nanoTime();
        try {
            function.checkfunction().call(buildContext(event, directTarget, eventActor));
        } catch (Exception exception) {
            Logger.warn("Lua power " + definition.getFileName() + " failed while handling " + hook.getKey() + ".");
            exception.printStackTrace();
            shutdown();
            return;
        }

        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
        if (elapsedMillis > 50) {
            Logger.warn("Lua power " + definition.getFileName() + " exceeded the 50ms execution budget on " + hook.getKey() + " and was disabled.");
            shutdown();
        }
    }

    private LuaValue buildContext(Event event, LivingEntity directTarget, LivingEntity eventActor) {
        LuaTable context = new LuaTable();
        context.set("state", stateTable);
        Player contextPlayer = resolveContextPlayer(directTarget, eventActor);

        LuaTable metatable = new LuaTable();
        metatable.set("__index", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable owner = args.arg1().checktable();
                LuaValue keyValue = args.arg(2);
                if (!keyValue.isstring()) {
                    return LuaValue.NIL;
                }

                String key = keyValue.tojstring();
                LuaValue resolved = resolveContextValue(owner, key, event, directTarget, eventActor, contextPlayer);
                if (!resolved.isnil()) {
                    owner.rawset(key, resolved);
                }
                return resolved;
            }
        });
        context.setmetatable(metatable);
        return context;
    }

    private LuaValue resolveContextValue(LuaTable context,
                                         String key,
                                         Event event,
                                         LivingEntity directTarget,
                                         LivingEntity eventActor,
                                         Player contextPlayer) {
        return switch (key) {
            case "boss" -> entityTables.createBossTable();
            case "log" -> createLogTable();
            case "cooldowns" -> createCooldownTable();
            case "scheduler" -> createSchedulerTable();
            case "players" -> contextTables.createPlayersTable(eventActor);
            case "entities" -> contextTables.createEntitiesTable(directTarget);
            case "vectors" -> contextTables.createVectorsTable();
            case "world" -> contextTables.createWorldTable();
            case "zones" -> createZonesTable();
            case "player" -> contextPlayer == null ? LuaValue.NIL : entityTables.createPlayerTable(contextPlayer);
            case "event" -> event == null ? LuaValue.NIL : entityTables.createEventTable(event);
            case "script" -> scriptBridge.createTable(event, directTarget);
            default -> LuaValue.NIL;
        };
    }

    private Player resolveContextPlayer(LivingEntity directTarget, LivingEntity eventActor) {
        if (directTarget instanceof Player player) {
            return player;
        }
        if (eventActor instanceof Player player) {
            return player;
        }
        return null;
    }

    private LuaTable createLogTable() {
        LuaTable log = new LuaTable();
        log.set("info", logFunction(log, message -> Logger.info("[LuaPower] " + message)));
        log.set("warn", logFunction(log, Logger::warn));
        log.set("debug", logFunction(log, message -> Logger.info("[LuaPowerDebug] " + message)));
        return log;
    }

    private LuaTable createCooldownTable() {
        LuaTable cooldowns = new LuaTable();
        cooldowns.set("local_ready", method(cooldowns, args ->
                LuaValue.valueOf(eliteEntity.isSharedCooldownReady(resolveCooldownKey(args, 1)))));
        cooldowns.set("local_remaining", method(cooldowns, args ->
                LuaValue.valueOf(eliteEntity.getSharedCooldownRemainingTicks(resolveCooldownKey(args, 1)))));
        cooldowns.set("check_local", method(cooldowns, args -> {
            String key = resolveCooldownKey(args, 1);
            int duration = args.checkint(2);
            if (!eliteEntity.isSharedCooldownReady(key)) {
                return LuaValue.FALSE;
            }
            eliteEntity.setSharedCooldown(key, duration);
            return LuaValue.TRUE;
        }));
        cooldowns.set("check", cooldowns.get("check_local"));
        cooldowns.set("set_local", method(cooldowns, args -> {
            eliteEntity.setSharedCooldown(resolveCooldownKey(args, 2), args.checklong(1));
            return LuaValue.NIL;
        }));
        cooldowns.set("set", method(cooldowns, args -> {
            eliteEntity.setSharedCooldown(resolveCooldownKey(args, 1), args.checklong(2));
            return LuaValue.NIL;
        }));
        cooldowns.set("global_ready", method(cooldowns, args -> LuaValue.valueOf(!eliteEntity.isInCooldown())));
        cooldowns.set("set_global", method(cooldowns, args -> {
            eliteEntity.doGlobalPowerCooldown(args.checkint(1));
            return LuaValue.NIL;
        }));
        return cooldowns;
    }

    private LuaTable createSchedulerTable() {
        LuaTable scheduler = new LuaTable();
        scheduler.set("run_after", method(scheduler, args -> {
            int ticks = args.checkint(1);
            LuaFunction callback = args.checkfunction(2);
            return LuaValue.valueOf(ownLaterTask(ticks, callback));
        }));
        scheduler.set("run_delayed", scheduler.get("run_after"));
        scheduler.set("run_every", method(scheduler, args -> {
            int ticks = args.checkint(1);
            LuaFunction callback = args.checkfunction(2);
            return LuaValue.valueOf(ownRepeatingTask(ticks, callback));
        }));
        scheduler.set("cancel_task", method(scheduler, args -> {
            cancelOwnedTask(args.checkint(1));
            return LuaValue.NIL;
        }));
        scheduler.set("timer", method(scheduler, args -> createLegacyTimerBuilder(args.checkint(1))));
        return scheduler;
    }

    private LuaTable createZonesTable() {
        LuaTable zones = new LuaTable();
        zones.set("get_entities_in_zone", method(zones, args -> contextTables.queryZoneEntities(args.arg1(), args.arg(2))));
        zones.set("get_locations_in_zone", method(zones, args -> contextTables.queryZoneLocations(args.arg1(), args.arg(2))));
        zones.set("zone_contains", method(zones, args -> {
            Shape shape = support.createShape(args.arg1());
            Location location = support.toLocation(args.arg(2));
            boolean border = args.narg() >= 3 && "border".equalsIgnoreCase(args.arg(3).optjstring("full"));
            if (shape == null || location == null) {
                return LuaValue.FALSE;
            }
            return LuaValue.valueOf(border ? shape.borderContains(location) : shape.contains(location));
        }));
        zones.set("watch_zone", method(zones, args -> {
            Shape shape = support.createShape(args.arg1());
            LuaTable callbacks = args.checktable(2);
            if (shape != null) {
                zoneWatches.add(new ZoneWatch(shape,
                        callbacks.get("on_enter").isfunction() ? callbacks.get("on_enter").checkfunction() : null,
                        callbacks.get("on_leave").isfunction() ? callbacks.get("on_leave").checkfunction() : null,
                        support.resolveZoneFilter(args.arg(3)),
                        support.resolveZoneMode(args.arg(3))));
                updateClockRegistration();
            }
            return LuaValue.NIL;
        }));
        zones.set("get_zone", method(zones, args -> createLegacyNamedZone(args.checkjstring(1))));
        zones.set("sphere", method(zones, args -> createLegacySphereBuilder(args.checkdouble(1))));
        return zones;
    }

    private LuaTable createLegacyNamedZone(String name) {
        LuaTable zone = new LuaTable();
        zone.set("name", LuaValue.valueOf(name));
        zone.set("contains", method(zone, args -> LuaValue.FALSE));
        return zone;
    }

    private LuaTable createLegacyTimerBuilder(int intervalTicks) {
        LuaTable timer = new LuaTable();
        LegacyTimerState state = new LegacyTimerState(intervalTicks);
        timer.set("repeating", method(timer, args -> {
            state.repeating = true;
            return timer;
        }));
        timer.set("delay", method(timer, args -> {
            state.initialDelayTicks = args.checkint(1);
            return timer;
        }));
        timer.set("run", method(timer, args -> {
            LuaFunction callback = args.checkfunction(1);
            int initialDelayTicks = state.initialDelayTicks == null ? intervalTicks : state.initialDelayTicks;
            if (state.repeating) {
                return LuaValue.valueOf(ownRepeatingTask(initialDelayTicks, intervalTicks, callback));
            }
            return LuaValue.valueOf(ownLaterTask(initialDelayTicks, callback));
        }));
        return timer;
    }

    private LuaTable createLegacySphereBuilder(double radius) {
        LuaTable builder = new LuaTable();
        LegacySphereState state = new LegacySphereState(radius);
        builder.set("at_boss", method(builder, args -> builder));
        builder.set("track_boss", method(builder, args -> {
            state.trackBoss = true;
            return builder;
        }));
        builder.set("every", method(builder, args -> {
            state.intervalTicks = args.checkint(1);
            return builder;
        }));
        builder.set("particles", method(builder, args -> {
            String particleKey = args.checkjstring(1);
            LuaTable options = args.narg() >= 2 && args.arg(2).istable() ? args.arg(2).checktable() : new LuaTable();
            return LuaValue.valueOf(ownRepeatingTask(state.intervalTicks, state.intervalTicks, () ->
                    runLegacySphereParticles(state, particleKey, options)));
        }));
        return builder;
    }

    private void runLegacySphereParticles(LegacySphereState state, String particleKey, LuaTable options) {
        Location center = state.trackBoss || state.anchorLocation == null ? eliteEntity.getLocation() : state.anchorLocation;
        if (center == null || center.getWorld() == null) {
            return;
        }
        if (!state.trackBoss && state.anchorLocation == null) {
            state.anchorLocation = center.clone();
        }
        Location particleLocation = center.clone().add(randomVectorInSphere(state.radius));
        Vector offset = support.toVector(options.get("offset"));
        if (offset != null) {
            particleLocation.add(offset);
        }
        LuaTable particle = new LuaTable();
        particle.set("particle", LuaValue.valueOf(particleKey));
        particle.set("amount", LuaValue.valueOf(options.get("amount").optint(1)));
        particle.set("speed", LuaValue.valueOf(options.get("speed").optdouble(0)));
        support.spawnParticle(particleLocation, particle, 1);
    }

    private Vector randomVectorInSphere(double radius) {
        double x;
        double y;
        double z;
        do {
            x = (Math.random() * 2 - 1) * radius;
            y = (Math.random() * 2 - 1) * radius;
            z = (Math.random() * 2 - 1) * radius;
        } while (x * x + y * y + z * z > radius * radius);
        return new Vector(x, y, z);
    }

    private void runCallback(LuaFunction callback) {
        if (closed) return;
        try {
            callback.call(buildContext(null, null, null));
        } catch (Exception exception) {
            Logger.warn("Lua power " + definition.getFileName() + " failed inside a scheduled callback.");
            exception.printStackTrace();
            shutdown();
        }
    }

    private void tickZoneWatches() {
        if (zoneWatches.isEmpty() || eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) return;
        for (ZoneWatch zoneWatch : new ArrayList<>(zoneWatches)) {
            zoneWatch.tick();
            if (closed) {
                return;
            }
        }
    }

    private void updateClockRegistration() {
        boolean shouldTick = !closed && eliteEntity.exists() && (definition.supportsHook(LuaPowerHook.ON_TICK) || !zoneWatches.isEmpty());
        if (shouldTick) {
            if (clockTaskId == null) {
                clockTaskId = GameClock.scheduleRepeating(1L, 1L, this::onTick);
            }
            return;
        }
        if (clockTaskId != null) {
            GameClock.cancel(clockTaskId);
            clockTaskId = null;
        }
    }

    private int ownLaterTask(int ticks, LuaFunction callback) {
        int[] taskId = new int[1];
        taskId[0] = GameClock.scheduleLater(ticks, () -> {
            ownedTasks.remove(taskId[0]);
            runCallback(callback);
        });
        ownedTasks.put(taskId[0], () -> GameClock.cancel(taskId[0]));
        return taskId[0];
    }

    private int ownRepeatingTask(int ticks, LuaFunction callback) {
        int taskId = GameClock.scheduleRepeating(0L, ticks, () -> runCallback(callback));
        ownedTasks.put(taskId, () -> GameClock.cancel(taskId));
        return taskId;
    }

    private int ownRepeatingTask(int initialDelayTicks, int intervalTicks, LuaFunction callback) {
        int taskId = GameClock.scheduleRepeating(initialDelayTicks, intervalTicks, () -> runCallback(callback));
        ownedTasks.put(taskId, () -> GameClock.cancel(taskId));
        return taskId;
    }

    private int ownLaterTask(int ticks, Runnable runnable) {
        int[] taskId = new int[1];
        taskId[0] = GameClock.scheduleLater(ticks, () -> {
            ownedTasks.remove(taskId[0]);
            runnable.run();
        });
        ownedTasks.put(taskId[0], () -> GameClock.cancel(taskId[0]));
        return taskId[0];
    }

    private int ownRepeatingTask(int ticks, Runnable runnable) {
        int taskId = GameClock.scheduleRepeating(0L, ticks, runnable);
        ownedTasks.put(taskId, () -> GameClock.cancel(taskId));
        return taskId;
    }

    private int ownRepeatingTask(int initialDelayTicks, int intervalTicks, Runnable runnable) {
        int taskId = GameClock.scheduleRepeating(initialDelayTicks, intervalTicks, runnable);
        ownedTasks.put(taskId, () -> GameClock.cancel(taskId));
        return taskId;
    }

    private void cancelOwnedTask(int taskId) {
        OwnedTask task = ownedTasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    private VarArgFunction logFunction(LuaTable owner, java.util.function.Consumer<String> consumer) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Varargs strippedArgs = stripMethodSelf(args, owner);
                if (strippedArgs.narg() > 0 && strippedArgs.arg1().isstring()) {
                    consumer.accept(strippedArgs.arg1().tojstring());
                }
                return LuaValue.NIL;
            }
        };
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

    private String resolveCooldownKey(Varargs args, int index) {
        if (args.narg() < index || args.arg(index).isnil()) {
            return "__lua:" + definition.getFileName();
        }
        return args.checkjstring(index);
    }

    private final class ZoneWatch {
        private final Shape shape;
        private final LuaFunction onEnter;
        private final LuaFunction onLeave;
        private final String filter;
        private final boolean borderMode;
        private final Map<UUID, LivingEntity> currentInside = new HashMap<>();

        private ZoneWatch(Shape shape, LuaFunction onEnter, LuaFunction onLeave, String filter, boolean borderMode) {
            this.shape = shape;
            this.onEnter = onEnter;
            this.onLeave = onLeave;
            this.filter = filter;
            this.borderMode = borderMode;
        }

        private void tick() {
            Set<UUID> seen = new HashSet<>();
            for (LivingEntity livingEntity : support.filterEntities(eliteEntity.getLocation().getWorld(), filter)) {
                if (livingEntity.equals(eliteEntity.getLivingEntity())) continue;
                boolean contains = borderMode ? shape.borderContains(livingEntity.getLocation()) : shape.contains(livingEntity);
                if (!contains) continue;
                seen.add(livingEntity.getUniqueId());
                if (!currentInside.containsKey(livingEntity.getUniqueId())) {
                    currentInside.put(livingEntity.getUniqueId(), livingEntity);
                    if (onEnter != null) runEntityCallback(onEnter, livingEntity);
                }
            }
            Iterator<Map.Entry<UUID, LivingEntity>> iterator = currentInside.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, LivingEntity> entry = iterator.next();
                if (seen.contains(entry.getKey())) continue;
                LivingEntity leavingEntity = entry.getValue();
                iterator.remove();
                if (onLeave != null && leavingEntity != null) runEntityCallback(onLeave, leavingEntity);
            }
        }

        private void runEntityCallback(LuaFunction callback, LivingEntity livingEntity) {
            try {
                callback.call(entityTables.createEntityTable(livingEntity));
            } catch (Exception exception) {
                Logger.warn("Lua power " + definition.getFileName() + " failed inside a zone callback.");
                exception.printStackTrace();
                shutdown();
            }
        }
    }

    @FunctionalInterface
    private interface OwnedTask {
        void cancel();
    }

    @FunctionalInterface
    private interface LuaCallback {
        Varargs invoke(Varargs args);
    }

    private static final class LegacyTimerState {
        private final int intervalTicks;
        private boolean repeating = false;
        private Integer initialDelayTicks = null;

        private LegacyTimerState(int intervalTicks) {
            this.intervalTicks = intervalTicks;
        }
    }

    private static final class LegacySphereState {
        private final double radius;
        private boolean trackBoss = false;
        private int intervalTicks = 1;
        private Location anchorLocation = null;

        private LegacySphereState(double radius) {
            this.radius = radius;
        }
    }
}
