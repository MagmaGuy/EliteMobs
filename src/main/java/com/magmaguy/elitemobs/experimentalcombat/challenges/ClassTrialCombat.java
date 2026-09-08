package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.instanced.arena.ArenaContainer;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/** Opponent adaptation of class mechanics, with readable tells and run-owned effects. */
final class ClassTrialCombat implements AutoCloseable {
    private static final Particle.DustOptions WARNING = new Particle.DustOptions(Color.RED, 1.2F);
    private final ClassTrialDefinition definition;
    private final CustomBossEntity boss;
    private final Player player;
    private final ArenaContainer arena;
    private final List<Cast> pending = new ArrayList<>();
    private final List<Helper> helpers = new ArrayList<>();
    private int tick;
    private int abilityIndex;
    private int nextCast = 60;
    private int guardUntil;
    private int strengthUntil;
    private int vulnerableUntil;
    private org.bukkit.entity.Horse steed;
    private int steedUntil;
    private boolean closed;

    ClassTrialCombat(ClassTrialDefinition definition, CustomBossEntity boss, Player player, ArenaContainer arena) {
        this.definition = definition;
        this.boss = boss;
        this.player = player;
        this.arena = arena;
    }

    /** Called every five server ticks; no independent delayed tasks can outlive this run. */
    void tick() {
        tick += 5;
        if (closed || !boss.exists() || !player.isOnline()) return;
        if (steed != null && tick >= steedUntil) { steed.remove(); steed = null; }
        helpers.removeIf(helper -> {
            if (tick < helper.expires && helper.entity.exists()) return false;
            helper.entity.remove(RemovalReason.ARENA_RESET);
            return true;
        });
        if (tick >= nextCast && pending.isEmpty() && !boss.getPowerSuppression().isSuppressed()) {
            var ability = definition.abilities().get(abilityIndex++ % definition.abilities().size());
            var spec = BuiltInClassContent.abilityRegistry().require(ability.id());
            player.sendMessage(ChatColorConverter.convert("&6" + definition.form().displayName()
                    + " Instructor &8» &e" + ability.displayName()));
            Location target = player.getLocation();
            boolean selfArea = switch (spec.target()) {
                case SELF, NEARBY_ENEMIES, NEARBY_ALLIES, MIXED_NEARBY -> true;
                default -> false;
            };
            if (selfArea) target = boss.getLocation();
            pending.add(new Cast(spec, target.clone(), tick + 25));
            // Cost influences opponent pacing while leaving time to react between demonstrations.
            nextCast = tick + Math.max(90, (int) Math.round(spec.resourceCost() * 3));
        }
        for (Cast cast : List.copyOf(pending)) {
            if (closed) return;
            if (tick < cast.resolveAt) {
                telegraph(cast);
            } else {
                pending.remove(cast);
                execute(cast);
            }
        }
    }

    boolean owns(CustomBossEntity entity) {
        return entity == boss || helpers.stream().anyMatch(helper -> helper.entity == entity);
    }

    double incomingMultiplier() { return (tick < guardUntil ? .55 : 1) * (tick < vulnerableUntil ? 1.25 : 1); }
    double outgoingMultiplier() { return tick < strengthUntil ? 1.25 : 1; }

    private double radius(FixedAbilitySpec spec) {
        if (spec.family() == AbilityFamily.PROJECTILE) return Math.max(1.4, Math.min(3, spec.tuning().radius()));
        return Math.max(2, Math.min(6, spec.tuning().radius()));
    }

    private void telegraph(Cast cast) {
        double radius = radius(cast.spec);
        for (int point = 0; point < 24; point++) {
            double angle = Math.PI * 2 * point / 24;
            Location marker = cast.target.clone().add(Math.cos(angle) * radius, .15, Math.sin(angle) * radius);
            player.spawnParticle(Particle.DUST, marker, 1, 0, 0, 0, 0, WARNING);
        }
    }

    private void execute(Cast cast) {
        if (closed || !boss.exists() || boss.getPowerSuppression().isSuppressed()) return;
        var spec = cast.spec;
        var effects = spec.effects();
        int duration = Math.max(20, Math.min(100, spec.tuning().durationTicks()));
        switch (spec.family()) {
            case MOUNTED_CHARGE, BALLISTIC_LEAP, SAFE_DASH, SAFE_BLINK, ALLY_FLIGHT -> move(spec, cast.target);
            case SUMMON -> summon();
            default -> { }
        }
        if (effects.contains(AbilityEffect.HEAL) || effects.contains(AbilityEffect.LIFESTEAL))
            boss.heal(boss.getMaxHealth() * Math.min(.06, Math.max(.02, spec.tuning().healingFraction())));
        if (effects.contains(AbilityEffect.SHIELD) || effects.contains(AbilityEffect.SELF_PROTECT)
                || effects.contains(AbilityEffect.ALLY_PROTECT)) guardUntil = tick + duration;
        if (effects.contains(AbilityEffect.STRENGTH) || effects.contains(AbilityEffect.SPELL_STRENGTH))
            strengthUntil = tick + duration;
        if (effects.contains(AbilityEffect.SELF_VULNERABLE)) vulnerableUntil = tick + duration;
        if (effects.contains(AbilityEffect.CLEANSE))
            boss.getLivingEntity().getActivePotionEffects().forEach(effect -> boss.getLivingEntity().removePotionEffect(effect.getType()));
        if (effects.contains(AbilityEffect.SPEED))
            boss.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1));
        boolean inRange = player.getLocation().distanceSquared(cast.target) <= Math.pow(radius(spec), 2)
                && arena.contains(player.getLocation()) && boss.getLivingEntity().hasLineOfSight(player);
        if (!inRange) return;
        if (effects.contains(AbilityEffect.DAMAGE)) {
            double multiplier = Math.max(.4, Math.min(1.6, spec.tuning().damageMultiplier()));
            CombatDamageContext.runEliteToPlayerMultiplier(multiplier,
                    () -> player.damage(1, boss.getLivingEntity()));
        }
        // Damage callbacks may finish the match and remove the instructor.
        if (closed || !boss.exists()) return;
        if (effects.contains(AbilityEffect.BURN)) player.setFireTicks(Math.max(player.getFireTicks(), duration));
        if (effects.contains(AbilityEffect.SLOW) || effects.contains(AbilityEffect.ROOT))
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration,
                    effects.contains(AbilityEffect.ROOT) ? 3 : 1));
        if (effects.contains(AbilityEffect.WEAKEN)) player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 0));
        if (effects.contains(AbilityEffect.GLOW) || effects.contains(AbilityEffect.PARTY_DAMAGE_MARK))
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0));
        if (effects.contains(AbilityEffect.KNOCKBACK) || effects.contains(AbilityEffect.PULL)
                || effects.contains(AbilityEffect.LAUNCH) || effects.contains(AbilityEffect.FEAR)) {
            Vector direction = player.getLocation().toVector().subtract(boss.getLocation().toVector());
            if (direction.lengthSquared() > .01) direction.normalize();
            if (effects.contains(AbilityEffect.PULL)) direction.multiply(-1);
            direction.multiply(Math.min(1, Math.max(.3, spec.tuning().displacement())));
            direction.setY(effects.contains(AbilityEffect.LAUNCH) ? .55 : .2);
            player.setVelocity(direction);
        }
    }

    private void move(FixedAbilitySpec spec, Location target) {
        Location origin = boss.getLocation();
        Vector direction = target.toVector().subtract(origin.toVector());
        double distance = direction.length();
        if (distance < .1) return;
        direction.normalize();
        double reach = Math.min(distance, Math.min(12, Math.max(4, spec.tuning().range())));
        var wall = origin.getWorld().rayTraceBlocks(origin.clone().add(0, 1, 0), direction, reach,
                org.bukkit.FluidCollisionMode.NEVER, true);
        if (wall != null) reach = Math.max(0, wall.getHitPosition().distance(origin.clone().add(0, 1, 0).toVector()) - 1);
        Location destination = origin.clone().add(direction.multiply(reach));
        if (!arena.contains(destination) || !destination.getBlock().isPassable()
                || !destination.clone().add(0, 1, 0).getBlock().isPassable()
                || destination.clone().subtract(0, 1, 0).getBlock().isPassable()) return;
        if (spec.family() == AbilityFamily.SAFE_BLINK) {
            origin.getWorld().spawnParticle(Particle.PORTAL, origin.clone().add(0, 1, 0), 30, .4, .6, .4);
            boss.getLivingEntity().teleport(destination);
        } else {
            if (spec.family() == AbilityFamily.MOUNTED_CHARGE && steed == null) {
                steed = origin.getWorld().spawn(origin, org.bukkit.entity.Horse.class);
                steed.setAdult();
                steed.setTamed(true);
                steed.setInvulnerable(true);
                steed.setPersistent(false);
                steed.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SADDLE));
                steed.addPassenger(boss.getLivingEntity());
                steedUntil = tick + 100;
            }
            Vector velocity = destination.toVector().subtract(origin.toVector()).normalize().multiply(.85);
            velocity.setY(spec.family() == AbilityFamily.BALLISTIC_LEAP || spec.family() == AbilityFamily.ALLY_FLIGHT ? .65 : .15);
            if (steed != null && spec.family() == AbilityFamily.MOUNTED_CHARGE) steed.setVelocity(velocity);
            else boss.getLivingEntity().setVelocity(velocity);
        }
    }

    private void summon() {
        if (helpers.size() >= 2) return;
        var fields = definition.boss();
        fields.setDisguise(null);
        fields.setName("&7Training Echo");
        fields.setEntityType(switch (definition.root()) {
            case "ranger" -> org.bukkit.entity.EntityType.WOLF;
            case "spellcaster" -> org.bukkit.entity.EntityType.SKELETON;
            case "cleric", "paladin" -> org.bukkit.entity.EntityType.IRON_GOLEM;
            default -> org.bukkit.entity.EntityType.HUSK;
        });
        fields.setHealthMultiplier(.6);
        fields.setDamageMultiplier(.35);
        fields.setBossType(com.magmaguy.elitemobs.mobconstructor.BossType.NORMAL);
        var helper = new CustomBossEntity(fields);
        helper.setSummoningEntity(boss);
        helper.spawn(boss.getLocation(), true);
        if (!helper.exists()) return;
        if (helper.getLivingEntity() instanceof Mob mob) mob.setTarget(player);
        helpers.add(new Helper(helper, tick + 180));
    }

    @Override public void close() {
        closed = true;
        if (steed != null) { steed.remove(); steed = null; }
        pending.clear();
        for (Helper helper : List.copyOf(helpers)) helper.entity.remove(RemovalReason.ARENA_RESET);
        helpers.clear();
    }

    private record Cast(FixedAbilitySpec spec, Location target, int resolveAt) {}
    private record Helper(CustomBossEntity entity, int expires) {}
}
