package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Collection;
import java.util.Locale;

final class LuaPowerContextTables {

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;
    private final LuaPowerEntityTables entityTables;

    LuaPowerContextTables(LuaPowerDefinition definition,
                          EliteEntity eliteEntity,
                          LuaPowerSupport support,
                          LuaPowerEntityTables entityTables) {
        this.definition = definition;
        this.eliteEntity = eliteEntity;
        this.support = support;
        this.entityTables = entityTables;
    }

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
                for (LivingEntity livingEntity : support.filterEntities(eliteEntity.getLocation().getWorld(), filter)) {
                    if (eliteEntity.getLivingEntity() != null &&
                            livingEntity.getUniqueId().equals(eliteEntity.getLivingEntity().getUniqueId())) {
                        continue;
                    }
                    if (livingEntity.getLocation().distanceSquared(eliteEntity.getLocation()) <= radius * radius) {
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

    LuaTable createWorldTable() {
        LuaTable world = new LuaTable();
        world.set("play_sound_at_location", method(world, args -> {
            support.playSound(support.toLocation(args.arg1()), args.checkjstring(2), support.getFloat(args, 3, 1f), support.getFloat(args, 4, 1f));
            return LuaValue.NIL;
        }));
        world.set("spawn_particle_at_location", method(world, args -> {
            support.spawnParticle(support.toLocation(args.arg1()), args.arg(2), args.optint(3, 1));
            return LuaValue.NIL;
        }));
        world.set("strike_lightning_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            if (location != null && location.getWorld() != null) {
                LightningSpawnBypass.strikeLightningIgnoreProtections(location);
            }
            return LuaValue.NIL;
        }));
        world.set("set_world_time", method(world, args -> {
            Location location = args.narg() >= 2 ? support.toLocation(args.arg1()) : eliteEntity.getLocation();
            long time = args.narg() >= 2 ? args.checklong(2) : args.checklong(1);
            if (location != null && location.getWorld() != null) {
                location.getWorld().setTime(time);
            }
            return LuaValue.NIL;
        }));
        world.set("set_world_weather", method(world, args -> {
            Location location = eliteEntity.getLocation();
            int weatherIndex = 1;
            if (args.narg() >= 2 && args.arg1().istable()) {
                location = support.toLocation(args.arg1());
                weatherIndex = 2;
            }
            if (location == null || location.getWorld() == null) {
                return LuaValue.NIL;
            }
            String weather = args.checkjstring(weatherIndex).toUpperCase(Locale.ROOT);
            int duration = args.narg() > weatherIndex ? args.optint(weatherIndex + 1, 6000) : 6000;
            World worldRef = location.getWorld();
            switch (weather) {
                case "CLEAR" -> {
                    worldRef.setStorm(false);
                    worldRef.setThundering(false);
                    worldRef.setWeatherDuration(duration);
                }
                case "PRECIPITATION", "RAIN" -> {
                    worldRef.setStorm(true);
                    worldRef.setThundering(false);
                    worldRef.setWeatherDuration(duration);
                }
                case "THUNDER" -> {
                    worldRef.setStorm(true);
                    worldRef.setThundering(true);
                    worldRef.setThunderDuration(duration);
                }
                default -> Logger.warn("Unknown weather type " + weather + " in Lua power " + definition.getFileName() + ".");
            }
            return LuaValue.NIL;
        }));
        world.set("run_console_command", method(world, args -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args.checkjstring(1));
            return LuaValue.NIL;
        }));
        world.set("spawn_boss_at_location", method(world, args -> {
            if (!(eliteEntity instanceof CustomBossEntity)) {
                return LuaValue.NIL;
            }
            CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(args.checkjstring(1));
            if (customBossEntity == null) {
                return LuaValue.NIL;
            }
            Location location = args.narg() >= 2 ? support.toLocation(args.arg(2)) : eliteEntity.getLocation();
            int level = args.narg() >= 3 ? args.checkint(3) : eliteEntity.getLevel();
            if (location != null) {
                customBossEntity.spawn(location, level, false);
            }
            return LuaValue.NIL;
        }));
        world.set("spawn_entity_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg(2));
            if (location == null || location.getWorld() == null) {
                return LuaValue.NIL;
            }
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(args.checkjstring(1).toUpperCase(Locale.ROOT));
            } catch (Exception exception) {
                Logger.warn("Unknown entity type " + args.arg(1) + " in Lua power " + definition.getFileName() + ".");
                return LuaValue.NIL;
            }
            Entity entity = location.getWorld().spawnEntity(location, entityType);
            if (args.narg() >= 3 && args.arg(3).istable()) {
                LuaTable options = args.arg(3).checktable();
                Vector velocity = support.toVector(options.get("velocity"));
                if (velocity != null) {
                    entity.setVelocity(velocity);
                }
                int duration = options.get("duration").optint(0);
                if (duration > 0) {
                    Entity spawnedEntity = entity;
                    com.magmaguy.elitemobs.utils.GameClock.scheduleLater(duration, () -> {
                        if (spawnedEntity.isValid()) {
                            spawnedEntity.remove();
                        }
                    });
                }
                String effectName = options.get("effect").optjstring(null);
                if (effectName != null) {
                    try {
                        entity.playEffect(EntityEffect.valueOf(effectName.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            return entity instanceof LivingEntity livingEntity ? entityTables.createEntityTable(livingEntity) : LuaValue.NIL;
        }));
        world.set("spawn_reinforcement_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg(2));
            if (location == null) {
                return LuaValue.NIL;
            }
            CustomBossEntity reinforcement = CustomSummonPower.summonReinforcement(eliteEntity, location, args.checkjstring(1), args.optint(3, 0));
            if (reinforcement != null && reinforcement.getLivingEntity() != null) {
                if (args.narg() >= 4) {
                    Vector velocity = support.toVector(args.arg(4));
                    if (velocity != null) {
                        reinforcement.getLivingEntity().setVelocity(velocity);
                    }
                }
                return entityTables.createEntityTable(reinforcement.getLivingEntity());
            }
            return LuaValue.NIL;
        }));
        world.set("spawn_fireworks_at_location", method(world, args -> {
            support.spawnFirework(support.toLocation(args.arg1()), args.arg(2).checktable());
            return LuaValue.NIL;
        }));
        return world;
    }

    LuaTable queryZoneEntities(LuaValue zoneDefinition, LuaValue optionsValue) {
        LuaTable results = new LuaTable();
        com.magmaguy.elitemobs.utils.shapes.Shape shape = support.createShape(zoneDefinition);
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
            boolean contains = borderMode ? shape.borderContains(livingEntity.getLocation()) : shape.contains(livingEntity);
            if (contains) {
                results.set(index++, entityTables.createEntityTable(livingEntity));
            }
        }
        return results;
    }

    LuaTable queryZoneLocations(LuaValue zoneDefinition, LuaValue optionsValue) {
        LuaTable results = new LuaTable();
        com.magmaguy.elitemobs.utils.shapes.Shape shape = support.createShape(zoneDefinition);
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
