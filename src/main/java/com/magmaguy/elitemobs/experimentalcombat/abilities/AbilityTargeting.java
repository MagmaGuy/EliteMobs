package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.magmacore.util.EntityAimAssist;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class AbilityTargeting {
    private final AbilitySemantics semantics;

    AbilityTargeting(AbilitySemantics semantics) {
        this.semantics = Objects.requireNonNull(semantics, "semantics");
    }

    TargetSelection select(Player caster, FixedAbilitySpec spec) {
        Location origin = spec.target() == AbilityTarget.AIMED_LOCATION
                ? aimedLocation(caster, spec.tuning().range())
                : caster.getLocation();
        return select(caster, spec, origin);
    }

    TargetSelection select(Player caster, FixedAbilitySpec spec, Location origin) {
        AbilityTuning tuning = spec.tuning();
        return switch (spec.target()) {
            case SELF -> new TargetSelection(List.of(), List.of(caster), origin);
            case AIMED_ENEMY -> aimedEnemy(caster, spec)
                    .map(enemy -> new TargetSelection(List.of(enemy), List.of(), enemy.getLocation()))
                    .orElseGet(() -> new TargetSelection(List.of(), List.of(), origin));
            case NEARBY_ENEMIES -> new TargetSelection(enemiesNear(caster, origin, tuning.radius(), spec), List.of(), origin);
            case FORWARD_ENEMIES -> new TargetSelection(enemiesForward(caster, tuning.range(), tuning.radius(), spec), List.of(), origin);
            case AIMED_ALLY -> aimedAlly(caster, spec)
                    .map(ally -> new TargetSelection(List.of(), List.of(ally), ally.getLocation()))
                    .orElseGet(() -> new TargetSelection(List.of(), List.of(caster), caster.getLocation()));
            case NEARBY_ALLIES -> new TargetSelection(List.of(), alliesNear(caster, origin, tuning.radius()), origin);
            case MIXED_NEARBY, AIMED_LOCATION -> new TargetSelection(
                    enemiesNear(caster, origin, tuning.radius(), spec),
                    alliesNear(caster, origin, tuning.radius()),
                    origin);
        };
    }

    TargetSelection selectArea(
            Player caster,
            FixedAbilitySpec spec,
            Location origin,
            double radius) {
        return new TargetSelection(
                enemiesNear(caster, origin, radius, spec),
                alliesNear(caster, origin, radius),
                origin);
    }

    List<LivingEntity> piercingEnemies(Player caster, FixedAbilitySpec spec) {
        Location eye = caster.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        double maximumDistance = obstructionDistance(eye, direction, spec.tuning().range());
        List<PiercingLinePolicy.Candidate<LivingEntity>> candidates = new ArrayList<>();
        for (Entity entity : caster.getWorld().getNearbyEntities(
                caster.getLocation(), maximumDistance, maximumDistance, maximumDistance)) {
            if (!(entity instanceof LivingEntity living)
                    || !semantics.canTargetEnemy(caster, living, spec)) continue;
            BoundingBox box = living.getBoundingBox();
            candidates.add(new PiercingLinePolicy.Candidate<>(living,
                    new PiercingLinePolicy.Box(
                            box.getMinX() - .35D, box.getMinY() - .35D, box.getMinZ() - .35D,
                            box.getMaxX() + .35D, box.getMaxY() + .35D, box.getMaxZ() + .35D)));
        }
        return PiercingLinePolicy.intersections(
                        new PiercingLinePolicy.Point(eye.getX(), eye.getY(), eye.getZ()),
                        new PiercingLinePolicy.Point(
                                direction.getX(), direction.getY(), direction.getZ()),
                        maximumDistance,
                        candidates)
                .stream()
                .map(PiercingLinePolicy.Intersection::target)
                .toList();
    }

    Optional<Player> aimedAlly(Player caster, FixedAbilitySpec spec) {
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.ASSISTED_ALLY_TARGETING))
            return EntityAimAssist.select(caster, validAllies(caster), spec.tuning().range(),
                    EntityAimAssist.DEFAULT_CONE_DEGREES,
                    ally -> ally.getGameMode() != org.bukkit.GameMode.SPECTATOR && caster.canSee(ally),
                    ally -> 0, 1).stream().findFirst();
        return aimedAlly(caster, spec.tuning().range());
    }

    Optional<Player> aimedAlly(Player caster, double range) {
        if (range <= 0) return Optional.empty();
        Location eye = caster.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        double obstructionDistance = obstructionDistance(eye, direction, range);

        return validAllies(caster).stream()
                .filter(candidate -> !candidate.getUniqueId().equals(caster.getUniqueId()))
                .map(candidate -> aimedCandidate(eye, direction, candidate, obstructionDistance))
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(AimedCandidate::perpendicularDistance)
                        .thenComparingDouble(AimedCandidate::forwardDistance))
                .map(AimedCandidate::player);
    }

    boolean unobstructedFrom(Location observerFeet, double observerHeight, LivingEntity target) {
        if (observerFeet.getWorld() == null || !observerFeet.getWorld().equals(target.getWorld())) return false;
        Location source = observerFeet.clone().add(0, Math.max(.5D, observerHeight * .5D), 0);
        Location destination = target.getLocation().add(0, target.getHeight() * .5D, 0);
        return unobstructed(source, destination);
    }

    Location aimedLocation(Player caster, double range) {
        Location eye = caster.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        RayTraceResult collision = caster.getWorld().rayTraceBlocks(
                eye, direction, Math.max(1D, range), FluidCollisionMode.NEVER, true);
        if (collision != null && collision.getHitPosition() != null)
            return collision.getHitPosition().toLocation(caster.getWorld());
        return eye.clone().add(direction.multiply(Math.max(1D, range)));
    }

    private Optional<LivingEntity> aimedEnemy(Player caster, FixedAbilitySpec spec) {
        double range = spec.tuning().range();
        if (range <= 0) return Optional.empty();
        RayTraceResult result = caster.getWorld().rayTraceEntities(
                caster.getEyeLocation(), caster.getEyeLocation().getDirection(), range, 0.6,
                entity -> entity instanceof LivingEntity living
                        && semantics.canTargetEnemy(caster, living, spec));
        if (result == null || !(result.getHitEntity() instanceof LivingEntity living)) return Optional.empty();
        Vector direction = caster.getEyeLocation().getDirection().normalize();
        double enemyDistance = living.getLocation().toVector().subtract(caster.getEyeLocation().toVector()).length();
        return enemyDistance <= obstructionDistance(caster.getEyeLocation(), direction, range) + 0.75D
                ? Optional.of(living)
                : Optional.empty();
    }

    private List<LivingEntity> enemiesNear(
            Player caster,
            Location center,
            double radius,
            FixedAbilitySpec spec) {
        if (center.getWorld() == null || radius <= 0) return List.of();
        List<LivingEntity> enemies = new ArrayList<>();
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living)
                    || !semantics.canTargetEnemy(caster, living, spec)) continue;
            if (living.getLocation().distanceSquared(center) <= radius * radius) enemies.add(living);
        }
        enemies.sort(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(center)));
        return List.copyOf(enemies);
    }

    private List<LivingEntity> enemiesForward(
            Player caster,
            double range,
            double width,
            FixedAbilitySpec spec) {
        if (range <= 0) return List.of();
        Location origin = caster.getEyeLocation();
        Vector direction = origin.getDirection().normalize();
        List<LivingEntity> enemies = enemiesNear(caster, caster.getLocation(), range, spec);
        return enemies.stream()
                .map(enemy -> forwardCandidate(origin, direction, enemy, range, Math.max(1D, width)))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(ForwardCandidate::forwardDistance))
                .map(ForwardCandidate::entity)
                .toList();
    }

    private List<Player> alliesNear(Player caster, Location center, double radius) {
        if (center.getWorld() == null) return List.of();
        double effectiveRadius = Math.max(0D, radius);
        return validAllies(caster).stream()
                .filter(ally -> ally.getWorld().equals(center.getWorld()))
                .filter(ally -> effectiveRadius == 0D || ally.getLocation().distanceSquared(center) <= effectiveRadius * effectiveRadius)
                .sorted(Comparator.comparingDouble(ally -> ally.getLocation().distanceSquared(center)))
                .toList();
    }

    private List<Player> validAllies(Player caster) {
        Map<UUID, Player> allies = new LinkedHashMap<>();
        allies.put(caster.getUniqueId(), caster);
        for (Player ally : semantics.alliesOf(caster)) {
            if (ally == null || !ally.isOnline() || !ally.isValid() || ally.isDead()) continue;
            if (!ally.getWorld().equals(caster.getWorld())) continue;
            allies.put(ally.getUniqueId(), ally);
        }
        return List.copyOf(allies.values());
    }

    private static AimedCandidate aimedCandidate(Location eye, Vector direction, Player candidate,
                                                  double obstructionDistance) {
        Vector toTarget = candidate.getEyeLocation().toVector().subtract(eye.toVector());
        double forward = toTarget.dot(direction);
        if (forward <= 0 || forward > obstructionDistance) return null;
        double perpendicularSquared = Math.max(0D, toTarget.lengthSquared() - forward * forward);
        if (perpendicularSquared > 2.25D) return null;
        return new AimedCandidate(candidate, forward, Math.sqrt(perpendicularSquared));
    }

    private static ForwardCandidate forwardCandidate(Location origin, Vector direction, LivingEntity entity,
                                                      double range, double width) {
        Vector toTarget = entity.getLocation().add(0, entity.getHeight() * .5, 0).toVector()
                .subtract(origin.toVector());
        double forward = toTarget.dot(direction);
        if (forward <= 0 || forward > range) return null;
        double perpendicularSquared = Math.max(0D, toTarget.lengthSquared() - forward * forward);
        if (perpendicularSquared > width * width) return null;
        return new ForwardCandidate(entity, forward);
    }

    private static boolean unobstructed(Location source, Location destination) {
        World world = source.getWorld();
        if (world == null || destination.getWorld() == null || !world.equals(destination.getWorld())) return false;
        Vector delta = destination.toVector().subtract(source.toVector());
        double distance = delta.length();
        if (distance < 1.0E-6D) return true;
        RayTraceResult collision = world.rayTraceBlocks(
                source, delta.normalize(), distance, FluidCollisionMode.NEVER, true);
        return collision == null || collision.getHitPosition() == null
                || collision.getHitPosition().distance(source.toVector()) >= distance - .1D;
    }

    private static double obstructionDistance(Location eye, Vector direction, double range) {
        World world = eye.getWorld();
        if (world == null) return 0;
        RayTraceResult collision = world.rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);
        return collision == null || collision.getHitPosition() == null
                ? range
                : collision.getHitPosition().distance(eye.toVector());
    }

    record TargetSelection(List<LivingEntity> enemies, List<Player> allies, Location origin) {
        TargetSelection {
            enemies = List.copyOf(enemies);
            allies = List.copyOf(allies);
            origin = origin.clone();
        }

        boolean isEmpty() {
            return enemies.isEmpty() && allies.isEmpty();
        }
    }

    private record AimedCandidate(Player player, double forwardDistance, double perpendicularDistance) {
    }

    private record ForwardCandidate(LivingEntity entity, double forwardDistance) {
    }

}
