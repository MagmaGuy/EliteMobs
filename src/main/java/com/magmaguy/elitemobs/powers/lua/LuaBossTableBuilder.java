package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.meta.ProjectileTagger;
import com.magmaguy.elitemobs.powers.specialpowers.ShieldWallSupport;
import com.magmaguy.elitemobs.powers.specialpowers.SpiritWalkSupport;
import com.magmaguy.elitemobs.powers.specialpowers.TrackingFireballSupport;
import com.magmaguy.elitemobs.powers.specialpowers.ZombieNecronomiconSupport;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import com.magmaguy.magmacore.util.ChatColorConverter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.List;
import java.util.Locale;

/**
 * Builds the Lua table exposed as {@code context.boss} to Lua power scripts.
 * <p>
 * Extracted from {@link LuaPowerEntityTables} for navigability.
 */
final class LuaBossTableBuilder {

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;
    private final LuaPowerEntityTables entityTables;
    private final LuaPowerScriptApi.OwnedTaskController taskController;
    private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;

    LuaBossTableBuilder(LuaPowerDefinition definition,
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
        LuaTable boss = new LuaTable();
        boss.set("name", LuaValue.valueOf(eliteEntity.getName() == null ? definition.getFileName() : eliteEntity.getName()));
        boss.set("uuid", LuaValue.valueOf(eliteEntity.getEliteUUID().toString()));
        boss.set("entity_type", LuaValue.valueOf(eliteEntity.getLivingEntity() == null
                ? "UNKNOWN"
                : eliteEntity.getLivingEntity().getType().name()));
        boss.set("is_monster", LuaValue.valueOf(eliteEntity.getLivingEntity() instanceof Monster));
        boss.set("level", LuaValue.valueOf(eliteEntity.getLevel()));
        boss.set("health", LuaValue.valueOf(eliteEntity.getHealth()));
        boss.set("maximum_health", LuaValue.valueOf(eliteEntity.getMaxHealth()));
        boss.set("damager_count", LuaValue.valueOf(eliteEntity.getDamagers().size()));
        boss.set("is_in_combat", LuaValue.valueOf(eliteEntity.isInCombat()));
        boss.set("exists", LuaValue.valueOf(eliteEntity.exists()));
        boss.set("current_location", support.toLocationTable(eliteEntity.getLocation()));
        boss.set("is_alive", method(boss, args -> LuaValue.valueOf(isAlive(eliteEntity.getLivingEntity()) && eliteEntity.exists())));
        boss.set("is_ai_enabled", method(boss, args -> LuaValue.valueOf(eliteEntity.getLivingEntity() != null
                && eliteEntity.getLivingEntity().hasAI())));
        boss.set("get_health", method(boss, args -> LuaValue.valueOf(eliteEntity.getHealth())));
        boss.set("get_maximum_health", method(boss, args -> LuaValue.valueOf(eliteEntity.getMaxHealth())));
        boss.set("get_damager_count", method(boss, args -> LuaValue.valueOf(eliteEntity.getDamagers().size())));
        boss.set("get_location", method(boss, args -> support.toLocationTable(eliteEntity.getLocation())));
        boss.set("get_eye_location", method(boss, args -> eliteEntity.getLivingEntity() == null
                ? LuaValue.NIL
                : support.toLocationTable(eliteEntity.getLivingEntity().getEyeLocation())));
        boss.set("get_ender_dragon_phase", method(boss, args -> eliteEntity.getLivingEntity() instanceof EnderDragon enderDragon
                ? LuaValue.valueOf(enderDragon.getPhase().name())
                : LuaValue.NIL));
        boss.set("set_ender_dragon_phase", method(boss, args -> {
            if (eliteEntity.getLivingEntity() instanceof EnderDragon enderDragon) {
                EnderDragon.Phase phase = support.parseEnum(args.checkjstring(1), EnderDragon.Phase.class, null);
                if (phase != null) {
                    enderDragon.setPhase(phase);
                }
            }
            return LuaValue.NIL;
        }));
        boss.set("get_height", method(boss, args -> LuaValue.valueOf(eliteEntity.getLivingEntity() == null
                ? 0
                : eliteEntity.getLivingEntity().getHeight())));
        boss.set("add_tag", method(boss, args -> {
            eliteEntity.addTag(args.checkjstring(1));
            entityTables.scheduleTagRemoval(eliteEntity.getLivingEntity(), args.optint(2, 0), args.checkjstring(1));
            return LuaValue.NIL;
        }));
        boss.set("remove_tag", method(boss, args -> {
            eliteEntity.removeTag(args.checkjstring(1));
            return LuaValue.NIL;
        }));
        boss.set("has_tag", method(boss, args -> LuaValue.valueOf(eliteEntity.hasTag(args.checkjstring(1)))));
        boss.set("restore_health", method(boss, args -> {
            eliteEntity.heal(args.checkdouble(1));
            return LuaValue.NIL;
        }));
        boss.set("deal_damage", method(boss, args -> {
            if (eliteEntity.getLivingEntity() != null) {
                eliteEntity.getLivingEntity().damage(args.checkdouble(1));
            }
            return LuaValue.NIL;
        }));
        boss.set("teleport_to_location", method(boss, args -> {
            Location destination = support.toLocation(args.arg1());
            if (eliteEntity.getLivingEntity() != null && destination != null) {
                eliteEntity.getLivingEntity().teleport(destination);
            }
            return LuaValue.NIL;
        }));
        boss.set("despawn", method(boss, args -> {
            eliteEntity.remove(RemovalReason.OTHER);
            return LuaValue.NIL;
        }));
        boss.set("set_ai_enabled", method(boss, args -> {
            entityTables.applyAiState(eliteEntity.getLivingEntity(), args.checkboolean(1), args.optint(2, 0));
            return LuaValue.NIL;
        }));
        boss.set("play_sound_at_self", method(boss, args -> {
            support.playSound(eliteEntity.getLocation(), args.checkjstring(1), resolveVolume(args), resolvePitch(args));
            return LuaValue.NIL;
        }));
        boss.set("spawn_particle_at_self", method(boss, args -> {
            support.spawnParticle(eliteEntity.getLocation(), args.arg(1), args.optint(2, 1));
            return LuaValue.NIL;
        }));
        boss.set("spawn_particles_at_location", method(boss, args -> {
            support.spawnParticle(support.toLocation(args.arg1()), particleSpec(args), args.optint(3, 1));
            return LuaValue.NIL;
        }));
        boss.set("set_velocity_vector", method(boss, args -> {
            if (eliteEntity.getLivingEntity() != null) {
                Vector velocity = support.toVector(args.arg1());
                if (velocity != null) {
                    eliteEntity.getLivingEntity().setVelocity(velocity);
                }
            }
            return LuaValue.NIL;
        }));
        boss.set("is_on_ground", method(boss, args -> LuaValue.valueOf(eliteEntity.getLivingEntity() != null
                && eliteEntity.getLivingEntity().isOnGround())));
        boss.set("set_custom_name", method(boss, args -> {
            if (eliteEntity.getLivingEntity() != null) {
                eliteEntity.getLivingEntity().setCustomName(ChatColorConverter.convert(args.checkjstring(1)));
            }
            return LuaValue.NIL;
        }));
        boss.set("reset_custom_name", method(boss, args -> {
            if (eliteEntity.getLivingEntity() != null) {
                eliteEntity.getLivingEntity().setCustomName(eliteEntity.getName());
            }
            return LuaValue.NIL;
        }));
        boss.set("set_custom_name_visible", method(boss, args -> {
            if (eliteEntity.getLivingEntity() != null) {
                eliteEntity.getLivingEntity().setCustomNameVisible(args.checkboolean(1));
            }
            return LuaValue.NIL;
        }));
        boss.set("face_direction_or_location", method(boss, args -> {
            if (eliteEntity.getLivingEntity() == null) {
                return LuaValue.NIL;
            }
            Vector direction = support.toVector(args.arg1());
            if (direction == null && args.arg1().istable()) {
                Location destination = support.toLocation(args.arg1());
                if (destination != null) {
                    direction = destination.toVector().subtract(eliteEntity.getLivingEntity().getLocation().toVector());
                }
            }
            if (direction != null && direction.lengthSquared() > 0) {
                Location location = eliteEntity.getLivingEntity().getLocation();
                location.setDirection(direction);
                eliteEntity.getLivingEntity().teleport(location);
            }
            return LuaValue.NIL;
        }));
        boss.set("play_model_animation", method(boss, args -> {
            if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomModel() != null) {
                customBossEntity.getCustomModel().playAnimationByName(args.checkjstring(1));
            }
            return LuaValue.NIL;
        }));
        boss.set("navigate_to_location", method(boss, args -> {
            if (eliteEntity instanceof CustomBossEntity customBossEntity) {
                Location destination = support.toLocation(args.arg1());
                if (destination != null) {
                    Navigation.navigateTo(customBossEntity, args.optdouble(2, 1.0), destination, args.optboolean(3, false), args.optint(4, 0));
                }
            }
            return LuaValue.NIL;
        }));
        boss.set("start_tracking_fireball_system", method(boss, args -> {
            if (eliteEntity.getLivingEntity() instanceof Monster monster) {
                TrackingFireballSupport.begin(monster, args.optdouble(1, 0.5));
            }
            return LuaValue.NIL;
        }));
        boss.set("handle_spirit_walk_damage", method(boss, args -> {
            EntityDamageEvent.DamageCause cause = support.parseEnum(args.checkjstring(1), EntityDamageEvent.DamageCause.class, null);
            SpiritWalkSupport.handleBossDamaged(eliteEntity, cause);
            return LuaValue.NIL;
        }));
        boss.set("shield_wall_is_active", method(boss, args -> LuaValue.valueOf(ShieldWallSupport.isActive(eliteEntity))));
        boss.set("initialize_shield_wall", method(boss, args -> {
            ShieldWallSupport.initialize(eliteEntity, args.optint(1, 1));
            return LuaValue.NIL;
        }));
        boss.set("shield_wall_absorb_damage", method(boss, args -> {
            Player player = support.resolvePlayerReference(args.arg1());
            if (player == null) {
                return LuaValue.FALSE;
            }
            return LuaValue.valueOf(ShieldWallSupport.preventDamage(player, eliteEntity, args.checkdouble(2)));
        }));
        boss.set("deactivate_shield_wall", method(boss, args -> {
            ShieldWallSupport.deactivate(eliteEntity);
            return LuaValue.NIL;
        }));
        boss.set("start_zombie_necronomicon", method(boss, args -> {
            LivingEntity target = support.resolveLivingEntityReference(args.arg1());
            if (target != null) {
                ZombieNecronomiconSupport.handleBossDamagedByPlayer(eliteEntity, target, args.checkjstring(2));
            }
            return LuaValue.NIL;
        }));
        boss.set("send_message", method(boss, args -> {
            if (eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
                return LuaValue.NIL;
            }
            String message = ChatColorConverter.convert(args.checkjstring(1));
            double range = args.optdouble(2, 20);
            double rangeSquared = range * range;
            for (Player player : eliteEntity.getLocation().getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(eliteEntity.getLocation()) <= rangeSquared) {
                    player.sendMessage(message);
                }
            }
            return LuaValue.NIL;
        }));
        boss.set("get_nearby_players", method(boss, args -> getNearbyPlayers(args.checkdouble(1))));
        boss.set("get_target_player", method(boss, args -> {
            if (eliteEntity.getLivingEntity() instanceof Mob mob && mob.getTarget() instanceof Player player) {
                return entityTables.createPlayerTable(player);
            }
            return LuaValue.NIL;
        }));
        boss.set("get_nearby_players_in_zone", method(boss, args -> getPlayersInZone(args.arg1())));
        boss.set("spawn_particles_in_zone", method(boss, args -> {
            spawnParticlesInZone(args.arg1(), false, particleSpec(args), args.optdouble(8, 1.0));
            return LuaValue.NIL;
        }));
        boss.set("spawn_particles_in_zone_border", method(boss, args -> {
            spawnParticlesInZone(args.arg1(), true, particleSpec(args), args.optdouble(8, 1.0));
            return LuaValue.NIL;
        }));
        boss.set("get_particles_from_self_toward_zone", method(boss, args ->
                buildDirectionalParticles(args.arg1(), args.checkjstring(2), eliteEntity.getLocation(), false, args.optdouble(3, 0.1))));
        boss.set("get_particles_toward_self", method(boss, args ->
                buildDirectionalParticles(args.arg1(), args.checkjstring(2), eliteEntity.getLocation(), true, args.optdouble(args.narg(), 0.1))));
        boss.set("spawn_particles_with_vector", method(boss, args -> {
            spawnDirectionalParticles(args.arg1());
            return LuaValue.NIL;
        }));
        boss.set("summon_reinforcement", method(boss, args -> {
            Location location = chooseZoneLocation(args.arg(2));
            if (location == null) {
                location = eliteEntity.getLocation();
            }
            CustomBossEntity reinforcement = CustomSummonPower.summonReinforcement(eliteEntity, location, args.checkjstring(1), args.optint(3, 0));
            return reinforcement == null || reinforcement.getLivingEntity() == null
                    ? LuaValue.NIL
                    : entityTables.createEntityTable(reinforcement.getLivingEntity());
        }));
        boss.set("summon_projectile", method(boss, args -> {
            Location origin = support.toLocation(args.arg(2));
            Location destination = support.toLocation(args.arg(3));
            if (origin == null || destination == null || origin.getWorld() == null) {
                return LuaValue.NIL;
            }
            EntityType entityType = support.parseEnum(args.checkjstring(1), EntityType.class, null);
            if (entityType == null) {
                return LuaValue.NIL;
            }
            LuaTable options = args.narg() >= 5 && args.arg(5).istable() ? args.arg(5).checktable() : new LuaTable();
            Vector velocity = destination.toVector().subtract(origin.toVector());
            if (velocity.lengthSquared() > 0) {
                velocity.normalize().multiply(args.optdouble(4, 1.0));
            }
            Entity projectile;
            boolean spawnAtOrigin = options.get("spawn_at_origin").optboolean(false);
            boolean directionOnly = options.get("direction_only").optboolean(false);
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            if (entityClass != null
                    && Projectile.class.isAssignableFrom(entityClass)
                    && eliteEntity.getLivingEntity() != null
                    && !spawnAtOrigin) {
                projectile = eliteEntity.getLivingEntity().launchProjectile(entityClass.asSubclass(Projectile.class), velocity);
                ((Projectile) projectile).setShooter(eliteEntity.getLivingEntity());
                if (projectile instanceof Fireball fireball && velocity.lengthSquared() > 0) {
                    fireball.setDirection(velocity);
                }
            } else {
                projectile = origin.getWorld().spawnEntity(origin, entityType);
                if (projectile instanceof Projectile spawnedProjectile && eliteEntity.getLivingEntity() != null) {
                    spawnedProjectile.setShooter(eliteEntity.getLivingEntity());
                }
                if (velocity.lengthSquared() > 0) {
                    if (projectile instanceof Fireball fireball) {
                        fireball.setDirection(velocity);
                        if (!directionOnly) {
                            projectile.setVelocity(velocity);
                        }
                    } else {
                        projectile.setVelocity(velocity);
                    }
                }
            }
            if (projectile instanceof Projectile trackedProjectile && options.get("track").optboolean(true)) {
                EntityTracker.registerProjectileEntity(trackedProjectile);
            }
            applyGenericSpawnOptions(projectile, options);
            if (options.get("on_land").isfunction()) {
                monitorEntityLanding(projectile,
                        options.get("on_land").checkfunction(),
                        options.get("max_ticks").optint(20 * 60 * 5));
            }
            return entityTables.createEntityReferenceTable(projectile);
        }));
        entityTables.addEntityEffectMethods(boss, eliteEntity.getLivingEntity());
        return boss;
    }

    // ── Boss-only helpers ──────────────────────────────────────────────

    private LuaValue getNearbyPlayers(double range) {
        LuaTable results = new LuaTable();
        if (eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
            return results;
        }
        int index = 1;
        double rangeSquared = range * range;
        for (Player player : eliteEntity.getLocation().getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(eliteEntity.getLocation()) <= rangeSquared) {
                results.set(index++, entityTables.createPlayerTable(player));
            }
        }
        return results;
    }

    private LuaValue getPlayersInZone(LuaValue zoneValue) {
        LuaTable results = new LuaTable();
        Shape shape = support.createShape(zoneValue);
        if (shape == null || eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
            return results;
        }
        int index = 1;
        for (Player player : eliteEntity.getLocation().getWorld().getPlayers()) {
            if (shape.contains(player)) {
                results.set(index++, entityTables.createPlayerTable(player));
            }
        }
        return results;
    }

    private void spawnParticlesInZone(LuaValue zoneValue, boolean border, LuaTable particle, double coverage) {
        Shape shape = support.createShape(zoneValue);
        if (shape == null) {
            return;
        }
        for (Location location : sampleLocations(shape, border, coverage)) {
            support.spawnParticle(location, particle, particle.get("amount").optint(1));
        }
    }

    private LuaTable buildDirectionalParticles(LuaValue zoneValue, String particleKey, Location anchor, boolean towardAnchor, double speed) {
        LuaTable results = new LuaTable();
        Shape shape = support.createShape(zoneValue);
        if (shape == null || anchor == null) {
            return results;
        }
        int index = 1;
        for (Location location : sampleLocations(shape, false, 0.2)) {
            Vector direction = towardAnchor
                    ? anchor.toVector().subtract(location.toVector())
                    : location.toVector().subtract(anchor.toVector());
            if (direction.lengthSquared() <= 0) {
                continue;
            }
            direction.normalize();
            LuaTable particle = new LuaTable();
            particle.set("location", support.toLocationTable(location));
            particle.set("particle", LuaValue.valueOf(particleKey));
            particle.set("amount", LuaValue.ZERO);
            particle.set("x", LuaValue.valueOf(direction.getX()));
            particle.set("y", LuaValue.valueOf(direction.getY()));
            particle.set("z", LuaValue.valueOf(direction.getZ()));
            particle.set("speed", LuaValue.valueOf(speed));
            results.set(index++, particle);
        }
        return results;
    }

    private void spawnDirectionalParticles(LuaValue particlesValue) {
        if (!particlesValue.istable()) {
            return;
        }
        LuaTable particles = particlesValue.checktable();
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = particles.next(key);
            key = next.arg1();
            if (key.isnil()) {
                break;
            }
            LuaValue particleValue = next.arg(2);
            if (!particleValue.istable()) {
                continue;
            }
            LuaTable particle = particleValue.checktable();
            support.spawnParticle(support.toLocation(particle.get("location")), particle, particle.get("amount").optint(1));
        }
    }

    private List<Location> sampleLocations(Shape shape, boolean border, double coverage) {
        List<Location> source = border ? shape.getEdgeLocations() : shape.getLocations();
        if (source.isEmpty()) {
            return source;
        }
        double normalizedCoverage = Math.max(0.01, Math.min(1.0, coverage));
        List<Location> sampled = new java.util.ArrayList<>();
        for (Location location : source) {
            if (Math.random() <= normalizedCoverage) {
                sampled.add(location);
            }
        }
        if (sampled.isEmpty()) {
            sampled.add(source.get(0));
        }
        return sampled;
    }

    private Location chooseZoneLocation(LuaValue zoneValue) {
        Shape shape = support.createShape(zoneValue);
        if (shape == null) {
            return support.toLocation(zoneValue);
        }
        List<Location> locations = shape.getLocations();
        if (locations.isEmpty()) {
            return shape.getCenter();
        }
        return locations.get((int) (Math.random() * locations.size()));
    }

    private LuaTable particleSpec(Varargs args) {
        LuaTable particle = new LuaTable();
        particle.set("particle", args.arg(2).isstring() ? args.arg(2) : args.arg(1));
        particle.set("amount", LuaValue.valueOf(args.narg() >= 3 ? args.arg(3).optint(1) : 1));
        particle.set("x", LuaValue.valueOf(args.narg() >= 4 ? args.arg(4).optdouble(0) : 0));
        particle.set("y", LuaValue.valueOf(args.narg() >= 5 ? args.arg(5).optdouble(0) : 0));
        particle.set("z", LuaValue.valueOf(args.narg() >= 6 ? args.arg(6).optdouble(0) : 0));
        particle.set("speed", LuaValue.valueOf(args.narg() >= 7 ? args.arg(7).optdouble(0) : 0));
        return particle;
    }

    private void applyGenericSpawnOptions(Entity entity, LuaTable options) {
        if (entity instanceof Projectile projectile && options.get("custom_damage").isnumber()) {
            ProjectileTagger.tagProjectileWithCustomDamage(projectile, options.get("custom_damage").todouble());
        }
        if (entity instanceof Projectile projectile && options.get("detonation_power").isstring()) {
            com.magmaguy.elitemobs.combatsystem.EliteProjectile.signExplosiveWithPower(projectile, options.get("detonation_power").tojstring());
        }
        if (entity instanceof Fireball fireball) {
            if (options.get("yield").isnumber()) {
                fireball.setYield((float) options.get("yield").todouble());
            }
            if (!options.get("incendiary").isnil()) {
                fireball.setIsIncendiary(options.get("incendiary").optboolean(true));
            }
        }
        if (!options.get("gravity").isnil()) {
            entity.setGravity(options.get("gravity").optboolean(true));
        }
        if (!options.get("glowing").isnil()) {
            entity.setGlowing(options.get("glowing").optboolean(false));
        }
        if (!options.get("invulnerable").isnil()) {
            entity.setInvulnerable(options.get("invulnerable").optboolean(false));
        }
        if (!options.get("persistent").isnil()) {
            entity.setPersistent(options.get("persistent").optboolean(true));
        }
        int duration = options.get("duration").optint(0);
        if (duration > 0) {
            GameClock.scheduleLater(duration, () -> {
                if (entity.isValid()) {
                    entity.remove();
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

    private void monitorEntityLanding(Entity entity, LuaFunction callback, int maxTicks) {
        final int[] taskId = new int[1];
        taskId[0] = taskController.runRepeating(1, 1, new Runnable() {
            private int counter = 0;

            @Override
            public void run() {
                if (!entity.isValid() || entity.isOnGround() || counter > maxTicks) {
                    taskController.cancel(taskId[0]);
                    callbackInvoker.invoke("a launched entity landing callback",
                            callback,
                            support.toLocationTable(entity.getLocation()),
                            entityTables.createEntityReferenceTable(entity));
                    return;
                }
                counter++;
            }
        });
    }

    // ── Small copied helpers ───────────────────────────────────────────

    private boolean isAlive(LivingEntity livingEntity) {
        return livingEntity != null && livingEntity.isValid() && !livingEntity.isDead();
    }

    private float resolveVolume(Varargs args) {
        if (args.narg() >= 2 && args.arg(2).istable()) {
            return (float) args.arg(2).checktable().get("volume").optdouble(1.0);
        }
        return support.getFloat(args, 2, 1f);
    }

    private float resolvePitch(Varargs args) {
        if (args.narg() >= 2 && args.arg(2).istable()) {
            return (float) args.arg(2).checktable().get("pitch").optdouble(1.0);
        }
        return support.getFloat(args, 3, 1f);
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
