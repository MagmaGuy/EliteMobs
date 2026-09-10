package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.magmacore.scripting.ScriptInstance;
import com.magmaguy.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Builds the Lua table exposed as {@code context.npc} to NPC scripts.
 * <p>
 * This is the NPC-specific (tier-3) surface only. The generic scripting surface
 * (context.world, zones, scheduler, cooldowns, log, event, state) is provided automatically
 * by Magmacore's {@link ScriptInstance}.
 */
final class LuaNPCTable {

    private LuaNPCTable() {}

    static LuaTable build(NPCEntity npcEntity, ScriptInstance instance) {
        LuaTable npc = new LuaTable();
        npc.set("name", LuaValue.valueOf(npcEntity.getNPCsConfigFields().getName()));
        npc.set("filename", LuaValue.valueOf(npcEntity.getNPCsConfigFields().getFilename()));
        npc.set("uuid", LuaValue.valueOf(npcEntity.getUuid().toString()));
        npc.set("activation_radius", LuaValue.valueOf(npcEntity.getNPCsConfigFields().getActivationRadius()));

        npc.set("is_valid", LuaTableSupport.tableMethod(npc, args -> LuaValue.valueOf(npcEntity.isValid())));
        npc.set("get_location", LuaTableSupport.tableMethod(npc, args -> locationTable(currentLocation(npcEntity))));
        npc.set("get_eye_location", LuaTableSupport.tableMethod(npc, args -> locationTable(
                npcEntity.getVillager() == null ? npcEntity.getSpawnLocation() : npcEntity.getVillager().getEyeLocation())));
        npc.set("get_activation_radius", LuaTableSupport.tableMethod(npc, args ->
                LuaValue.valueOf(npcEntity.getNPCsConfigFields().getActivationRadius())));
        npc.set("get_nearby_players", LuaTableSupport.tableMethod(npc, args -> getNearbyPlayers(npcEntity, args.checkdouble(1))));
        npc.set("face_direction_or_location", LuaTableSupport.tableMethod(npc, args -> {
            faceDirectionOrLocation(npcEntity, args.arg1());
            return LuaValue.NIL;
        }));
        npc.set("say_greeting", LuaTableSupport.tableMethod(npc, args -> {
            Player player = resolvePlayer(args.arg1(), fallbackPlayer(instance), npcEntity);
            if (player != null) npcEntity.sayGreeting(player);
            return LuaValue.NIL;
        }));
        npc.set("say_dialog", LuaTableSupport.tableMethod(npc, args -> {
            Player player = resolvePlayer(args.arg1(), fallbackPlayer(instance), npcEntity);
            if (player != null) npcEntity.sayDialog(player);
            return LuaValue.NIL;
        }));
        npc.set("say_farewell", LuaTableSupport.tableMethod(npc, args -> {
            Player player = resolvePlayer(args.arg1(), fallbackPlayer(instance), npcEntity);
            if (player != null) npcEntity.sayFarewell(player);
            return LuaValue.NIL;
        }));
        npc.set("play_model_animation", LuaTableSupport.tableMethod(npc, args -> {
            if (npcEntity.getCustomModel() != null)
                npcEntity.getCustomModel().playAnimationByName(args.checkjstring(1));
            return LuaValue.NIL;
        }));
        npc.set("patrol_pause", LuaTableSupport.tableMethod(npc,
                args -> LuaValue.valueOf(PatrolService.pause(npcEntity))));
        npc.set("patrol_resume", LuaTableSupport.tableMethod(npc,
                args -> LuaValue.valueOf(PatrolService.resume(npcEntity))));
        npc.set("hold", LuaTableSupport.tableMethod(npc, args -> LuaValue.valueOf(
                PatrolService.hold(npcEntity, offset(args)))));
        npc.set("walk_to", LuaTableSupport.tableMethod(npc, args -> LuaValue.valueOf(
                PatrolService.walkTo(npcEntity, offset(args)))));
        npc.set("teleport", LuaTableSupport.tableMethod(npc, args -> LuaValue.valueOf(
                PatrolService.teleport(npcEntity, offset(args)))));
        npc.set("remove", LuaTableSupport.tableMethod(npc, args -> {
            npcEntity.remove(RemovalReason.OTHER);
            return LuaValue.NIL;
        }));

        LivingEntity villager = npcEntity.getVillager();
        if (villager != null) {
            npc.set("current_location", locationTable(villager.getLocation()));
            npc.set("entity_type", LuaValue.valueOf(villager.getType().name()));
        }
        return npc;
    }

    private static Player fallbackPlayer(ScriptInstance instance) {
        LivingEntity actor = instance.getCurrentEventActor();
        return actor instanceof Player player ? player : null;
    }

    private static Location currentLocation(NPCEntity npcEntity) {
        return npcEntity.getVillager() == null ? npcEntity.getSpawnLocation() : npcEntity.getVillager().getLocation();
    }

    private static LuaValue locationTable(Location location) {
        if (location == null) return LuaValue.NIL;
        LuaTable table = LuaTableSupport.locationToTable(location);
        // Scripts can explicitly read get_location().direction when they want a facing vector.
        table.set("direction", LuaTableSupport.vectorToTable(location.getDirection()));
        return table;
    }

    private static LuaTable getNearbyPlayers(NPCEntity npcEntity, double radius) {
        LuaTable results = new LuaTable();
        Location location = currentLocation(npcEntity);
        if (location == null || location.getWorld() == null) return results;
        int index = 1;
        double radiusSquared = radius * radius;
        for (Player player : location.getWorld().getPlayers())
            if (player.getLocation().distanceSquared(location) <= radiusSquared)
                results.set(index++, LuaLivingEntityTable.build(player));
        return results;
    }

    private static void faceDirectionOrLocation(NPCEntity npcEntity, LuaValue value) {
        if (npcEntity.getVillager() == null || !npcEntity.getVillager().isValid()) return;
        if (PatrolService.isActivelyMoving(npcEntity)) return;
        Location location = npcEntity.getVillager().getLocation();
        Vector direction;
        if (value.istable() && (value.get("world").isstring()
                || value.get("current_location").istable())) {
            Location destination = toLocation(value, npcEntity);
            if (destination == null || !location.getWorld().equals(destination.getWorld())) return;
            direction = destination.toVector().subtract(location.toVector());
        } else direction = toVector(value);
        if (direction == null || direction.lengthSquared() <= 0) return;
        location.setDirection(direction);
        npcEntity.getVillager().teleport(location);
    }

    private static Vector offset(com.magmaguy.shaded.luaj.vm2.Varargs args) {
        return new Vector(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3));
    }

    private static Location toLocation(LuaValue value, NPCEntity npcEntity) {
        if (value == null || value.isnil() || !value.istable()) return null;
        LuaTable table = value.checktable();
        if (table.get("current_location").istable()) table = table.get("current_location").checktable();
        Location fallbackLocation = currentLocation(npcEntity);
        World world = fallbackLocation == null ? null : fallbackLocation.getWorld();
        if (table.get("world").isstring()) world = Bukkit.getWorld(table.get("world").tojstring());
        if (world == null) return null;
        return new Location(
                world,
                table.get("x").optdouble(0),
                table.get("y").optdouble(0),
                table.get("z").optdouble(0),
                (float) table.get("yaw").optdouble(0),
                (float) table.get("pitch").optdouble(0));
    }

    private static Vector toVector(LuaValue value) {
        if (value == null || value.isnil() || !value.istable()) return null;
        LuaTable table = value.checktable();
        if (table.get("direction").istable()) table = table.get("direction").checktable();
        if (!table.get("x").isnumber() && !table.get(1).isnumber()) return null;
        return new Vector(
                getIndexedOrNamedDouble(table, 1, "x"),
                getIndexedOrNamedDouble(table, 2, "y"),
                getIndexedOrNamedDouble(table, 3, "z"));
    }

    private static double getIndexedOrNamedDouble(LuaTable table, int index, String key) {
        LuaValue named = table.get(key);
        if (named.isnumber()) return named.todouble();
        LuaValue indexed = table.get(index);
        return indexed.isnumber() ? indexed.todouble() : 0D;
    }

    private static Player resolvePlayer(LuaValue value, Player fallback, NPCEntity npcEntity) {
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
}
