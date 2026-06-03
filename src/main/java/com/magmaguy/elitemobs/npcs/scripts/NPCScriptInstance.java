package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.elitemobs.api.NPCEntityRemoveEvent;
import com.magmaguy.elitemobs.api.NPCProximityEnterEvent;
import com.magmaguy.elitemobs.api.NPCProximityLeaveEvent;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import com.magmaguy.shaded.luaj.vm2.Varargs;
import com.magmaguy.shaded.luaj.vm2.lib.VarArgFunction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class NPCScriptInstance {

    @Getter
    private final NPCScriptDefinition definition;
    @Getter
    private final NPCEntity npcEntity;
    private final LuaTable scriptTable;
    private final LuaTable stateTable = new LuaTable();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<Integer, OwnedTask> ownedTasks = new LinkedHashMap<>();
    private Integer clockTaskId = null;
    private boolean closed = false;

    public NPCScriptInstance(NPCScriptDefinition definition, NPCEntity npcEntity) {
        this.definition = definition;
        this.npcEntity = npcEntity;
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
        cooldowns.clear();
    }

    public void onTick() {
        if (closed) return;
        if (!npcEntity.isValid()) {
            shutdown();
            return;
        }
        if (definition.supportsHook(NPCScriptHook.ON_TICK)) {
            handleEvent(NPCScriptHook.ON_TICK, null, null);
        }
    }

    public void handleEvent(NPCScriptHook hook, Event event, Player player) {
        if (closed || hook == null || !definition.supportsHook(hook)) return;
        LuaValue function = scriptTable.get(hook.getKey());
        if (!function.isfunction()) return;

        long startNanos = System.nanoTime();
        try {
            function.checkfunction().call(buildContext(event, player));
        } catch (Exception exception) {
            logLuaError(hook.getKey(), exception);
            shutdown();
            return;
        }

        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
        if (elapsedMillis > 50) {
            Logger.warn("[NPC Lua] " + definition.getFileName() + " took " + elapsedMillis + "ms in '"
                    + hook.getKey() + "' (limit: 50ms) - script disabled to prevent lag.");
            shutdown();
        }
    }

    private LuaValue buildContext(Event event, Player player) {
        LuaTable context = new LuaTable();
        context.set("state", stateTable);
        context.set("npc", createNPCTable(player));
        context.set("player", player == null ? LuaValue.NIL : createPlayerTable(player));
        context.set("event", event == null ? LuaValue.NIL : createEventTable(event));
        context.set("scheduler", createSchedulerTable());
        context.set("cooldowns", createCooldownTable());
        return context;
    }

    private LuaTable createNPCTable(Player contextPlayer) {
        LuaTable npc = new LuaTable();
        LivingEntity livingEntity = npcEntity.getVillager();
        npc.set("name", LuaValue.valueOf(npcEntity.getNPCsConfigFields().getName()));
        npc.set("filename", LuaValue.valueOf(npcEntity.getNPCsConfigFields().getFilename()));
        npc.set("uuid", LuaValue.valueOf(npcEntity.getUuid().toString()));
        npc.set("activation_radius", LuaValue.valueOf(npcEntity.getNPCsConfigFields().getActivationRadius()));
        npc.set("is_valid", method(npc, args -> LuaValue.valueOf(npcEntity.isValid())));
        npc.set("get_location", method(npc, args -> toLocationTable(npcEntity.getVillager() == null ? npcEntity.getSpawnLocation() : npcEntity.getVillager().getLocation())));
        npc.set("get_eye_location", method(npc, args -> toLocationTable(npcEntity.getVillager() == null ? npcEntity.getSpawnLocation() : npcEntity.getVillager().getEyeLocation())));
        npc.set("get_activation_radius", method(npc, args -> LuaValue.valueOf(npcEntity.getNPCsConfigFields().getActivationRadius())));
        npc.set("get_nearby_players", method(npc, args -> getNearbyPlayers(args.checkdouble(1))));
        npc.set("face_direction_or_location", method(npc, args -> {
            faceDirectionOrLocation(args.arg1());
            return LuaValue.NIL;
        }));
        npc.set("say_greeting", method(npc, args -> {
            Player player = resolvePlayer(args.arg1(), contextPlayer);
            if (player != null) npcEntity.sayGreeting(player);
            return LuaValue.NIL;
        }));
        npc.set("say_dialog", method(npc, args -> {
            Player player = resolvePlayer(args.arg1(), contextPlayer);
            if (player != null) npcEntity.sayDialog(player);
            return LuaValue.NIL;
        }));
        npc.set("say_farewell", method(npc, args -> {
            Player player = resolvePlayer(args.arg1(), contextPlayer);
            if (player != null) npcEntity.sayFarewell(player);
            return LuaValue.NIL;
        }));
        npc.set("play_model_animation", method(npc, args -> {
            if (npcEntity.getCustomModel() != null) {
                npcEntity.getCustomModel().playAnimationByName(args.checkjstring(1));
            }
            return LuaValue.NIL;
        }));
        if (livingEntity != null) {
            npc.set("current_location", toLocationTable(livingEntity.getLocation()));
            npc.set("entity_type", LuaValue.valueOf(livingEntity.getType().name()));
        }
        return npc;
    }

    private LuaTable createPlayerTable(Player player) {
        LuaTable table = new LuaTable();
        table.set("name", LuaValue.valueOf(player.getName()));
        table.set("display_name", LuaValue.valueOf(player.getDisplayName()));
        table.set("uuid", LuaValue.valueOf(player.getUniqueId().toString()));
        table.set("is_valid", method(table, args -> LuaValue.valueOf(player.isValid())));
        table.set("get_location", method(table, args -> toLocationTable(player.getLocation())));
        table.set("get_eye_location", method(table, args -> toLocationTable(player.getEyeLocation())));
        table.set("send_message", method(table, args -> {
            player.sendMessage(ChatColorConverter.convert(args.checkjstring(1)));
            return LuaValue.NIL;
        }));
        return table;
    }

    private LuaTable createEventTable(Event event) {
        LuaTable table = new LuaTable();
        table.set("event_type", LuaValue.valueOf(event.getClass().getSimpleName()));
        if (event instanceof NPCProximityEnterEvent proximityEnterEvent) {
            table.set("activation_radius", LuaValue.valueOf(proximityEnterEvent.getActivationRadius()));
        } else if (event instanceof NPCProximityLeaveEvent proximityLeaveEvent) {
            table.set("activation_radius", LuaValue.valueOf(proximityLeaveEvent.getActivationRadius()));
        } else if (event instanceof NPCEntityRemoveEvent removeEvent) {
            table.set("removal_reason", LuaValue.valueOf(removeEvent.getRemovalReason().name()));
        }
        if (event instanceof Cancellable cancellable) {
            table.set("cancel_event", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    cancellable.setCancelled(true);
                    return LuaValue.NIL;
                }
            });
        }
        return table;
    }

    private LuaTable createSchedulerTable() {
        LuaTable scheduler = new LuaTable();
        scheduler.set("run_after", method(scheduler, args -> {
            int ticks = args.checkint(1);
            LuaFunction callback = args.checkfunction(2);
            return LuaValue.valueOf(ownLaterTask(ticks, callback));
        }));
        scheduler.set("run_every", method(scheduler, args -> {
            int ticks = args.checkint(1);
            LuaFunction callback = args.checkfunction(2);
            return LuaValue.valueOf(ownRepeatingTask(ticks, callback));
        }));
        scheduler.set("cancel_task", method(scheduler, args -> {
            cancelOwnedTask(args.checkint(1));
            return LuaValue.NIL;
        }));
        return scheduler;
    }

    private LuaTable createCooldownTable() {
        LuaTable table = new LuaTable();
        table.set("local_ready", method(table, args -> LuaValue.valueOf(isCooldownReady(resolveCooldownKey(args, 1)))));
        table.set("local_remaining", method(table, args -> LuaValue.valueOf(getCooldownRemaining(resolveCooldownKey(args, 1)))));
        table.set("check_local", method(table, args -> {
            String key = resolveCooldownKey(args, 2);
            if (!isCooldownReady(key)) {
                return LuaValue.FALSE;
            }
            setCooldown(key, args.checklong(1));
            return LuaValue.TRUE;
        }));
        table.set("set_local", method(table, args -> {
            setCooldown(resolveCooldownKey(args, 2), args.checklong(1));
            return LuaValue.NIL;
        }));
        return table;
    }

    private LuaTable getNearbyPlayers(double radius) {
        LuaTable results = new LuaTable();
        Location location = npcEntity.getVillager() == null ? npcEntity.getSpawnLocation() : npcEntity.getVillager().getLocation();
        if (location == null || location.getWorld() == null) {
            return results;
        }
        int index = 1;
        double radiusSquared = radius * radius;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= radiusSquared) {
                results.set(index++, createPlayerTable(player));
            }
        }
        return results;
    }

    private void faceDirectionOrLocation(LuaValue value) {
        if (npcEntity.getVillager() == null || !npcEntity.getVillager().isValid()) return;
        Vector direction = toVector(value);
        if (direction == null) {
            Location destination = toLocation(value);
            if (destination != null && destination.getWorld() != null &&
                    npcEntity.getVillager().getWorld().getUID().equals(destination.getWorld().getUID())) {
                direction = destination.toVector().subtract(npcEntity.getVillager().getLocation().toVector());
            }
        }
        if (direction == null || direction.lengthSquared() <= 0) return;
        Location location = npcEntity.getVillager().getLocation();
        location.setDirection(direction);
        npcEntity.getVillager().teleport(location);
    }

    private LuaTable toLocationTable(Location location) {
        LuaTable table = new LuaTable();
        if (location == null) return table;
        table.set("x", LuaValue.valueOf(location.getX()));
        table.set("y", LuaValue.valueOf(location.getY()));
        table.set("z", LuaValue.valueOf(location.getZ()));
        table.set("yaw", LuaValue.valueOf(location.getYaw()));
        table.set("pitch", LuaValue.valueOf(location.getPitch()));
        table.set("direction", toVectorTable(location.getDirection()));
        if (location.getWorld() != null) {
            table.set("world", LuaValue.valueOf(location.getWorld().getName()));
        }
        return table;
    }

    private LuaTable toVectorTable(Vector vector) {
        LuaTable table = new LuaTable();
        if (vector == null) return table;
        table.set("x", LuaValue.valueOf(vector.getX()));
        table.set("y", LuaValue.valueOf(vector.getY()));
        table.set("z", LuaValue.valueOf(vector.getZ()));
        return table;
    }

    private Location toLocation(LuaValue value) {
        if (value == null || value.isnil() || !value.istable()) return null;
        LuaTable table = value.checktable();
        if (table.get("current_location").istable()) {
            table = table.get("current_location").checktable();
        }
        Location fallbackLocation = npcEntity.getVillager() == null ? npcEntity.getSpawnLocation() : npcEntity.getVillager().getLocation();
        World world = fallbackLocation == null ? null : fallbackLocation.getWorld();
        if (table.get("world").isstring()) {
            world = Bukkit.getWorld(table.get("world").tojstring());
        }
        if (world == null) return null;
        return new Location(
                world,
                table.get("x").optdouble(0),
                table.get("y").optdouble(0),
                table.get("z").optdouble(0),
                (float) table.get("yaw").optdouble(0),
                (float) table.get("pitch").optdouble(0));
    }

    private Vector toVector(LuaValue value) {
        if (value == null || value.isnil() || !value.istable()) return null;
        LuaTable table = value.checktable();
        if (table.get("direction").istable()) {
            table = table.get("direction").checktable();
        }
        if (!table.get("x").isnumber() && !table.get(1).isnumber()) return null;
        return new Vector(
                getIndexedOrNamedDouble(table, 1, "x"),
                getIndexedOrNamedDouble(table, 2, "y"),
                getIndexedOrNamedDouble(table, 3, "z"));
    }

    private double getIndexedOrNamedDouble(LuaTable table, int index, String key) {
        LuaValue named = table.get(key);
        if (named.isnumber()) return named.todouble();
        LuaValue indexed = table.get(index);
        return indexed.isnumber() ? indexed.todouble() : 0D;
    }

    private Player resolvePlayer(LuaValue value, Player fallback) {
        if (value == null || value.isnil()) return fallback;
        if (value.istable()) {
            LuaValue uuidValue = value.checktable().get("uuid");
            if (uuidValue.isstring()) {
                try {
                    return Bukkit.getPlayer(UUID.fromString(uuidValue.tojstring()));
                } catch (Exception ignored) {
                    return fallback;
                }
            }
        }
        if (value.isstring()) {
            try {
                return Bukkit.getPlayer(UUID.fromString(value.tojstring()));
            } catch (Exception ignored) {
                return Bukkit.getPlayer(value.tojstring());
            }
        }
        return fallback;
    }

    private boolean isCooldownReady(String key) {
        return getCooldownRemaining(key) <= 0;
    }

    private long getCooldownRemaining(String key) {
        Long expiry = cooldowns.get(key);
        if (expiry == null) return 0;
        long remaining = expiry - GameClock.getCurrentTick();
        if (remaining <= 0) {
            cooldowns.remove(key);
            return 0;
        }
        return remaining;
    }

    private void setCooldown(String key, long durationTicks) {
        cooldowns.put(key, GameClock.getCurrentTick() + Math.max(0, durationTicks));
    }

    private String resolveCooldownKey(Varargs args, int index) {
        if (args.narg() < index || args.arg(index).isnil()) {
            return "__npc_lua:" + definition.getFileName();
        }
        return args.checkjstring(index);
    }

    private void updateClockRegistration() {
        boolean shouldTick = !closed && npcEntity.isValid() && definition.supportsHook(NPCScriptHook.ON_TICK);
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

    private void cancelOwnedTask(int taskId) {
        OwnedTask task = ownedTasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    private void runCallback(LuaFunction callback) {
        if (closed) return;
        long startNanos = System.nanoTime();
        try {
            callback.call(buildContext(null, null));
        } catch (Exception exception) {
            logLuaError("scheduled callback", exception);
            shutdown();
            return;
        }
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
        if (elapsedMillis > 50) {
            Logger.warn("[NPC Lua] " + definition.getFileName() + " took " + elapsedMillis
                    + "ms in a scheduled callback (limit: 50ms) - script disabled to prevent lag.");
            shutdown();
        }
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

    private void logLuaError(String context, Exception exception) {
        String rawMessage = exception.getMessage() != null ? exception.getMessage() : exception.toString();
        Logger.warn("[NPC Lua] Error in '" + definition.getFileName() + "' during '" + context + "':");
        Logger.warn("[NPC Lua]   -> " + rawMessage);
        Logger.warn("[NPC Lua]   -> Script has been disabled for this NPC.");
    }

    @FunctionalInterface
    private interface OwnedTask {
        void cancel();
    }

    @FunctionalInterface
    private interface LuaCallback {
        Varargs invoke(Varargs args);
    }
}
