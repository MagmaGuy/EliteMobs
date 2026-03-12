package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.EliteDamageEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.utils.GameClock;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Objects;

final class LuaPowerEntityTables {

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;
    private final LuaPowerSupport support;

    LuaPowerEntityTables(LuaPowerDefinition definition, EliteEntity eliteEntity, LuaPowerSupport support) {
        this.definition = definition;
        this.eliteEntity = eliteEntity;
        this.support = support;
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
        boss.set("add_tag", method(boss, args -> {
            eliteEntity.addTag(args.checkjstring(1));
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
            if (eliteEntity.getLivingEntity() != null) {
                eliteEntity.getLivingEntity().setAI(args.checkboolean(1));
            }
            return LuaValue.NIL;
        }));
        boss.set("play_sound_at_self", method(boss, args -> {
            support.playSound(eliteEntity.getLocation(), args.checkjstring(1), support.getFloat(args, 2, 1f), support.getFloat(args, 3, 1f));
            return LuaValue.NIL;
        }));
        boss.set("spawn_particle_at_self", method(boss, args -> {
            support.spawnParticle(eliteEntity.getLocation(), args.arg(1), args.optint(2, 1));
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
        boss.set("apply_push_vector", method(boss, args -> {
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
        return eventTable;
    }

    LuaTable createEntityTable(LivingEntity livingEntity) {
        return livingEntity instanceof Player player ? createPlayerTable(player) : createLivingEntityTable(livingEntity);
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
        table.set("show_temporary_boss_bar", showBossBar);
        VarArgFunction runCommand = method(table, args -> {
            player.performCommand(args.checkjstring(1));
            return LuaValue.NIL;
        });
        table.set("run_command", runCommand);
        table.set("run_command_as_player", runCommand);
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
        entity.set("health", LuaValue.valueOf(livingEntity.getHealth()));
        entity.set("maximum_health", LuaValue.valueOf(Objects.requireNonNull(livingEntity.getAttribute(Attribute.MAX_HEALTH)).getValue()));
        entity.set("current_location", support.toLocationTable(livingEntity.getLocation()));
        entity.set("deal_damage", method(entity, args -> {
            livingEntity.damage(args.checkdouble(1));
            return LuaValue.NIL;
        }));
        entity.set("restore_health", method(entity, args -> {
            double maxHealth = Objects.requireNonNull(livingEntity.getAttribute(Attribute.MAX_HEALTH)).getValue();
            livingEntity.setHealth(Math.min(maxHealth, livingEntity.getHealth() + args.checkdouble(1)));
            return LuaValue.NIL;
        }));
        entity.set("play_sound_at_entity", method(entity, args -> {
            support.playSound(livingEntity.getLocation(), args.checkjstring(1), support.getFloat(args, 2, 1f), support.getFloat(args, 3, 1f));
            return LuaValue.NIL;
        }));
        VarArgFunction teleportToLocation = method(entity, args -> {
            Location destination = support.toLocation(args.arg1());
            if (destination != null) {
                livingEntity.teleport(destination);
            }
            return LuaValue.NIL;
        });
        entity.set("teleport_to", teleportToLocation);
        entity.set("teleport_to_location", teleportToLocation);
        VarArgFunction setVelocity = method(entity, args -> {
            Vector velocity = support.toVector(args.arg1());
            if (velocity != null) {
                livingEntity.setVelocity(velocity);
            }
            return LuaValue.NIL;
        });
        entity.set("set_velocity", setVelocity);
        entity.set("set_velocity_vector", setVelocity);
        VarArgFunction push = method(entity, args -> {
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
        });
        entity.set("push", push);
        entity.set("apply_push_vector", push);
        entity.set("set_ai_enabled", method(entity, args -> {
            livingEntity.setAI(args.checkboolean(1));
            int duration = args.optint(2, 0);
            if (duration > 0) {
                boolean targetValue = args.checkboolean(1);
                GameClock.scheduleLater(duration, () -> {
                    if (livingEntity.isValid()) {
                        livingEntity.setAI(!targetValue);
                    }
                });
            }
            return LuaValue.NIL;
        }));
        VarArgFunction setAware = method(entity, args -> {
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
        });
        entity.set("set_aware", setAware);
        entity.set("set_awareness_enabled", setAware);
        VarArgFunction setFacing = method(entity, args -> {
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
        });
        entity.set("set_facing", setFacing);
        entity.set("face_direction_or_location", setFacing);
        VarArgFunction playAnimation = method(entity, args -> {
            EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
            if (entityRef instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomModel() != null) {
                customBossEntity.getCustomModel().playAnimationByName(args.checkjstring(1));
            }
            return LuaValue.NIL;
        });
        entity.set("play_animation", playAnimation);
        entity.set("play_model_animation", playAnimation);
        VarArgFunction setScale = method(entity, args -> {
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
        });
        entity.set("set_scale", setScale);
        entity.set("set_entity_scale", setScale);
        VarArgFunction setInvulnerable = method(entity, args -> {
            support.applyInvulnerable(livingEntity, args.checkboolean(1), args.optint(2, 0));
            return LuaValue.NIL;
        });
        entity.set("set_invulnerable", setInvulnerable);
        entity.set("set_invulnerable_state", setInvulnerable);
        VarArgFunction removeElite = method(entity, args -> {
            EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
            if (entityRef != null) {
                entityRef.remove(RemovalReason.OTHER);
            }
            return LuaValue.NIL;
        });
        entity.set("remove_elite", removeElite);
        entity.set("remove_elite_entity", removeElite);
        VarArgFunction navigateTo = method(entity, args -> {
            EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
            if (entityRef instanceof CustomBossEntity customBossEntity) {
                Location destination = support.toLocation(args.arg1());
                if (destination != null) {
                    Navigation.navigateTo(customBossEntity, args.optdouble(2, 1.0), destination, args.optboolean(3, false), args.optint(4, 0));
                }
            }
            return LuaValue.NIL;
        });
        entity.set("navigate_to", navigateTo);
        entity.set("navigate_to_location", navigateTo);
        entity.set("add_tag", method(entity, args -> {
            support.applyTag(livingEntity, args.checkjstring(1), true);
            return LuaValue.NIL;
        }));
        entity.set("remove_tag", method(entity, args -> {
            support.applyTag(livingEntity, args.checkjstring(1), false);
            return LuaValue.NIL;
        }));
        entity.set("has_tag", method(entity, args -> LuaValue.valueOf(support.hasTag(livingEntity, args.checkjstring(1)))));
        addEntityEffectMethods(entity, livingEntity);
        return entity;
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
}
