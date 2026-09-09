package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.instanced.arena.ArenaContainer;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.lua.ScriptableBoss;
import com.magmaguy.magmacore.scripting.ScriptInstance;
import com.magmaguy.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.shaded.luaj.vm2.*;
import com.magmaguy.shaded.luaj.vm2.lib.VarArgFunction;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Function;

/** Supplies a known challenger and run-owned physical actors to the normal boss Lua runtime. */
final class TrialScriptActor extends ScriptableBoss implements Listener {
    private record Survival(String actor, String key, long expires, int protectionTicks) {}
    private final CustomBossEntity boss;
    private final Player player;
    private final ArenaContainer bounds;
    private final TrialMovement movement;
    private final TrialProjectiles projectiles;
    private final Map<String, CustomBossEntity> actors = new LinkedHashMap<>();
    private final Map<String, Survival> survival = new HashMap<>();
    private final Set<String> spentSurvival = new HashSet<>();
    private final Map<String, String> survivalNotifications = new HashMap<>();
    private final Map<UUID, Long> protectedActors = new HashMap<>();
    private final LuaElitePower power;
    private final double matchedHit;
    private Horse steed;
    private TrialEffects effects;
    private LuaTable trial;
    private long ticks;
    private long playerGraceUntil;
    private boolean closed;
    private boolean transferring;

    TrialScriptActor(CustomBossEntity boss, Player player, ArenaContainer bounds, LuaElitePower power) {
        super(boss);
        this.boss = boss;
        this.player = player;
        this.bounds = bounds;
        this.power = power;
        movement = new TrialMovement(bounds, player);
        projectiles = new TrialProjectiles(boss, player, bounds,
                () -> !closed && ticks >= playerGraceUntil);
        matchedHit = LevelScaling.calculateBaseDamageToElite(boss.getLevel());
        Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
    }

    @Override public LuaValue resolveExtraContext(String key, ScriptInstance instance) {
        if (key.equals("trial")) {
            if (trial == null) trial = buildTrialTable(instance);
            return trial;
        }
        return super.resolveExtraContext(key, instance);
    }

    private LuaTable buildTrialTable(ScriptInstance instance) {
        LuaTable table = new LuaTable();
        table.set("player", participantTable(instance, player));
        table.set("matched_hit", LuaValue.valueOf(matchedHit));
        table.set("active", new VarArgFunction() {
            @Override public Varargs invoke(Varargs args) { return LuaValue.valueOf(!closed && boss.exists() && player.isOnline()); }
        });
        table.set("position", method(table, args -> LuaTableSupport.locationToTable(body().getLocation())));
        table.set("tick", method(table, args -> { maintain(); return LuaValue.valueOf(ticks); }));
        table.set("say", method(table, args -> {
            player.sendMessage(ChatColorConverter.convert("&6" + boss.getName() + " &8» &f" + args.checkjstring(1)));
            return LuaValue.NIL;
        }));
        table.set("inside", method(table, args -> LuaValue.valueOf(bounds.contains(location(args.arg1())))));
        table.set("line_of_sight", method(table, args -> LuaValue.valueOf(boss.getLivingEntity().hasLineOfSight(player))));
        table.set("actor_los", method(table, args -> {
            LivingEntity from = effectTarget(args.checkjstring(1)), to = effectTarget(args.checkjstring(2));
            return LuaValue.valueOf(from != null && to != null && from.getWorld() == to.getWorld() && from.hasLineOfSight(to));
        }));
        table.set("push", method(table, args -> {
            Location source = location(args.arg1());
            double strength = bounded(args.checkdouble(2), -.4, .4);
            Vector direction = player.getLocation().toVector().subtract(source.toVector()).setY(0);
            if (direction.lengthSquared() < .001) return LuaValue.FALSE;
            Vector impulse = direction.normalize().multiply(strength);
            // Check the full displacement envelope, including subsequent momentum.
            if (!movement.straight(player, player.getLocation().add(impulse.clone().multiply(5)), true, false)) return LuaValue.FALSE;
            player.setVelocity(impulse.setY(.08));
            return LuaValue.TRUE;
        }));
        table.set("ground", method(table, args -> {
            Location point = location(args.arg1());
            if (!bounds.contains(point)) return LuaValue.NIL;
            var hit = point.getWorld().rayTraceBlocks(point.clone().add(0, 1, 0), new Vector(0,-1,0), 9,
                    FluidCollisionMode.NEVER, true);
            if (hit == null) return LuaValue.NIL;
            Location ground = hit.getHitPosition().toLocation(point.getWorld()).add(0,.01,0);
            return movement.standing(boss.getLivingEntity(), ground) ? LuaTableSupport.locationToTable(ground) : LuaValue.NIL;
        }));
        table.set("arm_survival", method(table, args -> {
            String actor = args.checkjstring(1), key = args.checkjstring(2);
            int duration = args.optint(3, 0), protect = args.optint(4, 40);
            if (duration < 0 || duration > 1200 || protect < 0 || protect > 120 || key.length() > 60)
                throw new IllegalArgumentException("Invalid finite survival ward");
            if (spentSurvival.size() + survival.size() >= 8 || spentSurvival.contains(key) || survival.containsKey(key)) return LuaValue.FALSE;
            survival.put(key, new Survival(actor, key, duration == 0 ? Long.MAX_VALUE : ticks + duration, protect));
            return LuaValue.TRUE;
        }));
        table.set("disarm_survival", method(table, args -> { survival.remove(args.checkjstring(1)); return LuaValue.NIL; }));
        table.set("consume_survival", method(table, args -> {
            String actor = survivalNotifications.remove(args.checkjstring(1));
            return actor == null ? LuaValue.NIL : LuaValue.valueOf(actor);
        }));
        table.set("effect", method(table, args -> {
            LivingEntity target = effectTarget(args.checkjstring(1));
            if (target != null) effects().apply(target,
                    Objects.requireNonNull(org.bukkit.potion.PotionEffectType.getByName(args.checkjstring(2))), args.checkint(3), args.optint(4, 0));
            return LuaValue.NIL;
        }));
        table.set("slow_player", method(table, args -> { effects().slow(player, args.checkdouble(1), args.checkint(2)); return LuaValue.NIL; }));
        table.set("clear_player_slow", method(table, args -> { if (effects != null) effects.clearMovement(player); return LuaValue.NIL; }));
        table.set("cleanse", method(table, args -> {
            LivingEntity target = effectTarget(args.checkjstring(1));
            if (target != null) effects().cleanse(target, args.optboolean(2, false), args.optint(3, 0));
            return LuaValue.NIL;
        }));
        table.set("damage", method(table, args -> {
            double amount = bounded(args.checkdouble(1), 0, 2);
            double before = player.getHealth() + player.getAbsorptionAmount();
            if (!closed && ticks >= playerGraceUntil && bounds.contains(player.getLocation()) && boss.getLivingEntity().hasLineOfSight(player))
                CombatDamageContext.runEliteToPlayerMultiplier(amount, () -> player.damage(1, boss.getLivingEntity()));
            return LuaValue.valueOf(player.getHealth() + player.getAbsorptionAmount() < before);
        }));
        table.set("player_grace", method(table, args -> {
            playerGraceUntil = Math.max(playerGraceUntil, ticks + (int) bounded(args.checkint(1), 0, 120));
            return LuaValue.NIL;
        }));
        table.set("launch_player", method(table, args -> {
            double lift = bounded(args.optdouble(1, .25), 0, .35);
            for (double y = .25; y <= 1.5; y += .25)
                if (!movement.clear(player, player.getLocation().add(0, y, 0))) return LuaValue.FALSE;
            player.setVelocity(new Vector(0, lift, 0));
            return LuaValue.TRUE;
        }));
        table.set("face", method(table, args -> {
            Location target = location(args.arg1());
            Location origin = body().getLocation();
            double yaw = Math.toDegrees(Math.atan2(-(target.getX() - origin.getX()), target.getZ() - origin.getZ()));
            double delta = (yaw - origin.getYaw() + 540) % 360 - 180;
            double limit = bounded(args.optdouble(2, 5), 0, 20);
            boss.getLivingEntity().setRotation((float) (origin.getYaw() + Math.max(-limit, Math.min(limit, delta))), 0);
            if (steed != null) steed.setRotation(boss.getLivingEntity().getLocation().getYaw(), 0);
            return LuaValue.NIL;
        }));
        table.set("safe_destination", method(table, args -> {
            Location target = location(args.arg1());
            return movement.straight(body(), target, args.optboolean(2, true), args.optboolean(3, true))
                    ? LuaTableSupport.locationToTable(target) : LuaValue.NIL;
        }));
        table.set("blink", method(table, args -> {
            Location target = location(args.arg1());
            if (!movement.straight(body(), target, true, true)) return LuaValue.FALSE;
            return LuaValue.valueOf(body().teleport(target));
        }));
        table.set("step", method(table, args -> {
            Location target = location(args.arg1());
            boolean grounded = args.optboolean(3, true);
            if (!movement.straight(body(), target, grounded, args.optboolean(4, true))) return LuaValue.FALSE;
            Vector direction = target.toVector().subtract(body().getLocation().toVector());
            double speed = Math.min(bounded(args.optdouble(2, .25), 0, .8), direction.length());
            if (direction.lengthSquared() > .001) body().setVelocity(direction.normalize().multiply(speed));
            return LuaValue.TRUE;
        }));
        table.set("leap", method(table, args -> LuaValue.valueOf(movement.leap(body(), location(args.arg1()), args.optint(2, 22)))));
        table.set("stop", method(table, args -> { if (body().isValid()) body().setVelocity(new Vector()); return LuaValue.NIL; }));
        table.set("mount", method(table, args -> { mount(); return LuaValue.valueOf(steed != null); }));
        table.set("dismount", method(table, args -> { dismount(); return LuaValue.NIL; }));
        table.set("own_projectile", method(table, args -> {
            Entity entity = reference(args.arg1());
            if (!(entity instanceof Projectile projectile)) throw new IllegalArgumentException("Expected a physical projectile");
            String group = args.checkjstring(3);
            double damage = bounded(args.checkdouble(2), 0, 2);
            double cap = bounded(args.optdouble(4, damage), 0, 3);
            projectiles.own(projectile, damage, group, cap, args.optint(5, 100));
            return LuaValue.NIL;
        }));
        table.set("group_damage", method(table, args -> LuaValue.valueOf(projectiles.spent(args.checkjstring(1)))));
        table.set("projectile_count", method(table, args -> LuaValue.valueOf(projectiles.count(args.checkjstring(1)))));
        table.set("forget_group", method(table, args -> { projectiles.forget(args.checkjstring(1)); return LuaValue.NIL; }));
        table.set("clear_projectiles", method(table, args -> { projectiles.clear(args.checkjstring(1)); return LuaValue.NIL; }));
        table.set("projectile_result", method(table, args -> {
            TrialProjectiles.Impact impact = projectiles.consume(args.checkjstring(1));
            if (impact == null) return LuaValue.NIL;
            LuaTable result = new LuaTable();
            result.set("location", LuaTableSupport.locationToTable(impact.location()));
            result.set("hit_player", LuaValue.valueOf(impact.hitPlayer()));
            result.set("damage", LuaValue.valueOf(impact.damage()));
            return result;
        }));
        table.set("arrow", method(table, args -> {
            LuaTable options = args.arg(7).opttable(new LuaTable());
            Projectile arrow = projectiles.arrow(location(args.arg1()), location(args.arg(2)), args.checkdouble(3),
                    args.checkdouble(4), args.checkjstring(5), args.checkdouble(6),
                    options.get("lifetime").optint(48), options.get("gravity").optboolean(false));
            return LuaValue.valueOf(arrow != null);
        }));
        table.set("spawn_actor", method(table, args -> spawnActor(instance, args)));
        table.set("actor", method(table, args -> {
            var actor = actors.get(args.checkjstring(1));
            return actor == null || !actor.exists() ? LuaValue.NIL : new ScriptableBoss(actor).buildContextTable(instance);
        }));
        table.set("actor_step", method(table, args -> {
            var actor = actors.get(args.checkjstring(1));
            if (actor == null || !actor.exists()) return LuaValue.FALSE;
            Location target = location(args.arg(2));
            if (!movement.straight(actor.getLivingEntity(), target, true, args.optboolean(4, true))) return LuaValue.FALSE;
            Vector direction = target.toVector().subtract(actor.getLocation().toVector());
            double speed = Math.min(bounded(args.optdouble(3, .15), 0, .8), direction.length());
            if (direction.lengthSquared() > .01) actor.getLivingEntity().setVelocity(direction.normalize().multiply(speed));
            return LuaValue.TRUE;
        }));
        table.set("actor_leap", method(table, args -> {
            var actor = actors.get(args.checkjstring(1));
            return LuaValue.valueOf(actor != null && actor.exists()
                    && movement.leap(actor.getLivingEntity(), location(args.arg(2)), args.optint(3, 20)));
        }));
        table.set("remove_actor", method(table, args -> {
            CustomBossEntity actor = actors.remove(args.checkjstring(1));
            if (actor != null && actor.exists()) actor.remove(RemovalReason.EFFECT_TIMEOUT);
            return LuaValue.NIL;
        }));
        table.set("actor_damage", method(table, args -> {
            var actor = actors.get(args.checkjstring(1));
            double amount = bounded(args.optdouble(2, .2), 0, 1);
            if (ticks < playerGraceUntil || actor == null || !actor.exists() || actor.getLocation().distanceSquared(player.getLocation()) > 3 * 3
                    || !actor.getLivingEntity().hasLineOfSight(player)) return LuaValue.FALSE;
            double before = player.getHealth() + player.getAbsorptionAmount();
            actor.getLivingEntity().swingMainHand();
            CombatDamageContext.runEliteToPlayerMultiplier(amount, () -> player.damage(1, actor.getLivingEntity()));
            return LuaValue.valueOf(player.getHealth() + player.getAbsorptionAmount() < before);
        }));
        table.set("actor_arrow", method(table, args -> {
            var actor = actors.get(args.checkjstring(1));
            if (actor == null || !actor.exists()) return LuaValue.FALSE;
            Location origin = actor.getLivingEntity().getEyeLocation();
            Vector direction = location(args.arg(2)).toVector().subtract(origin.toVector());
            if (direction.lengthSquared() < .001) return LuaValue.FALSE;
            double damage = bounded(args.optdouble(3, .2), 0, 1);
            Arrow arrow = origin.getWorld().spawnArrow(origin, direction.normalize(), .7f, 0);
            arrow.setShooter(actor.getLivingEntity());
            arrow.setGravity(false);
            arrow.setPersistent(false);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            projectiles.own(arrow, damage, "helper_" + ticks + "_" + args.checkjstring(1), damage, 100);
            return LuaValue.TRUE;
        }));
        table.set("damaged_actor", method(table, args -> {
            if (!(instance.getCurrentEvent() instanceof EliteMobDamagedByPlayerEvent event)) return LuaValue.NIL;
            if (event.getEliteMobEntity() == boss) return LuaValue.valueOf("boss");
            return actors.entrySet().stream().filter(entry -> entry.getValue() == event.getEliteMobEntity())
                    .findFirst().<LuaValue>map(entry -> LuaValue.valueOf(entry.getKey())).orElse(LuaValue.NIL);
        }));
        table.set("transfer_damage", method(table, args -> {
            // An existing normalized damage amount is transferred directly, never normalized a second time.
            double damage = bounded(args.checkdouble(1), 0, boss.getMaxHealth());
            if (damage > 0 && boss.exists() && !transferring) transferDamage(boss.getLivingEntity(), damage);
            return LuaValue.NIL;
        }));
        table.set("transfer_to", method(table, args -> {
            LivingEntity target = effectTarget(args.checkjstring(1));
            if (target == player) throw new IllegalArgumentException("Redistribution only targets owned trial actors");
            double damage = bounded(args.checkdouble(2), 0, boss.getMaxHealth());
            if (target != null && damage > 0 && !transferring) transferDamage(target, damage);
            return LuaValue.NIL;
        }));
        table.set("is_transfer", method(table, args -> LuaValue.valueOf(transferring)));
        return table;
    }

    private void transferDamage(LivingEntity target, double damage) {
        int previousTicks = target.getNoDamageTicks();
        double previousDamage = target.getLastDamage();
        transferring = true;
        try {
            target.setNoDamageTicks(0);
            CombatDamageContext.runPlayerToEliteBypass(CombatDamageContext.currentPlayerToEliteSource().orElse(null),
                    () -> target.damage(damage, player));
        } finally {
            transferring = false;
            if (target.isValid() && !target.isDead()) {
                target.setNoDamageTicks(previousTicks);
                target.setLastDamage(previousDamage);
            }
        }
    }

    private LuaValue spawnActor(ScriptInstance instance, Varargs args) {
        String id = args.checkjstring(1);
        if (actors.containsKey(id) || actors.size() >= 8) return LuaValue.NIL;
        Location spawn = location(args.arg(2));
        if (!movement.standing(boss.getLivingEntity(), spawn)) return LuaValue.NIL;
        double healthHits = bounded(args.optdouble(3, 3), .5, 8);
        String template = boss.getCustomBossesConfigFields().getFileConfiguration()
                .getString("classTrial.actors." + args.optjstring(5, "HUSK"));
        var fields = CustomBossesConfig.getCustomBoss(template);
        if (fields == null) throw new IllegalArgumentException("Missing configured trial actor: " + template);
        boolean prop = fields.getFileConfiguration().getBoolean("trialActor.prop");
        CustomBossEntity actor = new CustomBossEntity(fields);
        // Hit budgets and names are authored by the Lua mechanic, applied to this spawn only.
        actor.setHealthMultiplier(fields.getHealthMultiplier() * healthHits / LevelScaling.TARGET_HITS_TO_KILL_MOB);
        actor.setSummoningEntity(boss);
        actor.spawn(spawn, boss.getLevel(), true);
        if (!actor.exists()) return LuaValue.NIL;
        actor.setName(ChatColorConverter.convert(args.optjstring(4, fields.getName())), true);
        if (!movement.standing(actor.getLivingEntity(), spawn)) { actor.remove(RemovalReason.EFFECT_TIMEOUT); return LuaValue.NIL; }
        actors.put(id, actor);
        actor.getLivingEntity().setAI(false);
        if (prop) {
            var resistance = actor.getLivingEntity().getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE);
            if (resistance != null) resistance.setBaseValue(1);
        }
        return new ScriptableBoss(actor).buildContextTable(instance);
    }

    private void maintain() {
        ticks++;
        if (closed || !boss.exists() || !player.isOnline()) return;
        projectiles.tick(ticks);
        survival.values().removeIf(ward -> ticks >= ward.expires);
        protectedActors.values().removeIf(expiry -> ticks >= expiry);
        if (effects != null) effects.tick(ticks);
        // Recheck actual physics, including external pushes, against the same read-only bounds.
        if (!bounds.contains(body().getLocation())) throw new IllegalStateException("Trial actor left its bounds");
        Vector velocity = body().getVelocity();
        if (velocity.lengthSquared() > .001 && !movement.clear(body(), body().getLocation().add(velocity)))
            body().setVelocity(new Vector(0, Math.min(0, velocity.getY()), 0));
        for (var actor : actors.values()) {
            if (!actor.exists()) continue;
            var entity = actor.getLivingEntity();
            if (!bounds.contains(entity.getLocation())) { actor.remove(RemovalReason.EFFECT_TIMEOUT); continue; }
            Vector travel = entity.getVelocity();
            if (travel.lengthSquared() > .001 && !movement.clear(entity, entity.getLocation().add(travel)))
                entity.setVelocity(new Vector(0, Math.min(0, travel.getY()), 0));
        }
    }

    private Entity body() { return steed == null ? boss.getLivingEntity() : steed; }

    private void mount() {
        if (steed != null) return;
        Horse candidate = boss.getLocation().getWorld().spawn(boss.getLocation(), Horse.class, horse -> {
            horse.setAdult(); horse.setTamed(true); horse.setAI(false); horse.setInvulnerable(true);
            horse.setPersistent(false); horse.setCollidable(false);
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            horse.getInventory().setArmor(new ItemStack(Material.GOLDEN_HORSE_ARMOR));
        });
        if (!movement.standing(candidate, candidate.getLocation()) || !candidate.addPassenger(boss.getLivingEntity())) {
            candidate.remove(); return;
        }
        steed = candidate;
    }

    private void dismount() {
        if (steed == null) return;
        if (boss.getLivingEntity() != null) boss.getLivingEntity().leaveVehicle();
        steed.remove();
        steed = null;
    }

    boolean owns(CustomBossEntity entity) { return entity == boss || actors.containsValue(entity); }

    /** Runs after EliteMobs has normalized damage, preserving the original damage event and attacker. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void finiteSurvival(EntityDamageEvent event) {
        if (closed || !(event.getEntity() instanceof LivingEntity target)) return;
        if (protectedActors.getOrDefault(target.getUniqueId(), 0L) > ticks) { event.setDamage(0); return; }
        if (event.getFinalDamage() < target.getHealth()) return;
        for (Survival ward : List.copyOf(survival.values())) {
            if (ticks >= ward.expires || effectTarget(ward.actor) != target) continue;
            survival.remove(ward.key); spentSurvival.add(ward.key);
            survivalNotifications.put(ward.key, ward.actor);
            protectedActors.put(target.getUniqueId(), ticks + ward.protectionTicks);
            // Normalized elite combat has already removed vanilla armor modifiers at this point.
            event.setDamage(Math.max(0, target.getHealth() - 1));
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void helperDamage(EliteMobDamagedByPlayerEvent event) {
        if (!closed && event.getEliteMobEntity() instanceof CustomBossEntity actor && actor != boss && owns(actor))
            power.check(event, boss, event.getPlayer());
    }

    @Override public void onShutdown() {
        if (closed) return;
        closed = true;
        HandlerList.unregisterAll(this);
        projectiles.close();
        survival.clear(); spentSurvival.clear(); survivalNotifications.clear(); protectedActors.clear();
        dismount();
        if (effects != null) effects.close();
        if (boss.exists()) { boss.getLivingEntity().setVelocity(new Vector()); }
        actors.values().forEach(actor -> { if (actor.exists()) actor.remove(RemovalReason.ARENA_RESET); });
        actors.clear();
    }

    private Location location(LuaValue value) {
        return LuaTableSupport.tableToLocation(value.checktable(), boss.getLocation().getWorld());
    }

    private LivingEntity effectTarget(String id) {
        if (id.equals("player")) return player;
        if (id.equals("boss")) return boss.exists() ? boss.getLivingEntity() : null;
        CustomBossEntity actor = actors.get(id);
        return actor == null || !actor.exists() ? null : actor.getLivingEntity();
    }

    private TrialEffects effects() {
        if (effects == null) { effects = new TrialEffects(); effects.tick(ticks); }
        return effects;
    }

    private Entity reference(LuaValue value) {
        String id = value.checktable().get("__bukkit_uuid").optjstring(value.get("uuid").optjstring(""));
        return Bukkit.getEntity(UUID.fromString(id));
    }

    private static double bounded(double value, double min, double max) {
        if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException("Trial value outside bounds");
        return value;
    }

    private VarArgFunction method(LuaTable owner, Function<Varargs, LuaValue> callback) {
        return new VarArgFunction() {
            @Override public Varargs invoke(Varargs args) {
                if (closed) return LuaValue.NIL;
                return callback.apply(args.narg() > 0 && args.arg1().raweq(owner) ? args.subargs(2) : args);
            }
        };
    }
}
