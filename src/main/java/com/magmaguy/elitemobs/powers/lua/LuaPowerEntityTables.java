package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.EliteDamageEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.ScriptZoneEnterEvent;
import com.magmaguy.elitemobs.api.ScriptZoneLeaveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class LuaPowerEntityTables {

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;
    private final LuaPowerScriptApi.OwnedTaskController taskController;
    private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;

    LuaPowerEntityTables(LuaPowerDefinition definition,
                         EliteEntity eliteEntity,
                         LuaPowerSupport support,
                         LuaPowerScriptApi.OwnedTaskController taskController,
                         LuaPowerScriptApi.CallbackInvoker callbackInvoker) {
        this.definition = definition;
        this.eliteEntity = eliteEntity;
        this.support = support;
        this.taskController = taskController;
        this.callbackInvoker = callbackInvoker;
    }

    LuaTable createBossTable() {
        LuaTable boss = new LuaTable();
        boss.set("name", LuaValue.valueOf(eliteEntity.getName() == null ? definition.getFileName() : eliteEntity.getName()));
        boss.set("level", LuaValue.valueOf(eliteEntity.getLevel()));
        boss.set("health", LuaValue.valueOf(eliteEntity.getHealth()));
        boss.set("maximum_health", LuaValue.valueOf(eliteEntity.getMaxHealth()));
        boss.set("is_in_combat", LuaValue.valueOf(eliteEntity.isInCombat()));
        boss.set("exists", LuaValue.valueOf(eliteEntity.exists()));
        boss.set("current_location", support.toLocationTable(eliteEntity.getLocation()));
        boss.set("is_alive", method(boss, args -> LuaValue.valueOf(isAlive(eliteEntity.getLivingEntity()) && eliteEntity.exists())));
        boss.set("get_location", method(boss, args -> support.toLocationTable(eliteEntity.getLocation())));
        boss.set("add_tag", method(boss, args -> {
            eliteEntity.addTag(args.checkjstring(1));
            scheduleTagRemoval(eliteEntity.getLivingEntity(), args.optint(2, 0), args.checkjstring(1));
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
            applyAiState(eliteEntity.getLivingEntity(), args.checkboolean(1), args.optint(2, 0));
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
            support.spawnParticle(resolveLocationArgument(args.arg1()), particleSpec(args), args.optint(3, 1));
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
                return createPlayerTable(player);
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
                    : createEntityTable(reinforcement.getLivingEntity());
        }));
        boss.set("summon_projectile", method(boss, args -> {
            Location origin = resolveLocationArgument(args.arg(2));
            Location destination = resolveLocationArgument(args.arg(3));
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
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            if (entityClass != null
                    && Projectile.class.isAssignableFrom(entityClass)
                    && eliteEntity.getLivingEntity() != null) {
                projectile = eliteEntity.getLivingEntity().launchProjectile(entityClass.asSubclass(Projectile.class), velocity);
                ((Projectile) projectile).setShooter(eliteEntity.getLivingEntity());
                if (projectile instanceof Fireball fireball && velocity.lengthSquared() > 0) {
                    fireball.setDirection(velocity);
                }
            } else {
                projectile = origin.getWorld().spawnEntity(origin, entityType);
                if (velocity.lengthSquared() > 0) {
                    projectile.setVelocity(velocity);
                }
            }
            applyGenericSpawnOptions(projectile, options);
            if (options.get("on_land").isfunction()) {
                monitorEntityLanding(projectile,
                        options.get("on_land").checkfunction(),
                        options.get("max_ticks").optint(20 * 60 * 5));
            }
            return createEntityReferenceTable(projectile);
        }));
        addEntityEffectMethods(boss, eliteEntity.getLivingEntity());
        return boss;
    }

    LuaTable createEventTable(Event event) {
        LuaTable eventTable = new LuaTable();
        if (event instanceof EliteDamageEvent eliteDamageEvent) {
            eventTable.set("damage_amount", LuaValue.valueOf(eliteDamageEvent.getDamage()));
            eventTable.set("cancel_event", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    eliteDamageEvent.setCancelled(true);
                    return LuaValue.NIL;
                }
            });
            eventTable.set("set_damage_amount", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    eliteDamageEvent.setDamage(args.checkdouble(1));
                    return LuaValue.NIL;
                }
            });
            eventTable.set("multiply_damage_amount", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    eliteDamageEvent.setDamage(eliteDamageEvent.getDamage() * args.checkdouble(1));
                    return LuaValue.NIL;
                }
            });
        } else if (event instanceof Cancellable cancellable) {
            eventTable.set("cancel_event", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    cancellable.setCancelled(true);
                    return LuaValue.NIL;
                }
            });
        }
        if (event instanceof EliteMobSpawnEvent spawnEvent) {
            eventTable.set("spawn_reason", LuaValue.valueOf(spawnEvent.getReason().name()));
        }
        if (event instanceof ScriptZoneEnterEvent zoneEnterEvent) {
            eventTable.set("entity", createEntityTable(zoneEnterEvent.getEntity()));
        } else if (event instanceof ScriptZoneLeaveEvent zoneLeaveEvent) {
            eventTable.set("entity", createEntityTable(zoneLeaveEvent.getEntity()));
        }
        return eventTable;
    }

    LuaTable createEntityTable(LivingEntity livingEntity) {
        return livingEntity instanceof Player player ? createPlayerTable(player) : createLivingEntityTable(livingEntity);
    }

    LuaValue createEntityReferenceTable(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return createEntityTable(livingEntity);
        }
        LuaTable table = new LuaTable();
        table.set("name", LuaValue.valueOf(entity.getName()));
        table.set("uuid", LuaValue.valueOf(entity.getUniqueId().toString()));
        table.set("entity_type", LuaValue.valueOf(entity.getType().name()));
        table.set("is_player", LuaValue.FALSE);
        table.set("is_elite", LuaValue.FALSE);
        table.set("is_valid", method(table, args -> LuaValue.valueOf(entity.isValid())));
        table.set("current_location", support.toLocationTable(entity.getLocation()));
        table.set("get_location", method(table, args -> support.toLocationTable(entity.getLocation())));
        table.set("teleport_to_location", method(table, args -> {
            Location destination = support.toLocation(args.arg1());
            if (destination != null) {
                entity.teleport(destination);
            }
            return LuaValue.NIL;
        }));
        table.set("set_velocity_vector", method(table, args -> {
            Vector velocity = support.toVector(args.arg1());
            if (velocity != null) {
                entity.setVelocity(velocity);
            }
            return LuaValue.NIL;
        }));
        table.set("remove", method(table, args -> {
            entity.remove();
            return LuaValue.NIL;
        }));
        return table;
    }

    LuaTable createPlayerTable(Player player) {
        LuaTable table = createLivingEntityTable(player);
        table.set("send_message", method(table, args -> {
            player.sendMessage(ChatColorConverter.convert(args.checkjstring(1)));
            return LuaValue.NIL;
        }));
        table.set("show_action_bar", method(table, args -> {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColorConverter.convert(args.checkjstring(1))));
            return LuaValue.NIL;
        }));
        table.set("show_title", method(table, args -> {
            player.sendTitle(ChatColorConverter.convert(args.checkjstring(1)),
                    ChatColorConverter.convert(args.optjstring(2, "")),
                    args.optint(3, 10),
                    args.optint(4, 40),
                    args.optint(5, 10));
            return LuaValue.NIL;
        }));
        VarArgFunction showBossBar = method(table, args -> {
            String title = ChatColorConverter.convert(args.checkjstring(1));
            BarColor color = support.parseEnum(args.optjstring(2, "WHITE"), BarColor.class, BarColor.WHITE);
            BarStyle style = support.parseEnum(args.optjstring(3, "SOLID"), BarStyle.class, BarStyle.SOLID);
            int duration = args.optint(4, 40);
            BossBar bossBar = Bukkit.createBossBar(title, color, style);
            bossBar.addPlayer(player);
            if (duration > 0) {
                GameClock.scheduleLater(duration, bossBar::removeAll);
            }
            return LuaValue.NIL;
        });
        table.set("show_boss_bar", showBossBar);
        VarArgFunction runCommand = method(table, args -> {
            player.performCommand(args.checkjstring(1));
            return LuaValue.NIL;
        });
        table.set("run_command", runCommand);
        return table;
    }

    LuaTable createLivingEntityTable(LivingEntity livingEntity) {
        LuaTable entity = new LuaTable();
        entity.set("name", LuaValue.valueOf(livingEntity.getName()));
        entity.set("uuid", LuaValue.valueOf(livingEntity.getUniqueId().toString()));
        entity.set("entity_type", LuaValue.valueOf(livingEntity.getType().name()));
        entity.set("is_player", LuaValue.valueOf(livingEntity instanceof Player));
        entity.set("is_elite", LuaValue.valueOf(EntityTracker.getEliteMobEntity(livingEntity) != null));
        entity.set("is_valid", LuaValue.valueOf(livingEntity.isValid()));
        entity.set("is_alive", method(entity, args -> LuaValue.valueOf(isAlive(livingEntity))));
        entity.set("health", LuaValue.valueOf(livingEntity.getHealth()));
        entity.set("maximum_health", LuaValue.valueOf(Objects.requireNonNull(livingEntity.getAttribute(Attribute.MAX_HEALTH)).getValue()));
        entity.set("current_location", support.toLocationTable(livingEntity.getLocation()));
        entity.set("get_location", method(entity, args -> support.toLocationTable(livingEntity.getLocation())));
        entity.set("deal_damage", method(entity, args -> {
            livingEntity.damage(args.checkdouble(1));
            return LuaValue.NIL;
        }));
        entity.set("restore_health", method(entity, args -> {
            double amount = args.checkdouble(1);
            EliteEntity trackedElite = EntityTracker.getEliteMobEntity(livingEntity);
            if (trackedElite != null) {
                trackedElite.heal(amount);
            } else {
                double maxHealth = AttributeManager.getAttributeBaseValue(livingEntity, "generic_max_health");
                livingEntity.setHealth(Math.min(maxHealth, livingEntity.getHealth() + amount));
            }
            return LuaValue.NIL;
        }));
        entity.set("play_sound_at_entity", method(entity, args -> {
            support.playSound(livingEntity.getLocation(), args.checkjstring(1), resolveVolume(args), resolvePitch(args));
            return LuaValue.NIL;
        }));
        entity.set("teleport_to_location", method(entity, args -> {
            Location destination = support.toLocation(args.arg1());
            if (destination != null) {
                livingEntity.teleport(destination);
            }
            return LuaValue.NIL;
        }));
        entity.set("set_velocity_vector", method(entity, args -> {
            Vector velocity = support.toVector(args.arg1());
            if (velocity != null) {
                livingEntity.setVelocity(velocity);
            }
            return LuaValue.NIL;
        }));
        entity.set("apply_push_vector", method(entity, args -> {
            Vector velocity = support.toVector(args.arg1());
            boolean additive = args.optboolean(2, false);
            int delay = args.optint(3, 1);
            if (velocity == null) {
                return LuaValue.NIL;
            }
            GameClock.scheduleLater(delay, () -> {
                if (!livingEntity.isValid()) {
                    return;
                }
                livingEntity.setVelocity(additive ? livingEntity.getVelocity().add(velocity) : velocity);
            });
            return LuaValue.NIL;
        }));
        entity.set("set_ai_enabled", method(entity, args -> {
            applyAiState(livingEntity, args.checkboolean(1), args.optint(2, 0));
            return LuaValue.NIL;
        }));
        entity.set("set_awareness_enabled", method(entity, args -> {
            if (livingEntity instanceof Mob mob) {
                boolean aware = args.checkboolean(1);
                mob.setAware(aware);
                int duration = args.optint(2, 0);
                if (duration > 0) {
                    GameClock.scheduleLater(duration, () -> {
                        if (mob.isValid()) {
                            mob.setAware(!aware);
                        }
                    });
                }
            }
            return LuaValue.NIL;
        }));
        entity.set("face_direction_or_location", method(entity, args -> {
            Vector direction = support.toVector(args.arg1());
            if (direction == null && args.arg1().istable()) {
                Location destination = support.toLocation(args.arg1());
                if (destination != null && livingEntity.getLocation().getWorld() != null &&
                        livingEntity.getLocation().getWorld().getUID().equals(destination.getWorld().getUID())) {
                    direction = destination.toVector().subtract(livingEntity.getLocation().toVector());
                }
            }
            if (direction != null && direction.lengthSquared() > 0) {
                Location location = livingEntity.getLocation();
                location.setDirection(direction);
                livingEntity.teleport(location);
            }
            return LuaValue.NIL;
        }));
        entity.set("play_model_animation", method(entity, args -> {
            EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
            if (entityRef instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomModel() != null) {
                customBossEntity.getCustomModel().playAnimationByName(args.checkjstring(1));
            }
            return LuaValue.NIL;
        }));
        entity.set("set_scale", method(entity, args -> {
            double scale = args.checkdouble(1);
            int duration = args.optint(2, 0);
            AttributeInstance attribute = AttributeManager.getAttributeInstance(livingEntity, "generic_scale");
            if (attribute != null) {
                attribute.setBaseValue(scale);
                if (duration > 0) {
                    GameClock.scheduleLater(duration, () -> attribute.setBaseValue(1.0));
                }
            }
            return LuaValue.NIL;
        }));
        entity.set("set_invulnerable", method(entity, args -> {
            support.applyInvulnerable(livingEntity, args.checkboolean(1), args.optint(2, 0));
            return LuaValue.NIL;
        }));
        entity.set("remove_elite", method(entity, args -> {
            EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
            if (entityRef != null) {
                entityRef.remove(RemovalReason.OTHER);
            }
            return LuaValue.NIL;
        }));
        entity.set("navigate_to_location", method(entity, args -> {
            EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
            if (entityRef instanceof CustomBossEntity customBossEntity) {
                Location destination = support.toLocation(args.arg1());
                if (destination != null) {
                    Navigation.navigateTo(customBossEntity, args.optdouble(2, 1.0), destination, args.optboolean(3, false), args.optint(4, 0));
                }
            }
            return LuaValue.NIL;
        }));
        entity.set("add_tag", method(entity, args -> {
            support.applyTag(livingEntity, args.checkjstring(1), true);
            scheduleTagRemoval(livingEntity, args.optint(2, 0), args.checkjstring(1));
            return LuaValue.NIL;
        }));
        entity.set("remove_tag", method(entity, args -> {
            support.applyTag(livingEntity, args.checkjstring(1), false);
            return LuaValue.NIL;
        }));
        entity.set("has_tag", method(entity, args -> LuaValue.valueOf(support.hasTag(livingEntity, args.checkjstring(1)))));
        entity.set("push_relative_to", method(entity, args -> {
            Location source = resolveLocationArgument(args.arg1());
            if (source == null || !livingEntity.isValid()) {
                return LuaValue.NIL;
            }
            Vector relativePush = livingEntity.getLocation().toVector().subtract(source.toVector());
            if (relativePush.lengthSquared() > 0) {
                relativePush.normalize().multiply(args.optdouble(2, 1.0));
            }
            relativePush.add(new Vector(args.optdouble(3, 0), args.optdouble(4, 0), args.optdouble(5, 0)));
            livingEntity.setVelocity(relativePush);
            return LuaValue.NIL;
        }));
        addEntityEffectMethods(entity, livingEntity);
        return entity;
    }

    private boolean isAlive(LivingEntity livingEntity) {
        return livingEntity != null && livingEntity.isValid() && !livingEntity.isDead();
    }

    private void applyAiState(LivingEntity livingEntity, boolean targetValue, int duration) {
        if (livingEntity == null) {
            return;
        }
        livingEntity.setAI(targetValue);
        if (duration > 0) {
            GameClock.scheduleLater(duration, () -> {
                if (livingEntity.isValid()) {
                    livingEntity.setAI(!targetValue);
                }
            });
        }
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

    private void scheduleTagRemoval(LivingEntity livingEntity, int duration, String tag) {
        if (livingEntity == null || duration <= 0) {
            return;
        }
        GameClock.scheduleLater(duration, () -> {
            if (livingEntity.isValid()) {
                support.applyTag(livingEntity, tag, false);
            }
        });
    }

    private LuaValue getNearbyPlayers(double range) {
        LuaTable results = new LuaTable();
        if (eliteEntity.getLocation() == null || eliteEntity.getLocation().getWorld() == null) {
            return results;
        }
        int index = 1;
        double rangeSquared = range * range;
        for (Player player : eliteEntity.getLocation().getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(eliteEntity.getLocation()) <= rangeSquared) {
                results.set(index++, createPlayerTable(player));
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
                results.set(index++, createPlayerTable(player));
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
            support.spawnParticle(resolveLocationArgument(particle.get("location")), particle, particle.get("amount").optint(1));
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
            return resolveLocationArgument(zoneValue);
        }
        List<Location> locations = shape.getLocations();
        if (locations.isEmpty()) {
            return shape.getCenter();
        }
        return locations.get((int) (Math.random() * locations.size()));
    }

    private Location resolveLocationArgument(LuaValue value) {
        return support.toLocation(value);
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

    private void addEntityEffectMethods(LuaTable entityTable, LivingEntity livingEntity) {
        entityTable.set("apply_potion_effect", method(entityTable, args -> {
            support.applyPotionEffect(livingEntity, args.checkjstring(1), args.checkint(2), args.optint(3, 0));
            return LuaValue.NIL;
        }));
        entityTable.set("set_fire_ticks", method(entityTable, args -> {
            if (livingEntity != null && livingEntity.isValid()) {
                livingEntity.setFireTicks(args.checkint(1));
            }
            return LuaValue.NIL;
        }));
        entityTable.set("add_visual_freeze_ticks", method(entityTable, args -> {
            if (livingEntity != null && livingEntity.isValid()) {
                livingEntity.setFreezeTicks(livingEntity.getFreezeTicks() + args.optint(1, 1));
            }
            return LuaValue.NIL;
        }));
        entityTable.set("place_temporary_block", method(entityTable, args -> {
            support.placeTemporaryBlock(livingEntity == null ? null : livingEntity.getLocation(),
                    args.checkjstring(1),
                    args.optint(2, 0),
                    args.optboolean(3, false));
            return LuaValue.NIL;
        }));
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

    private void applyGenericSpawnOptions(Entity entity, LuaTable options) {
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
                entity.playEffect(EntityEffect.valueOf(effectName.toUpperCase(java.util.Locale.ROOT)));
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
                            createEntityReferenceTable(entity));
                    return;
                }
                counter++;
            }
        });
    }
}
