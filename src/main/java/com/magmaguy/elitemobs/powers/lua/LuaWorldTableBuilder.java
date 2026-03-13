package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.ProjectileDamage;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.ScriptListener;
import com.magmaguy.elitemobs.powers.specialpowers.EnderDragonEmpoweredLightningSupport;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.List;
import java.util.Locale;

/**
 * Builds the Lua table exposed as {@code context.world} to Lua power scripts.
 * <p>
 * Extracted from {@link LuaPowerContextTables} for navigability.
 */
final class LuaWorldTableBuilder {

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;
    private final LuaPowerEntityTables entityTables;
    private final LuaPowerScriptApi.OwnedTaskController taskController;
    private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;

    LuaWorldTableBuilder(LuaPowerDefinition definition,
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
    }

    LuaTable build() {
        LuaTable world = new LuaTable();
        world.set("play_sound_at_location", method(world, args -> {
            support.playSound(support.toLocation(args.arg1()), args.checkjstring(2), support.getFloat(args, 3, 1f), support.getFloat(args, 4, 1f));
            return LuaValue.NIL;
        }));
        world.set("spawn_particle_at_location", method(world, args -> {
            support.spawnParticle(support.toLocation(args.arg1()), args.arg(2), args.optint(3, 1));
            return LuaValue.NIL;
        }));
        world.set("set_block_at_location", method(world, args -> {
            support.placeTemporaryBlock(support.toLocation(args.arg1()), args.checkjstring(2), 0, args.optboolean(3, false));
            return LuaValue.NIL;
        }));
        world.set("place_temporary_block_at_location", method(world, args -> {
            support.placeTemporaryBlock(support.toLocation(args.arg1()), args.checkjstring(2), args.optint(3, 0), args.optboolean(4, false));
            return LuaValue.NIL;
        }));
        world.set("get_block_type_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            if (location == null) {
                return LuaValue.NIL;
            }
            return LuaValue.valueOf(location.getBlock().getType().name());
        }));
        world.set("get_highest_block_y_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            if (location == null || location.getWorld() == null) {
                return LuaValue.NIL;
            }
            return LuaValue.valueOf(location.getWorld().getHighestBlockAt(location).getY());
        }));
        world.set("get_blast_resistance_at_location", method(world, args ->
                LuaValue.valueOf(support.getBlastResistance(support.toLocation(args.arg1())))));
        world.set("is_air_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            return LuaValue.valueOf(location != null && location.getBlock().getType().isAir());
        }));
        world.set("is_passable_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            return LuaValue.valueOf(location != null && location.getBlock().isPassable());
        }));
        world.set("is_passthrough_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            return LuaValue.valueOf(location != null && !location.getBlock().getType().isSolid());
        }));
        world.set("is_on_floor_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            if (location == null) {
                return LuaValue.FALSE;
            }
            return LuaValue.valueOf(!location.getBlock().getType().isSolid()
                    && location.clone().subtract(0, 1, 0).getBlock().getType().isSolid());
        }));
        world.set("is_standing_on_material", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            Material material = support.parseMaterial(args.checkjstring(2));
            if (location == null || material == null) {
                return LuaValue.FALSE;
            }
            return LuaValue.valueOf(location.clone().subtract(0, 1, 0).getBlock().getType() == material);
        }));
        world.set("strike_lightning_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            if (location != null && location.getWorld() != null) {
                LightningSpawnBypass.bypass();
                location.getWorld().strikeLightning(location);
            }
            return LuaValue.NIL;
        }));
        world.set("run_empowered_lightning_task_at_location", method(world, args -> {
            EnderDragonEmpoweredLightningSupport.lightningTask(support.toLocation(args.arg1()));
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
            CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(args.checkjstring(1));
            if (customBossEntity == null) {
                return LuaValue.NIL;
            }
            Location location = args.narg() >= 2 ? support.toLocation(args.arg(2)) : eliteEntity.getLocation();
            int level = args.narg() >= 3 ? args.checkint(3) : eliteEntity.getLevel();
            if (location != null) {
                customBossEntity.spawn(location, level, false);
                if (customBossEntity.getLivingEntity() != null) {
                    return entityTables.createEntityTable(customBossEntity.getLivingEntity());
                }
            }
            return LuaValue.NIL;
        }));
        world.set("spawn_custom_boss_at_location", method(world, args -> {
            CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(args.checkjstring(1));
            if (customBossEntity == null) {
                return LuaValue.NIL;
            }
            Location location = support.toLocation(args.arg(2));
            if (location == null) {
                return LuaValue.NIL;
            }
            LuaTable options = args.narg() >= 3 && args.arg(3).istable() ? args.arg(3).checktable() : new LuaTable();
            boolean silent = options.get("silent").optboolean(false);
            if (options.get("level").isnumber()) {
                customBossEntity.spawn(location, options.get("level").toint(), silent);
            } else {
                customBossEntity.spawn(location, silent);
            }
            if (customBossEntity.getLivingEntity() == null) {
                return LuaValue.NIL;
            }
            if (options.get("add_as_reinforcement").optboolean(false)) {
                eliteEntity.addReinforcement(customBossEntity);
            }
            Vector velocity = support.toVector(options.get("velocity"));
            if (velocity != null) {
                customBossEntity.getLivingEntity().setVelocity(velocity);
            }
            return entityTables.createEntityTable(customBossEntity.getLivingEntity());
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
            LuaTable options = args.narg() >= 3 && args.arg(3).istable() ? args.arg(3).checktable() : new LuaTable();
            Entity entity = location.getWorld().spawnEntity(location, entityType);
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
            if (options.get("on_land").isfunction()) {
                monitorEntityLanding(entity,
                        options.get("on_land").checkfunction(),
                        options.get("max_ticks").optint(20 * 60 * 5));
            }
            return entityTables.createEntityReferenceTable(entity);
        }));
        world.set("spawn_falling_block_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            Material material = support.parseMaterial(args.checkjstring(2));
            if (location == null || location.getWorld() == null || material == null) {
                return LuaValue.NIL;
            }
            LuaTable options = args.narg() >= 3 && args.arg(3).istable() ? args.arg(3).checktable() : new LuaTable();
            FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, material.createBlockData());
            fallingBlock.setDropItem(options.get("drop_item").optboolean(false));
            fallingBlock.setHurtEntities(options.get("hurt_entities").optboolean(false));
            Vector velocity = support.toVector(options.get("velocity"));
            if (velocity != null) {
                fallingBlock.setVelocity(velocity);
            }
            if (options.get("on_land").isfunction()) {
                LuaFunction onLand = options.get("on_land").checkfunction();
                ScriptListener.luaFallingBlocks.put(fallingBlock, (entity, landingLocation) ->
                        callbackInvoker.invoke("a falling block landing callback",
                                onLand,
                                support.toLocationTable(landingLocation),
                                entityTables.createEntityReferenceTable(entity)));
            }
            return entityTables.createEntityReferenceTable(fallingBlock);
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
            Entity firework = support.spawnFirework(support.toLocation(args.arg1()), args.arg(2).checktable());
            return firework == null ? LuaValue.NIL : entityTables.createEntityReferenceTable(firework);
        }));
        world.set("spawn_splash_potion_at_location", method(world, args -> {
            Location location = support.toLocation(args.arg1());
            if (location == null || !args.arg(2).istable()) {
                return LuaValue.NIL;
            }
            Entity thrownPotion = support.spawnSplashPotion(location, args.arg(2).checktable());
            return thrownPotion == null ? LuaValue.NIL : entityTables.createEntityReferenceTable(thrownPotion);
        }));
        world.set("generate_fake_explosion", method(world, args -> {
            List<Location> blockLocations = new java.util.ArrayList<>();
            if (args.arg1().istable()) {
                LuaTable locations = args.arg1().checktable();
                LuaValue key = LuaValue.NIL;
                while (true) {
                    Varargs next = locations.next(key);
                    key = next.arg1();
                    if (key.isnil()) {
                        break;
                    }
                    Location blockLocation = support.toLocation(next.arg(2));
                    if (blockLocation != null) {
                        blockLocations.add(blockLocation);
                    }
                }
            }
            support.generateFakeExplosion(blockLocations, args.narg() >= 2 ? support.toLocation(args.arg(2)) : null);
            return LuaValue.NIL;
        }));
        world.set("spawn_fake_gold_nugget_at_location", method(world, args -> {
            ProjectileDamage.FakeProjectile projectile = support.spawnGoldNuggetProjectile(
                    support.toLocation(args.arg1()),
                    support.toVector(args.arg(2)));
            if (projectile == null) {
                return LuaValue.NIL;
            }
            if (args.narg() >= 3 && args.arg(3).optboolean(false)) {
                projectile.setGravity(true);
            }
            return createFakeProjectileTable(projectile);
        }));
        world.set("run_fake_gold_nugget_damage", method(world, args -> {
            if (!args.arg1().istable()) {
                return LuaValue.NIL;
            }
            List<ProjectileDamage.FakeProjectile> projectiles = new java.util.ArrayList<>();
            LuaTable table = args.arg1().checktable();
            LuaValue key = LuaValue.NIL;
            while (true) {
                Varargs next = table.next(key);
                key = next.arg1();
                if (key.isnil()) {
                    break;
                }
                ProjectileDamage.FakeProjectile projectile = extractFakeProjectile(next.arg(2));
                if (projectile != null) {
                    projectiles.add(projectile);
                }
            }
            support.runGoldNuggetDamage(projectiles);
            return LuaValue.NIL;
        }));
        world.set("generate_player_loot", method(world, args -> {
            support.generatePlayerLoot(args.optint(1, 1));
            return LuaValue.NIL;
        }));
        world.set("drop_bonus_coins", method(world, args -> {
            support.dropBonusCoins(args.optdouble(1, 2D));
            return LuaValue.NIL;
        }));
        return world;
    }

    LuaTable buildSettingsTable() {
        LuaTable settings = new LuaTable();
        settings.set("warning_visual_effects_enabled", method(settings, args ->
                LuaValue.valueOf(MobCombatSettingsConfig.isEnableWarningVisualEffects())));
        return settings;
    }

    // ── World-only helpers ─────────────────────────────────────────────

    private void monitorEntityLanding(Entity entity, LuaFunction callback, int maxTicks) {
        final int[] taskId = new int[1];
        taskId[0] = taskController.runRepeating(1, 1, new Runnable() {
            private int counter = 0;

            @Override
            public void run() {
                if (!entity.isValid() || entity.isOnGround() || counter > maxTicks) {
                    taskController.cancel(taskId[0]);
                    callbackInvoker.invoke("an entity landing callback",
                            callback,
                            support.toLocationTable(entity.getLocation()),
                            entityTables.createEntityReferenceTable(entity));
                    return;
                }
                counter++;
            }
        });
    }

    private LuaTable createFakeProjectileTable(ProjectileDamage.FakeProjectile projectile) {
        LuaTable table = new LuaTable();
        table.set("__fake_projectile", LuaValue.userdataOf(projectile));
        table.set("get_location", method(table, args -> support.toLocationTable(projectile.getLocation())));
        table.set("set_gravity", method(table, args -> {
            projectile.setGravity(args.checkboolean(1));
            return LuaValue.NIL;
        }));
        table.set("remove", method(table, args -> {
            projectile.remove();
            return LuaValue.NIL;
        }));
        table.set("is_removed", method(table, args -> LuaValue.valueOf(projectile.isRemoved())));
        return table;
    }

    private ProjectileDamage.FakeProjectile extractFakeProjectile(LuaValue value) {
        if (!value.istable()) {
            return null;
        }
        LuaValue userdata = value.checktable().get("__fake_projectile");
        if (userdata.isuserdata(ProjectileDamage.FakeProjectile.class)) {
            return (ProjectileDamage.FakeProjectile) userdata.checkuserdata(ProjectileDamage.FakeProjectile.class);
        }
        return null;
    }

    // ── Lua method-call boilerplate ────────────────────────────────────

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
