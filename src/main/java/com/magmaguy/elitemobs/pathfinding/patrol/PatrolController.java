package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.easyminecraftgoals.PathfindingStatus;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Owns one logical actor's route state across native body replacement and chunk detachment. */
final class PatrolController {
    private static final int MATERIALIZE_RETRY_TICKS = 20;

    private final PatrolActor actor;
    private final PatrolRoute route;
    private final PatrolStateStore stateStore;

    private PathfindingHandle driver;
    private UUID attachedBodyId;
    private PatrolRuntimeState state = PatrolRuntimeState.RESUMING;
    private int currentNode;
    private int targetNode;
    private int direction = 1;
    private int failures;
    private int materializeCooldown;
    private double fraction;
    private double virtualSpeed;
    private Location safeLocation;
    private boolean destinationAssigned;
    private boolean combatHeld;
    private boolean lifecycleHeld;
    private boolean aiHeld;
    private boolean yieldedHeld;
    private boolean manuallyPaused;
    private Location manualTarget;
    private boolean holdAfterManualMove;
    private boolean terrainHeld;
    private boolean retired;
    private List<Location> latestResolvedPath = List.of();
    private final Deque<Location> detachedPath = new ArrayDeque<>();
    private int persistentChunkX = Integer.MIN_VALUE;
    private int persistentChunkZ = Integer.MIN_VALUE;

    PatrolController(PatrolActor actor, PatrolStateStore stateStore) {
        this.actor = actor;
        this.route = actor.route();
        this.stateStore = stateStore;

        PatrolStateStore.StoredState restored = actor.persistsWhileDetached()
                ? stateStore.get(actor.canonicalIdentity()).orElse(null)
                : null;
        if (restored != null) restore(restored);
        else initializeAtOrigin();
    }

    PatrolActor actor() {
        return actor;
    }

    PatrolRuntimeState state() {
        return state;
    }

    boolean tick(long tick) {
        if (retired) return true;
        LivingEntity body = liveBody();
        if (body == null) {
            if (driver != null || attachedBodyId != null) detachBody();
            if (!actor.persistsWhileDetached()) {
                retired = true;
                state = PatrolRuntimeState.DISABLED;
                return true;
            }
            tickDetached();
        } else {
            if (!body.getUniqueId().equals(attachedBodyId)) attachBody(body);
            tickLoaded(body, tick);
        }

        if (tick % 20L == 0L) checkpoint();
        return retired;
    }

    void shutdown() {
        closeDriver();
        checkpoint();
    }

    void retire(boolean discardPersistentState) {
        retired = true;
        closeDriver();
        if (discardPersistentState) stateStore.remove(actor.canonicalIdentity());
        else checkpoint();
    }

    void setCombatHeld(boolean held) {
        combatHeld = held;
        if (held) {
            clearDestination();
            state = PatrolRuntimeState.YIELDED;
        } else if (!manuallyPaused && manualTarget == null) {
            resumeFromBody();
        }
        checkpoint();
    }

    void holdUntilBodyReplacement() {
        lifecycleHeld = true;
        clearDestination();
        checkpoint();
    }

    void worldUnloaded() {
        checkpoint();
        closeDriver();
        attachedBodyId = null;
        latestResolvedPath = List.of();
        detachedPath.clear();
        terrainHeld = true;
        if (safeLocation != null) safeLocation.setWorld(null);
        state = PatrolRuntimeState.DETACHED;
    }

    void worldLoaded(World world) {
        if (world == null || !actor.origin().worldName().equals(world.getName())) return;
        if (safeLocation != null && safeLocation.getWorld() == null) safeLocation.setWorld(world);
    }

    void pause() {
        manuallyPaused = true;
        clearDestination();
        state = PatrolRuntimeState.PAUSED;
        checkpoint();
    }

    void resume() {
        manuallyPaused = false;
        manualTarget = null;
        holdAfterManualMove = false;
        resumeFromBody();
        checkpoint();
    }

    boolean moveToOffset(Vector offset, boolean holdOnArrival) {
        Location origin = actor.origin().resolve();
        LivingEntity body = liveBody();
        if (origin == null || body == null || offset == null) return false;
        Location target = origin.add(offset);
        if (body.getWorld() != target.getWorld()) return false;
        manuallyPaused = false;
        combatHeld = false;
        manualTarget = target;
        holdAfterManualMove = holdOnArrival;
        failures = 0;
        destinationAssigned = false;
        state = PatrolRuntimeState.MANUAL_MOVING;
        return issueDestination(target);
    }

    boolean teleportToOffset(Vector offset) {
        Location origin = actor.origin().resolve();
        LivingEntity body = liveBody();
        if (origin == null || body == null || offset == null) return false;
        Location target = origin.add(offset);
        if (target.getWorld() != body.getWorld() || NMSManager.getAdapter() == null
                || !NMSManager.getAdapter().isPositionEntityTicking(target)
                || !body.teleport(target)) return false;
        safeLocation = body.getLocation().clone();
        updatePersistentLocation(true);
        resumeFromBody();
        checkpoint();
        return true;
    }

    boolean isActivelyMoving() {
        return state == PatrolRuntimeState.PATROLLING
                || state == PatrolRuntimeState.RESUMING
                || state == PatrolRuntimeState.MANUAL_MOVING;
    }

    Location logicalLocation() {
        LivingEntity body = liveBody();
        if (body != null) return body.getLocation();
        if (manualTarget != null) return safeLocation == null ? manualTarget.clone() : safeLocation.clone();
        try {
            return route.interpolate(actor.origin(), currentNode, targetNode, fraction);
        } catch (RuntimeException exception) {
            return safeLocation == null ? null : safeLocation.clone();
        }
    }

    Location materializationLocation() {
        return safeLocation == null ? null : safeLocation.clone();
    }

    Location leashAnchor(Location bodyLocation) {
        return route.closestPoint(actor.origin(), bodyLocation);
    }

    Map<String, Object> diagnostics() {
        Map<String, Object> diagnostics = new LinkedHashMap<>();
        diagnostics.put("state", state.name());
        diagnostics.put("currentNode", currentNode);
        diagnostics.put("targetNode", targetNode);
        diagnostics.put("direction", direction);
        diagnostics.put("fraction", fraction);
        diagnostics.put("clockFrozen", clockFrozen());
        diagnostics.put("holdReason", holdReason());
        diagnostics.put("bodyPresent", liveBody() != null);
        diagnostics.put("virtualSpeed", virtualSpeed);
        Location logical = logicalLocation();
        if (logical != null) {
            diagnostics.put("world", logical.getWorld() == null ? actor.origin().worldName() : logical.getWorld().getName());
            diagnostics.put("x", logical.getX());
            diagnostics.put("y", logical.getY());
            diagnostics.put("z", logical.getZ());
        }
        return Map.copyOf(diagnostics);
    }

    private void attachBody(LivingEntity body) {
        closeDriver();
        attachedBodyId = body.getUniqueId();
        lifecycleHeld = false;
        terrainHeld = false;
        detachedPath.clear();
        latestResolvedPath = List.of();
        safeLocation = body.getLocation().clone();
        updatePersistentLocation(true);

        if (!actor.prepareBody()) {
            state = PatrolRuntimeState.DISABLED;
            Logger.warn("Patrol disabled for " + actor.displayName() + ": " + actor.ineligibleReason());
            return;
        }
        driver = NMSManager.getAdapter() == null
                ? null
                : NMSManager.getAdapter().createPathfindingHandle(body).orElse(null);
        if (driver == null) {
            state = PatrolRuntimeState.DISABLED;
            Logger.warn("Patrol disabled for " + actor.displayName() + ": no native pathfinding adapter for "
                    + body.getType());
            return;
        }

        double observedSpeed = actor.currentMovementSpeed() * route.speedModifier();
        if (route.virtualSpeed() != null) virtualSpeed = route.virtualSpeed();
        else if (Double.isFinite(observedSpeed) && observedSpeed > 0D) virtualSpeed = observedSpeed;
        resumeFromBody();
    }

    private void detachBody() {
        LivingEntity body = liveBody();
        if (body != null) {
            safeLocation = body.getLocation().clone();
            if (targetNode != currentNode) {
                fraction = route.projectedFraction(actor.origin(), safeLocation, currentNode, targetNode);
            }
        }
        captureResolvedPath();
        prepareDetachedPath();
        closeDriver();
        attachedBodyId = null;
        state = PatrolRuntimeState.DETACHED;
        updatePersistentLocation(true);
        checkpoint();
    }

    private void tickLoaded(LivingEntity body, long tick) {
        actor.syncVisuals();
        safeLocation = body.getLocation().clone();
        terrainHeld = false;
        updatePersistentLocation(false);
        if (targetNode != currentNode && manualTarget == null) {
            fraction = route.projectedFraction(actor.origin(), safeLocation, currentNode, targetNode);
        }

        if (actor.owner() instanceof com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity regionalBoss) {
            Navigation.updateLeashAnchor(regionalBoss, leashAnchor(body.getLocation()));
        }

        if (driver == null || state == PatrolRuntimeState.DISABLED) return;
        if (manuallyPaused) {
            clearDestination();
            state = PatrolRuntimeState.PAUSED;
            return;
        }
        if (combatHeld || actor.isInCombat()) {
            clearDestination();
            state = PatrolRuntimeState.YIELDED;
            return;
        }
        if (!body.hasAI() || body instanceof Mob mob && !mob.isAware()) {
            aiHeld = true;
            clearDestination();
            state = PatrolRuntimeState.AI_OFF;
            return;
        }
        boolean resumingAfterAiHold = aiHeld;
        aiHeld = false;
        if (resumingAfterAiHold) {
            resumeFromBody();
            return;
        }

        PathfindingStatus status = driver.status();
        if (tick % 5L == 0L) captureResolvedPath();
        if (manualTarget != null) {
            tickManual(status);
            return;
        }
        if (!destinationAssigned || status == PathfindingStatus.IDLE) {
            issueRouteDestination();
            return;
        }
        switch (status) {
            case ARRIVED -> routeArrived();
            case NO_PATH, ENDED_SHORT, STUCK -> routeFailed();
            case CLOSED -> attachBody(body);
            case WAITING -> {
                yieldedHeld = true;
                state = PatrolRuntimeState.YIELDED;
            }
            case MOVING -> {
                yieldedHeld = false;
                if (state != PatrolRuntimeState.RESUMING) state = PatrolRuntimeState.PATROLLING;
            }
            case IDLE -> issueRouteDestination();
        }
    }

    private void tickManual(PathfindingStatus status) {
        if (!destinationAssigned || status == PathfindingStatus.IDLE) {
            issueDestination(manualTarget);
            return;
        }
        switch (status) {
            case ARRIVED -> {
                destinationAssigned = false;
                manualTarget = null;
                failures = 0;
                if (holdAfterManualMove) {
                    manuallyPaused = true;
                    state = PatrolRuntimeState.HELD;
                } else {
                    resumeFromBody();
                }
                checkpoint();
            }
            case NO_PATH, ENDED_SHORT, STUCK -> {
                destinationAssigned = false;
                if (failures++ == 0) issueDestination(manualTarget);
                else {
                    manualTarget = null;
                    manuallyPaused = holdAfterManualMove;
                    state = holdAfterManualMove ? PatrolRuntimeState.HELD : PatrolRuntimeState.PAUSED;
                    checkpoint();
                }
            }
            case CLOSED -> state = PatrolRuntimeState.DISABLED;
            case WAITING -> {
                yieldedHeld = true;
                state = PatrolRuntimeState.YIELDED;
            }
            case MOVING -> {
                yieldedHeld = false;
                state = PatrolRuntimeState.MANUAL_MOVING;
            }
            case IDLE -> issueDestination(manualTarget);
        }
    }

    private void tickDetached() {
        state = PatrolRuntimeState.DETACHED;
        if (materializeCooldown > 0) materializeCooldown--;
        if (!clockFrozen() && manualTarget == null && virtualSpeed > 0D) advanceDetachedPath(virtualSpeed);
        updatePersistentLocation(false);

        if (safeLocation != null
                && materializeCooldown == 0
                && NMSManager.getAdapter() != null
                && NMSManager.getAdapter().isPositionEntityTicking(safeLocation)) {
            materializeCooldown = MATERIALIZE_RETRY_TICKS;
            actor.materialize();
        }
    }

    private void advanceDetachedPath(double blocks) {
        if (safeLocation == null || safeLocation.getWorld() == null || detachedPath.isEmpty()) {
            terrainHeld = true;
            return;
        }
        terrainHeld = false;
        double remaining = blocks;
        int guard = 0;
        while (remaining > 0D && !detachedPath.isEmpty() && guard++ < 1024) {
            Location target = detachedPath.getFirst();
            if (target.getWorld() != safeLocation.getWorld()) {
                detachedPath.clear();
                terrainHeld = true;
                return;
            }
            Vector delta = target.toVector().subtract(safeLocation.toVector());
            double distance = delta.length();
            if (distance <= 1.0E-6D) {
                detachedPath.removeFirst();
                continue;
            }
            if (remaining + 1.0E-9D < distance) {
                safeLocation.add(delta.multiply(remaining / distance));
                remaining = 0D;
            } else {
                remaining -= distance;
                safeLocation = detachedPath.removeFirst().clone();
            }
        }
        fraction = route.projectedFraction(actor.origin(), safeLocation, currentNode, targetNode);
        updatePersistentLocation(false);
        Location authoredTarget = route.node(actor.origin(), targetNode);
        if (safeLocation.distanceSquared(authoredTarget) <= 0.25D) {
            currentNode = targetNode;
            fraction = 0D;
            PatrolRoute.Step step = route.next(currentNode, direction);
            targetNode = step.targetNode();
            direction = step.direction();
            detachedPath.clear();
        }
        if (detachedPath.isEmpty()) terrainHeld = true;
    }

    private void captureResolvedPath() {
        if (driver == null) return;
        List<Location> resolved = driver.routePreview();
        latestResolvedPath = resolved.size() < 2
                ? List.of()
                : resolved.stream().map(Location::clone).toList();
    }

    private void prepareDetachedPath() {
        detachedPath.clear();
        if (safeLocation == null || safeLocation.getWorld() == null || latestResolvedPath.isEmpty()) {
            terrainHeld = true;
            return;
        }
        int nextPoint = 1;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (int index = 0; index < latestResolvedPath.size() - 1; index++) {
            Location start = latestResolvedPath.get(index);
            Location end = latestResolvedPath.get(index + 1);
            if (start.getWorld() != safeLocation.getWorld() || end.getWorld() != safeLocation.getWorld()) continue;
            double distance = distanceSquaredToSegment(safeLocation, start, end);
            if (distance < nearestDistance) {
                nextPoint = index + 1;
                nearestDistance = distance;
            }
        }
        for (int index = nextPoint; index < latestResolvedPath.size(); index++) {
            Location candidate = latestResolvedPath.get(index);
            if (candidate.getWorld() == safeLocation.getWorld()) detachedPath.addLast(candidate.clone());
        }
        terrainHeld = detachedPath.isEmpty();
    }

    private static double distanceSquaredToSegment(Location point, Location start, Location end) {
        Vector segment = end.toVector().subtract(start.toVector());
        double lengthSquared = segment.lengthSquared();
        if (lengthSquared <= 1.0E-8D) return point.distanceSquared(start);
        double fraction = point.toVector().subtract(start.toVector()).dot(segment) / lengthSquared;
        fraction = Math.max(0D, Math.min(1D, fraction));
        return point.toVector().distanceSquared(start.toVector().add(segment.multiply(fraction)));
    }

    private void routeArrived() {
        destinationAssigned = false;
        failures = 0;
        currentNode = targetNode;
        fraction = 0D;
        safeLocation = route.node(actor.origin(), currentNode);
        latestResolvedPath = List.of();
        detachedPath.clear();
        PatrolRoute.Step step = route.next(currentNode, direction);
        targetNode = step.targetNode();
        direction = step.direction();
        state = PatrolRuntimeState.PATROLLING;
        updatePersistentLocation(true);
        checkpoint();
        issueRouteDestination();
    }

    private void routeFailed() {
        destinationAssigned = false;
        if (failures++ == 0) {
            issueRouteDestination();
            return;
        }

        failures = 0;
        currentNode = targetNode;
        PatrolRoute.Step step = route.next(currentNode, direction);
        targetNode = step.targetNode();
        direction = step.direction();
        fraction = 0D;
        checkpoint();
        issueRouteDestination();
    }

    private boolean issueRouteDestination() {
        Location target;
        try {
            target = route.node(actor.origin(), targetNode);
        } catch (RuntimeException exception) {
            state = PatrolRuntimeState.DETACHED;
            return false;
        }
        return issueDestination(target);
    }

    private boolean issueDestination(Location target) {
        if (driver == null || target == null) return false;
        yieldedHeld = false;
        destinationAssigned = driver.moveTo(target, route.speedModifier());
        if (!destinationAssigned) state = PatrolRuntimeState.DISABLED;
        return destinationAssigned;
    }

    private void resumeFromBody() {
        if (driver == null) return;
        LivingEntity body = liveBody();
        if (body == null) return;
        targetNode = route.nearestNode(actor.origin(), body.getLocation());
        fraction = 0D;
        failures = 0;
        destinationAssigned = false;
        state = PatrolRuntimeState.RESUMING;
        issueRouteDestination();
    }

    private void clearDestination() {
        if (driver != null) driver.clearDestination();
        destinationAssigned = false;
    }

    private void closeDriver() {
        if (driver != null) driver.close();
        driver = null;
        destinationAssigned = false;
    }

    private LivingEntity liveBody() {
        LivingEntity body = actor.body();
        return body != null && body.isValid() && !body.isDead() ? body : null;
    }

    private void initializeAtOrigin() {
        currentNode = 0;
        targetNode = route.next(0, 1).targetNode();
        direction = 1;
        fraction = 0D;
        virtualSpeed = route.virtualSpeed() == null ? 0D : route.virtualSpeed();
        safeLocation = route.node(actor.origin(), currentNode);
    }

    private void restore(PatrolStateStore.StoredState restored) {
        currentNode = validNode(restored.currentNode()) ? restored.currentNode() : 0;
        targetNode = validNode(restored.targetNode()) ? restored.targetNode() : route.next(currentNode, 1).targetNode();
        if (targetNode == currentNode) targetNode = route.next(currentNode, restored.direction()).targetNode();
        direction = restored.direction() < 0 ? -1 : 1;
        fraction = Math.max(0D, Math.min(1D, restored.fraction()));
        virtualSpeed = route.virtualSpeed() == null ? restored.virtualSpeed() : route.virtualSpeed();
        safeLocation = restored.safeLocation() == null ? null : restored.safeLocation().resolve();
        if (safeLocation == null || safeLocation.getWorld() == null
                || !actor.origin().worldName().equals(safeLocation.getWorld().getName())) {
            safeLocation = route.node(actor.origin(), currentNode);
        }
    }

    private boolean validNode(int node) {
        return node >= 0 && node < route.size();
    }

    private void updatePersistentLocation(boolean force) {
        if (safeLocation == null || safeLocation.getWorld() == null) return;
        int chunkX = safeLocation.getBlockX() >> 4;
        int chunkZ = safeLocation.getBlockZ() >> 4;
        if (!force && chunkX == persistentChunkX && chunkZ == persistentChunkZ) return;
        persistentChunkX = chunkX;
        persistentChunkZ = chunkZ;
        actor.updatePersistentLocation(safeLocation.clone());
    }

    private void checkpoint() {
        if (!actor.persistsWhileDetached()) return;
        stateStore.put(actor.canonicalIdentity(), new PatrolStateStore.StoredState(
                actor.canonicalIdentity(), currentNode, targetNode, direction, fraction, virtualSpeed,
                PatrolStateStore.StoredLocation.from(safeLocation, actor.origin().worldName())));
    }

    private boolean clockFrozen() {
        return combatHeld || lifecycleHeld || aiHeld || yieldedHeld || manuallyPaused
                || terrainHeld || state == PatrolRuntimeState.HELD || state == PatrolRuntimeState.AI_OFF;
    }

    private String holdReason() {
        if (combatHeld) return "combat";
        if (lifecycleHeld) return "body_replacement";
        if (aiHeld) return "ai_off";
        if (yieldedHeld) return "yielded";
        if (terrainHeld) return "terrain_unavailable";
        if (state == PatrolRuntimeState.AI_OFF) return "ai_off";
        if (state == PatrolRuntimeState.HELD) return "script_hold";
        if (manuallyPaused) return "manual";
        return "";
    }
}
