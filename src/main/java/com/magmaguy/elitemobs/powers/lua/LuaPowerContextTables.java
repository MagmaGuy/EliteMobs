package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import com.magmaguy.shaded.luaj.vm2.Varargs;
import com.magmaguy.shaded.luaj.vm2.lib.VarArgFunction;

import java.util.Collection;

/**
 * Creates Lua tables for context sections (players, entities, vectors, zones).
 * <p>
 * The world and settings tables are built by {@link LuaWorldTableBuilder}
 * (delegated via {@link #createWorldTable()} and {@link #createSettingsTable()}).
 */
final class LuaPowerContextTables {

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;
    private final LuaPowerEntityTables entityTables;
    private final LuaPowerScriptApi.OwnedTaskController taskController;
    private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;
    private final LuaWorldTableBuilder worldTableBuilder;

    LuaPowerContextTables(LuaPowerDefinition definition,
                          EliteEntity eliteEntity,
                          LuaPowerSupport support,
                          LuaPowerEntityTables entityTables,
                          LuaPowerScriptApi.OwnedTaskController taskController,
                          LuaPowerScriptApi.CallbackInvoker callbackInvoker) {
        this.definition = definition;
        this.eliteEntity = eliteEntity;
        this.support = support;
        this.entityTables = entityTables;
        this.taskController = taskController;
        this.callbackInvoker = callbackInvoker;
        this.worldTableBuilder = new LuaWorldTableBuilder(definition, eliteEntity, support, entityTables, taskController, callbackInvoker);
    }

    // ── Players table ──────────────────────────────────────────────────

    LuaTable createPlayersTable(LivingEntity eventActor) {
        LuaTable players = new LuaTable();
        players.set("current_target", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (eventActor instanceof Player player) {
                    return entityTables.createPlayerTable(player);
                }
                if (eliteEntity.getLivingEntity() instanceof Mob mob && mob.getTarget() instanceof Player player) {
                    return entityTables.createPlayerTable(player);
                }
                return LuaValue.NIL;
            }
        });
        players.set("nearby_players", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable results = new LuaTable();
                if (eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
                    return results;
                }
                double radius = args.checkdouble(1);
                int index = 1;
                for (Player player : eliteEntity.getLocation().getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(eliteEntity.getLocation()) <= radius * radius) {
                        results.set(index++, entityTables.createPlayerTable(player));
                    }
                }
                return results;
            }
        });
        players.set("all_players_in_world", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable results = new LuaTable();
                if (eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
                    return results;
                }
                int index = 1;
                for (Player player : eliteEntity.getLocation().getWorld().getPlayers()) {
                    results.set(index++, entityTables.createPlayerTable(player));
                }
                return results;
            }
        });
        return players;
    }

    // ── Entities table ─────────────────────────────────────────────────

    LuaTable createEntitiesTable(LivingEntity directTarget) {
        LuaTable entities = new LuaTable();
        entities.set("get_nearby_entities", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                double radius = args.checkdouble(1);
                String filter = args.narg() >= 2 && args.arg(2).isstring() ? args.arg(2).tojstring() : "living";
                LuaTable results = new LuaTable();
                if (eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
                    return results;
                }
                int index = 1;
                if ("all".equalsIgnoreCase(filter) || "entities".equalsIgnoreCase(filter)) {
                    for (org.bukkit.entity.Entity entity : eliteEntity.getLocation().getWorld().getNearbyEntities(
                            eliteEntity.getLocation(), radius, radius, radius)) {
                        if (eliteEntity.getLivingEntity() != null &&
                                entity.getUniqueId().equals(eliteEntity.getLivingEntity().getUniqueId())) {
                            continue;
                        }
                        results.set(index++, entityTables.createEntityReferenceTable(entity));
                    }
                } else {
                    for (LivingEntity livingEntity : support.filterEntities(eliteEntity.getLocation().getWorld(), filter)) {
                        if (eliteEntity.getLivingEntity() != null &&
                                livingEntity.getUniqueId().equals(eliteEntity.getLivingEntity().getUniqueId())) {
                            continue;
                        }
                        if (livingEntity.getLocation().distanceSquared(eliteEntity.getLocation()) <= radius * radius) {
                            results.set(index++, entityTables.createEntityTable(livingEntity));
                        }
                    }
                }
                return results;
            }
        });
        entities.set("get_entities_in_box", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Location center = support.toLocation(args.arg1());
                double halfX = args.checkdouble(2);
                double halfY = args.checkdouble(3);
                double halfZ = args.checkdouble(4);
                String filter = args.narg() >= 5 && args.arg(5).isstring() ? args.arg(5).tojstring() : "living";
                LuaTable results = new LuaTable();
                if (center == null || center.getWorld() == null) {
                    return results;
                }
                int index = 1;
                double minX = center.getX() - halfX;
                double maxX = center.getX() + halfX;
                double minY = center.getY() - halfY;
                double maxY = center.getY() + halfY;
                double minZ = center.getZ() - halfZ;
                double maxZ = center.getZ() + halfZ;
                for (LivingEntity livingEntity : support.filterEntities(center.getWorld(), filter)) {
                    if (eliteEntity.getLivingEntity() != null &&
                            livingEntity.getUniqueId().equals(eliteEntity.getLivingEntity().getUniqueId())) {
                        continue;
                    }
                    Location entityLocation = livingEntity.getLocation();
                    if (entityLocation.getX() >= minX && entityLocation.getX() <= maxX
                            && entityLocation.getY() >= minY && entityLocation.getY() <= maxY
                            && entityLocation.getZ() >= minZ && entityLocation.getZ() <= maxZ) {
                        results.set(index++, entityTables.createEntityTable(livingEntity));
                    }
                }
                return results;
            }
        });
        entities.set("get_all_entities", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String filter = args.narg() >= 1 && args.arg(1).isstring() ? args.arg(1).tojstring() : "living";
                LuaTable results = new LuaTable();
                if (eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
                    return results;
                }
                int index = 1;
                for (LivingEntity livingEntity : support.filterEntities(eliteEntity.getLocation().getWorld(), filter)) {
                    if (eliteEntity.getLivingEntity() != null &&
                            livingEntity.getUniqueId().equals(eliteEntity.getLivingEntity().getUniqueId())) {
                        continue;
                    }
                    results.set(index++, entityTables.createEntityTable(livingEntity));
                }
                return results;
            }
        });
        entities.set("get_direct_target_entity", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return directTarget == null ? LuaValue.NIL : entityTables.createEntityTable(directTarget);
            }
        });
        entities.set("get_boss_spawn_location", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return support.toLocationTable(eliteEntity.getSpawnLocation());
            }
        });
        return entities;
    }

    // ── Vectors table ──────────────────────────────────────────────────

    LuaTable createVectorsTable() {
        LuaTable vectors = new LuaTable();
        vectors.set("get_vector_between_locations", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Location source = support.toLocation(args.arg1());
                Location destination = support.toLocation(args.arg(2));
                if (source == null || destination == null || source.getWorld() == null || destination.getWorld() == null ||
                        !source.getWorld().getUID().equals(destination.getWorld().getUID())) {
                    return support.toVectorTable(new Vector(0, 0, 0));
                }
                Vector vector = destination.toVector().subtract(source.toVector());
                if (args.narg() >= 3 && args.arg(3).istable()) {
                    support.applyVectorOptions(vector, args.arg(3).checktable());
                }
                return support.toVectorTable(vector);
            }
        });
        vectors.set("rotate_vector", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Vector vector = support.toVector(args.arg1());
                if (vector == null) {
                    return support.toVectorTable(new Vector(0, 0, 0));
                }
                double pitch = args.narg() >= 2 ? args.arg(2).optdouble(0) : 0;
                double yaw = args.narg() >= 3 ? args.arg(3).optdouble(0) : 0;
                return support.toVectorTable(support.rotateVector(vector, pitch, yaw));
            }
        });
        vectors.set("normalize_vector", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Vector vector = support.toVector(args.arg1());
                if (vector == null) {
                    return support.toVectorTable(new Vector(0, 0, 0));
                }
                if (vector.lengthSquared() > 0) {
                    vector.normalize();
                }
                return support.toVectorTable(vector);
            }
        });
        return vectors;
    }

    // ── World & settings tables (delegated to LuaWorldTableBuilder) ──

    LuaTable createWorldTable() {
        return worldTableBuilder.build();
    }

    LuaTable createSettingsTable() {
        return worldTableBuilder.buildSettingsTable();
    }

    // ── Zone queries ───────────────────────────────────────────────────

    LuaTable queryZoneEntities(LuaValue zoneDefinition, LuaValue optionsValue) {
        LuaTable results = new LuaTable();
        com.magmaguy.magmacore.scripting.zones.Shape shape = support.createShape(zoneDefinition);
        if (shape == null || eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
            return results;
        }
        String filter = support.resolveZoneFilter(optionsValue);
        boolean borderMode = support.resolveZoneMode(optionsValue);
        int index = 1;
        for (LivingEntity livingEntity : support.filterEntities(eliteEntity.getLocation().getWorld(), filter)) {
            if (eliteEntity.getLivingEntity() != null &&
                    livingEntity.getUniqueId().equals(eliteEntity.getLivingEntity().getUniqueId())) {
                continue;
            }
            boolean contains = borderMode ? shape.borderContains(livingEntity) : shape.contains(livingEntity);
            if (contains) {
                results.set(index++, entityTables.createEntityTable(livingEntity));
            }
        }
        return results;
    }

    LuaTable queryZoneLocations(LuaValue zoneDefinition, LuaValue optionsValue) {
        LuaTable results = new LuaTable();
        com.magmaguy.magmacore.scripting.zones.Shape shape = support.createShape(zoneDefinition);
        if (shape == null) {
            return results;
        }
        boolean borderMode = support.resolveZoneMode(optionsValue);
        double coverage = 1.0;
        if (optionsValue != null && optionsValue.istable()) {
            coverage = optionsValue.checktable().get("coverage").optdouble(1.0);
        }
        Collection<Location> locations = borderMode ? shape.getEdgeLocations() : shape.getLocations();
        int index = 1;
        for (Location location : locations) {
            if (coverage < 1.0 && Math.random() > coverage) {
                continue;
            }
            results.set(index++, support.toLocationTable(location));
        }
        return results;
    }
}
