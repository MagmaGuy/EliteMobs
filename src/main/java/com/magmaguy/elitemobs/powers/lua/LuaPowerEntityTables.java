package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.EliteDamageEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.ScriptZoneEnterEvent;
import com.magmaguy.elitemobs.api.ScriptZoneLeaveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import com.magmaguy.shaded.luaj.vm2.Varargs;
import com.magmaguy.shaded.luaj.vm2.lib.VarArgFunction;

import java.util.Objects;

/**
 * Creates Lua tables for entities (boss, player, living entity, generic entity)
 * and events.
 * <p>
 * The boss table is built by {@link LuaBossTableBuilder} (delegated via
 * {@link #createBossTable()}).
 */
final class LuaPowerEntityTables {

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;
    private final LuaPowerScriptApi.OwnedTaskController taskController;
    private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;
    private final LuaBossTableBuilder bossTableBuilder;

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
        this.bossTableBuilder = new LuaBossTableBuilder(definition, eliteEntity, support, this, taskController, callbackInvoker);
    }

    // ── Boss table (delegated to LuaBossTableBuilder) ──────────────────

    LuaTable createBossTable() {
        return bossTableBuilder.build();
    }

    // ── Event table ────────────────────────────────────────────────────

    LuaTable createEventTable(Event event) {
        LuaTable eventTable = new LuaTable();
        if (event instanceof EliteDamageEvent eliteDamageEvent) {
            eventTable.set("damage_amount", LuaValue.valueOf(eliteDamageEvent.getDamage()));
            if (event instanceof EliteMobDamagedEvent eliteMobDamagedEvent) {
                eventTable.set("damage_cause", LuaValue.valueOf(eliteMobDamagedEvent.getEntityDamageEvent().getCause().name()));
            } else if (event instanceof EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent) {
                eventTable.set("damage_cause", LuaValue.valueOf(eliteMobDamagedByPlayerEvent.getEntityDamageByEntityEvent().getCause().name()));
            } else if (event instanceof EliteMobDamagedByEliteMobEvent eliteMobDamagedByEliteMobEvent) {
                eventTable.set("damage_cause", LuaValue.valueOf(eliteMobDamagedByEliteMobEvent.getEntityDamageByEntityEvent().getCause().name()));
            } else if (event instanceof PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent) {
                eventTable.set("damage_cause", LuaValue.valueOf(playerDamagedByEliteMobEvent.getEntityDamageByEntityEvent().getCause().name()));
            }
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
            EntityDamageByEntityEvent entityDamageByEntityEvent = null;
            if (event instanceof EliteMobDamagedEvent eliteMobDamagedEvent
                    && eliteMobDamagedEvent.getEntityDamageEvent() instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                entityDamageByEntityEvent = damageByEntityEvent;
            } else if (event instanceof EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent) {
                entityDamageByEntityEvent = eliteMobDamagedByPlayerEvent.getEntityDamageByEntityEvent();
            } else if (event instanceof EliteMobDamagedByEliteMobEvent eliteMobDamagedByEliteMobEvent) {
                entityDamageByEntityEvent = eliteMobDamagedByEliteMobEvent.getEntityDamageByEntityEvent();
            } else if (event instanceof PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent) {
                entityDamageByEntityEvent = playerDamagedByEliteMobEvent.getEntityDamageByEntityEvent();
            }
            if (entityDamageByEntityEvent != null) {
                Entity damager = entityDamageByEntityEvent.getDamager();
                eventTable.set("damager", damager == null ? LuaValue.NIL : createEntityReferenceTable(damager));
                if (damager instanceof org.bukkit.entity.Projectile projectile) {
                    eventTable.set("projectile", createEntityReferenceTable(projectile));
                }
            }
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
        if (event instanceof com.magmaguy.elitemobs.api.EliteMobDeathEvent deathEvent) {
            eventTable.set("entity", createEntityReferenceTable(deathEvent.getEntity()));
        }
        if (event instanceof ScriptZoneEnterEvent zoneEnterEvent) {
            eventTable.set("entity", createEntityTable(zoneEnterEvent.getEntity()));
        } else if (event instanceof ScriptZoneLeaveEvent zoneLeaveEvent) {
            eventTable.set("entity", createEntityTable(zoneLeaveEvent.getEntity()));
        }
        return eventTable;
    }

    // ── Entity tables ──────────────────────────────────────────────────

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
        table.set("get_velocity", method(table, args -> support.toVectorTable(entity.getVelocity())));
        table.set("is_on_ground", method(table, args -> LuaValue.valueOf(entity.isOnGround())));
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
        table.set("set_direction_vector", method(table, args -> {
            Vector direction = support.toVector(args.arg1());
            if (direction != null && entity instanceof org.bukkit.entity.Fireball fireball) {
                fireball.setDirection(direction);
            }
            return LuaValue.NIL;
        }));
        table.set("set_yield", method(table, args -> {
            if (entity instanceof org.bukkit.entity.Fireball fireball) {
                fireball.setYield((float) args.checkdouble(1));
            }
            return LuaValue.NIL;
        }));
        table.set("set_gravity", method(table, args -> {
            entity.setGravity(args.checkboolean(1));
            return LuaValue.NIL;
        }));
        table.set("detonate", method(table, args -> {
            if (entity instanceof org.bukkit.entity.Firework firework) {
                firework.detonate();
            }
            return LuaValue.NIL;
        }));
        table.set("remove", method(table, args -> {
            entity.remove();
            return LuaValue.NIL;
        }));
        table.set("unregister", method(table, args -> {
            EntityTracker.unregister(entity,
                    support.parseEnum(args.optjstring(1, "OTHER"), RemovalReason.class, RemovalReason.OTHER));
            return LuaValue.NIL;
        }));
        return table;
    }

    // ── Player table ───────────────────────────────────────────────────

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
        table.set("game_mode", LuaValue.valueOf(player.getGameMode().name()));
        return table;
    }

    // ── Living entity table ────────────────────────────────────────────

    LuaTable createLivingEntityTable(LivingEntity livingEntity) {
        LuaTable entity = new LuaTable();
        entity.set("name", LuaValue.valueOf(livingEntity.getName()));
        entity.set("uuid", LuaValue.valueOf(livingEntity.getUniqueId().toString()));
        entity.set("entity_type", LuaValue.valueOf(livingEntity.getType().name()));
        entity.set("is_player", LuaValue.valueOf(livingEntity instanceof Player));
        entity.set("is_monster", LuaValue.valueOf(livingEntity instanceof Monster));
        entity.set("is_elite", LuaValue.valueOf(EntityTracker.getEliteMobEntity(livingEntity) != null));
        entity.set("is_valid", LuaValue.valueOf(livingEntity.isValid()));
        entity.set("is_alive", method(entity, args -> LuaValue.valueOf(isAlive(livingEntity))));
        entity.set("is_ai_enabled", method(entity, args -> LuaValue.valueOf(livingEntity.hasAI())));
        entity.set("is_frozen", method(entity, args -> {
            EliteEntity elite = EntityTracker.getEliteMobEntity(livingEntity);
            if (elite instanceof CustomBossEntity customBoss) {
                return LuaValue.valueOf(customBoss.getCustomBossesConfigFields().isFrozen());
            }
            return LuaValue.FALSE;
        }));
        entity.set("health", LuaValue.valueOf(livingEntity.getHealth()));
        entity.set("maximum_health", LuaValue.valueOf(Objects.requireNonNull(livingEntity.getAttribute(Attribute.MAX_HEALTH)).getValue()));
        entity.set("current_location", support.toLocationTable(livingEntity.getLocation()));
        entity.set("get_location", method(entity, args -> support.toLocationTable(livingEntity.getLocation())));
        entity.set("get_eye_location", method(entity, args -> support.toLocationTable(livingEntity.getEyeLocation())));
        entity.set("get_height", method(entity, args -> LuaValue.valueOf(livingEntity.getHeight())));
        entity.set("get_health", method(entity, args -> LuaValue.valueOf(livingEntity.getHealth())));
        entity.set("get_maximum_health", method(entity, args -> {
            AttributeInstance maxHealth = livingEntity.getAttribute(Attribute.MAX_HEALTH);
            return LuaValue.valueOf(maxHealth == null ? 0 : maxHealth.getValue());
        }));
        entity.set("get_velocity", method(entity, args -> support.toVectorTable(livingEntity.getVelocity())));
        entity.set("deal_damage", method(entity, args -> {
            livingEntity.damage(args.checkdouble(1));
            return LuaValue.NIL;
        }));
        entity.set("deal_custom_damage", method(entity, args -> {
            if (eliteEntity.getLivingEntity() != null) {
                BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), livingEntity, args.checkdouble(1));
            }
            return LuaValue.NIL;
        }));
        entity.set("deal_damage_from_boss", method(entity, args -> {
            if (eliteEntity.getLivingEntity() != null) {
                livingEntity.damage(args.checkdouble(1), eliteEntity.getLivingEntity());
            }
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
        entity.set("play_sound_at_self", method(entity, args -> {
            support.playSound(livingEntity.getLocation(), args.checkjstring(1), resolveVolume(args), resolvePitch(args));
            return LuaValue.NIL;
        }));
        entity.set("spawn_particle_at_self", method(entity, args -> {
            support.spawnParticle(livingEntity.getLocation(), args.arg(1), args.optint(2, 1));
            return LuaValue.NIL;
        }));
        entity.set("spawn_particles_at_location", method(entity, args -> {
            support.spawnParticle(support.toLocation(args.arg1()), support.toParticleSpec(args), args.optint(3, 1));
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
        entity.set("set_gravity", method(entity, args -> {
            livingEntity.setGravity(args.checkboolean(1));
            return LuaValue.NIL;
        }));
        entity.set("is_on_ground", method(entity, args -> LuaValue.valueOf(livingEntity.isOnGround())));
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
        entity.set("set_custom_name", method(entity, args -> {
            livingEntity.setCustomName(ChatColorConverter.convert(args.checkjstring(1)));
            return LuaValue.NIL;
        }));
        entity.set("reset_custom_name", method(entity, args -> {
            EliteEntity trackedElite = EntityTracker.getEliteMobEntity(livingEntity);
            livingEntity.setCustomName(trackedElite != null ? trackedElite.getName() : livingEntity.getName());
            return LuaValue.NIL;
        }));
        entity.set("set_custom_name_visible", method(entity, args -> {
            livingEntity.setCustomNameVisible(args.checkboolean(1));
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
        entity.set("is_healing", method(entity, args -> {
            EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
            return LuaValue.valueOf(entityRef != null && entityRef.isHealing());
        }));
        entity.set("set_healing", method(entity, args -> {
            EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
            if (entityRef != null) {
                entityRef.setHealing(args.checkboolean(1));
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
            Location source = support.toLocation(args.arg1());
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
        entity.set("overlaps_box_at_location", method(entity, args -> {
            Location center = support.toLocation(args.arg1());
            if (center == null) {
                return LuaValue.FALSE;
            }
            double halfX = args.optdouble(2, 0.5);
            double halfY = args.optdouble(3, halfX);
            double halfZ = args.optdouble(4, halfX);
            return LuaValue.valueOf(livingEntity.getBoundingBox().overlaps(
                    new Vector(center.getX() - halfX, center.getY() - halfY, center.getZ() - halfZ),
                    new Vector(center.getX() + halfX, center.getY() + halfY, center.getZ() + halfZ)));
        }));
        addEntityEffectMethods(entity, livingEntity);
        return entity;
    }

    // ── Shared helpers (package-private for LuaBossTableBuilder) ──────

    private boolean isAlive(LivingEntity livingEntity) {
        return livingEntity != null && livingEntity.isValid() && !livingEntity.isDead();
    }

    void applyAiState(LivingEntity livingEntity, boolean targetValue, int duration) {
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

    void scheduleTagRemoval(LivingEntity livingEntity, int duration, String tag) {
        if (livingEntity == null || duration <= 0) {
            return;
        }
        GameClock.scheduleLater(duration, () -> {
            if (livingEntity.isValid()) {
                support.applyTag(livingEntity, tag, false);
            }
        });
    }

    void addEntityEffectMethods(LuaTable entityTable, LivingEntity livingEntity) {
        entityTable.set("apply_potion_effect", method(entityTable, args -> {
            support.applyPotionEffect(livingEntity, args.checkjstring(1), args.checkint(2), args.optint(3, 0));
            return LuaValue.NIL;
        }));
        entityTable.set("set_equipment", method(entityTable, args -> {
            if (livingEntity == null || livingEntity.getEquipment() == null) {
                return LuaValue.NIL;
            }
            EquipmentSlot slot = support.parseEnum(args.checkjstring(1), EquipmentSlot.class, null);
            Material material = support.parseMaterial(args.checkjstring(2));
            if (slot == null || material == null) {
                return LuaValue.NIL;
            }
            ItemStack itemStack = new ItemStack(material);
            LuaTable options = args.narg() >= 3 && args.arg(3).istable() ? args.arg(3).checktable() : new LuaTable();
            if (options.get("enchantments").istable()) {
                LuaTable enchantments = options.get("enchantments").checktable();
                LuaValue key = LuaValue.NIL;
                while (true) {
                    Varargs next = enchantments.next(key);
                    key = next.arg1();
                    if (key.isnil()) {
                        break;
                    }
                    if (!next.arg(2).istable()) {
                        continue;
                    }
                    LuaTable enchantmentSpec = next.arg(2).checktable();
                    Enchantment enchantment = Enchantment.getByName(enchantmentSpec.get("type").optjstring("").toUpperCase(java.util.Locale.ROOT));
                    if (enchantment != null) {
                        itemStack.addUnsafeEnchantment(enchantment, enchantmentSpec.get("level").optint(1));
                    }
                }
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setUnbreakable(options.get("unbreakable").optboolean(false));
                itemStack.setItemMeta(itemMeta);
            }
            switch (slot) {
                case HEAD -> livingEntity.getEquipment().setHelmet(itemStack);
                case CHEST -> livingEntity.getEquipment().setChestplate(itemStack);
                case LEGS -> livingEntity.getEquipment().setLeggings(itemStack);
                case FEET -> livingEntity.getEquipment().setBoots(itemStack);
                case HAND -> livingEntity.getEquipment().setItemInMainHand(itemStack);
                case OFF_HAND -> livingEntity.getEquipment().setItemInOffHand(itemStack);
                default -> {
                }
            }
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
