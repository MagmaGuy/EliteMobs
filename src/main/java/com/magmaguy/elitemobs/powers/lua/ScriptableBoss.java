package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.scripting.ScriptHook;
import com.magmaguy.magmacore.scripting.ScriptInstance;
import com.magmaguy.magmacore.scripting.ScriptableEntity;
import com.magmaguy.magmacore.scripting.zones.Shape;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import com.magmaguy.shaded.luaj.vm2.Varargs;
import com.magmaguy.shaded.luaj.vm2.lib.VarArgFunction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Adapts an {@link EliteEntity} boss for the shared Magmacore scripting runtime
 * ({@link ScriptInstance}). This replaces the old self-contained {@code LuaPowerInstance}:
 * the lifecycle (Lua VM, hook dispatch, the lazy context metatable, the 50ms watchdog, owned
 * tasks, shutdown) is now Magmacore's, while the rich boss-specific context tables are still
 * produced by the original builder classes ({@link LuaPowerEntityTables},
 * {@link LuaPowerContextTables}, {@link LuaPowerScriptApi}, {@link LuaPowerSupport}) — reused
 * verbatim — so the full boss scripting API is preserved.
 */
public class ScriptableBoss extends ScriptableEntity {

    // ── Boss-specific hooks ─────────────────────────────────────────────
    public static final ScriptHook ON_DAMAGED = new ScriptHook("on_boss_damaged");
    public static final ScriptHook ON_DAMAGED_BY_PLAYER = new ScriptHook("on_boss_damaged_by_player");
    public static final ScriptHook ON_DAMAGED_BY_ELITE = new ScriptHook("on_boss_damaged_by_elite");
    public static final ScriptHook ON_PLAYER_DAMAGED = new ScriptHook("on_player_damaged_by_boss");
    public static final ScriptHook ON_ENTER_COMBAT = new ScriptHook("on_enter_combat");
    public static final ScriptHook ON_EXIT_COMBAT = new ScriptHook("on_exit_combat");
    public static final ScriptHook ON_HEAL = new ScriptHook("on_heal");
    public static final ScriptHook ON_TARGET = new ScriptHook("on_boss_target_changed");
    public static final ScriptHook ON_DEATH = new ScriptHook("on_death");
    public static final ScriptHook ON_PHASE_SWITCH = new ScriptHook("on_phase_switch");

    private static final Set<ScriptHook> SUPPORTED_HOOKS = Set.of(
            ScriptHook.ON_SPAWN, ScriptHook.ON_TICK,
            ScriptHook.ON_ZONE_ENTER, ScriptHook.ON_ZONE_LEAVE,
            ON_DAMAGED, ON_DAMAGED_BY_PLAYER, ON_DAMAGED_BY_ELITE,
            ON_PLAYER_DAMAGED, ON_ENTER_COMBAT, ON_EXIT_COMBAT,
            ON_HEAL, ON_TARGET, ON_DEATH, ON_PHASE_SWITCH
    );

    private final EliteEntity eliteEntity;

    // Lazily-built boss runtime helpers — reuse the existing builders verbatim. Built on the
    // first context access (when the owning ScriptInstance is available for task/callback wiring).
    private boolean helpersReady = false;
    private String fileName;
    private LuaPowerSupport support;
    private LuaPowerEntityTables entityTables;
    private LuaPowerContextTables contextTables;
    private LuaPowerScriptApi scriptApi;

    public ScriptableBoss(EliteEntity eliteEntity) {
        this.eliteEntity = eliteEntity;
    }

    public EliteEntity getEliteEntity() {
        return eliteEntity;
    }

    private void ensureHelpers(ScriptInstance instance) {
        if (helpersReady) return;
        this.fileName = instance.getDefinition().getFileName();
        this.support = new LuaPowerSupport(instance.getDefinition(), eliteEntity);
        LuaPowerScriptApi.OwnedTaskController taskController = new LuaPowerScriptApi.OwnedTaskController() {
            @Override
            public int runLater(int ticks, Runnable runnable) {
                return instance.ownLater(ticks, runnable);
            }

            @Override
            public int runRepeating(int initialDelayTicks, int intervalTicks, Runnable runnable) {
                return instance.ownRepeating(initialDelayTicks, intervalTicks, runnable);
            }

            @Override
            public void cancel(int taskId) {
                instance.cancelOwned(taskId);
            }
        };
        LuaPowerScriptApi.CallbackInvoker callbackInvoker = instance::invokeOwnedCallback;
        this.entityTables = new LuaPowerEntityTables(instance.getDefinition(), eliteEntity, support, taskController, callbackInvoker);
        this.contextTables = new LuaPowerContextTables(instance.getDefinition(), eliteEntity, support, entityTables, taskController, callbackInvoker);
        this.scriptApi = new LuaPowerScriptApi(instance.getDefinition(), eliteEntity, support, entityTables, taskController, callbackInvoker);
        this.helpersReady = true;
    }

    // ── ScriptableEntity contract ───────────────────────────────────────

    @Override
    public LuaTable buildContextTable(ScriptInstance instance) {
        ensureHelpers(instance);
        return entityTables.createBossTable();
    }

    @Override
    public String getContextKey() {
        return "boss";
    }

    @Override
    public Set<ScriptHook> getSupportedHooks() {
        return SUPPORTED_HOOKS;
    }

    @Override
    public Entity getBukkitEntity() {
        return eliteEntity.getLivingEntity();
    }

    @Override
    public Location getLocation() {
        return eliteEntity.getLocation();
    }

    @Override
    public LuaValue resolveExtraContext(String key, ScriptInstance instance) {
        ensureHelpers(instance);
        LivingEntity directTarget = instance.getCurrentDirectTarget();
        LivingEntity eventActor = instance.getCurrentEventActor();
        Event event = instance.getCurrentEvent();
        Player contextPlayer = resolveContextPlayer(directTarget, eventActor);
        return switch (key) {
            case "log" -> createLogTable();
            case "cooldowns" -> createCooldownTable();
            case "scheduler" -> createSchedulerTable(instance);
            case "players" -> contextTables.createPlayersTable(eventActor);
            case "entities" -> contextTables.createEntitiesTable(directTarget);
            case "vectors" -> contextTables.createVectorsTable();
            case "world" -> contextTables.createWorldTable();
            case "settings" -> contextTables.createSettingsTable();
            case "zones" -> createZonesTable(instance);
            case "player" -> contextPlayer == null ? LuaValue.NIL : entityTables.createPlayerTable(contextPlayer);
            case "event" -> event == null ? LuaValue.NIL : entityTables.createEventTable(event);
            case "script" -> scriptApi.createTable(event, directTarget);
            default -> LuaValue.NIL;
        };
    }

    // ── Boss-specific context tables (moved verbatim from the old LuaPowerInstance) ──

    private Player resolveContextPlayer(LivingEntity directTarget, LivingEntity eventActor) {
        if (directTarget instanceof Player player) return player;
        if (eventActor instanceof Player player) return player;
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
            if (!eliteEntity.isSharedCooldownReady(key)) return LuaValue.FALSE;
            eliteEntity.setSharedCooldown(key, duration);
            return LuaValue.TRUE;
        }));
        cooldowns.set("set_local", method(cooldowns, args -> {
            eliteEntity.setSharedCooldown(resolveCooldownKey(args, 2), args.checklong(1));
            return LuaValue.NIL;
        }));
        cooldowns.set("global_ready", method(cooldowns, args -> LuaValue.valueOf(!eliteEntity.isInCooldown())));
        cooldowns.set("set_global", method(cooldowns, args -> {
            eliteEntity.doGlobalPowerCooldown(args.checkint(1));
            return LuaValue.NIL;
        }));
        return cooldowns;
    }

    private LuaTable createSchedulerTable(ScriptInstance instance) {
        LuaTable scheduler = new LuaTable();
        scheduler.set("run_after", method(scheduler, args -> {
            int ticks = args.checkint(1);
            LuaFunction callback = args.checkfunction(2);
            return LuaValue.valueOf(instance.ownLuaLater(ticks, callback));
        }));
        scheduler.set("run_every", method(scheduler, args -> {
            int ticks = args.checkint(1);
            LuaFunction callback = args.checkfunction(2);
            return LuaValue.valueOf(instance.ownLuaRepeating(0, ticks, callback));
        }));
        scheduler.set("cancel_task", method(scheduler, args -> {
            instance.cancelOwned(args.checkint(1));
            return LuaValue.NIL;
        }));
        // MagmaCore-convention aliases so both naming styles work on bosses too.
        scheduler.set("run_later", method(scheduler, args -> {
            int ticks = args.checkint(1);
            LuaFunction callback = args.checkfunction(2);
            return LuaValue.valueOf(instance.ownLuaLater(ticks, callback));
        }));
        scheduler.set("run_repeating", method(scheduler, args -> {
            int delay = args.checkint(1);
            int interval = args.checkint(2);
            LuaFunction callback = args.checkfunction(3);
            return LuaValue.valueOf(instance.ownLuaRepeating(delay, interval, callback));
        }));
        scheduler.set("cancel", method(scheduler, args -> {
            instance.cancelOwned(args.checkint(1));
            return LuaValue.NIL;
        }));
        return scheduler;
    }

    private LuaTable createZonesTable(ScriptInstance instance) {
        LuaTable zones = new LuaTable();
        zones.set("get_entities_in_zone", method(zones, args -> contextTables.queryZoneEntities(args.arg1(), args.arg(2))));
        zones.set("get_locations_in_zone", method(zones, args -> contextTables.queryZoneLocations(args.arg1(), args.arg(2))));
        zones.set("zone_contains", method(zones, args -> {
            Shape shape = support.createShape(args.arg1());
            Location location = support.toLocation(args.arg(2));
            boolean border = args.narg() >= 3 && "border".equalsIgnoreCase(args.arg(3).optjstring("full"));
            if (shape == null || location == null) return LuaValue.FALSE;
            return LuaValue.valueOf(border ? shape.borderContains(location) : shape.contains(location));
        }));
        zones.set("watch_zone", method(zones, args -> {
            Shape shape = support.createShape(args.arg1());
            LuaTable callbacks = args.checktable(2);
            if (shape == null) return LuaValue.NIL;
            LuaFunction onEnter = callbacks.get("on_enter").isfunction() ? callbacks.get("on_enter").checkfunction() : null;
            LuaFunction onLeave = callbacks.get("on_leave").isfunction() ? callbacks.get("on_leave").checkfunction() : null;
            ZoneWatch watch = new ZoneWatch(instance, shape, onEnter, onLeave,
                    support.resolveZoneFilter(args.arg(3)), support.resolveZoneMode(args.arg(3)));
            // Drive the watch as an OWNED repeating task — auto-cancelled on shutdown, and self-cancels
            // when the boss no longer exists. (Replaces the old onTick-driven zoneWatches list.)
            int[] taskId = new int[1];
            taskId[0] = instance.ownRepeating(1, 1, () -> {
                if (!eliteEntity.exists() || eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
                    instance.cancelOwned(taskId[0]);
                    return;
                }
                watch.tick();
            });
            return LuaValue.NIL;
        }));
        return zones;
    }

    private String resolveCooldownKey(Varargs args, int index) {
        if (args.narg() < index || args.arg(index).isnil()) {
            return "__lua:" + fileName;
        }
        return args.checkjstring(index);
    }

    // ── Zone watch (moved verbatim from the old LuaPowerInstance, now task-driven) ──

    private final class ZoneWatch {
        private final ScriptInstance instance;
        private final Shape shape;
        private final LuaFunction onEnter;
        private final LuaFunction onLeave;
        private final String filter;
        private final boolean borderMode;
        private final Map<UUID, LivingEntity> currentInside = new HashMap<>();

        private ZoneWatch(ScriptInstance instance, Shape shape, LuaFunction onEnter, LuaFunction onLeave, String filter, boolean borderMode) {
            this.instance = instance;
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
                boolean contains = borderMode ? shape.borderContains(livingEntity) : shape.contains(livingEntity);
                if (!contains) continue;
                seen.add(livingEntity.getUniqueId());
                if (!currentInside.containsKey(livingEntity.getUniqueId())) {
                    currentInside.put(livingEntity.getUniqueId(), livingEntity);
                    if (onEnter != null) {
                        instance.invokeOwnedCallback("a zone enter callback", onEnter, entityTables.createEntityTable(livingEntity));
                    }
                }
            }
            Iterator<Map.Entry<UUID, LivingEntity>> iterator = currentInside.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, LivingEntity> entry = iterator.next();
                if (seen.contains(entry.getKey())) continue;
                LivingEntity leavingEntity = entry.getValue();
                iterator.remove();
                if (onLeave != null && leavingEntity != null) {
                    instance.invokeOwnedCallback("a zone leave callback", onLeave, entityTables.createEntityTable(leavingEntity));
                }
            }
        }
    }

    // ── Lua method-call boilerplate ────────────────────────────────────

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
        if (args.narg() == 0 || !args.arg1().raweq(owner)) return args;
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
