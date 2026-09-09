package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.UUID;

final class NpcPatrolActor implements PatrolActor {
    private final NPCEntity npc;
    private final PatrolOrigin origin;
    private final String canonicalIdentity;
    private long nextProximityCheck;
    private UUID nearestPlayerId;
    private CustomModel facingModel;

    NpcPatrolActor(NPCEntity npc) {
        this.npc = npc;
        this.origin = PatrolOrigin.from(npc.getSpawnLocation(), npc.getWorldName());
        this.canonicalIdentity = PatrolIdentity.canonical(
                "npc", npc.getNPCsConfigFields().getFilename(), origin);
    }

    @Override public UUID runtimeId() { return npc.getUuid(); }
    @Override public String canonicalIdentity() { return canonicalIdentity; }
    @Override public String displayName() { return npc.getNPCsConfigFields().getFilename(); }
    @Override public PatrolOrigin origin() { return origin; }
    @Override public PatrolRoute route() { return npc.getNPCsConfigFields().getPatrolRoute(); }
    @Override public LivingEntity body() { return npc.getVillager(); }
    @Override public boolean persistsWhileDetached() { return !npc.getNPCsConfigFields().isInstanced(); }
    @Override public boolean isInCombat() { return false; }

    @Override
    public boolean isNearPlayer(long tick) {
        LivingEntity body = body();
        double radius = npc.getNPCsConfigFields().getPatrolPauseNearPlayersRadius();
        if (radius <= 0D || body == null || !body.isValid() || body.isDead()) {
            nearestPlayerId = null;
            nextProximityCheck = 0L;
            return false;
        }
        if (tick < nextProximityCheck) return nearestPlayerId != null;
        nextProximityCheck = tick + 20L;
        nearestPlayerId = null;
        Location location = body.getLocation();
        double nearestDistance = radius * radius;
        for (var entity : body.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player player && player.isValid() && !player.isDead()
                    && player.getGameMode() != GameMode.SPECTATOR) {
                double distance = player.getLocation().distanceSquared(location);
                // Stable tie-breaker avoids alternating between players standing at the same distance.
                if (distance < nearestDistance || distance == nearestDistance
                        && (nearestPlayerId == null || player.getUniqueId().compareTo(nearestPlayerId) < 0)) {
                    nearestDistance = distance;
                    nearestPlayerId = player.getUniqueId();
                }
            }
        }
        return nearestPlayerId != null;
    }

    @Override
    public void tickFacing(boolean paused) {
        CustomModel model = npc.getCustomModel();
        if (facingModel != null && facingModel != model) {
            facingModel.setLookTarget(null);
            facingModel = null;
        }
        Location target = null;
        LivingEntity body = body();
        if (paused && npc.getNPCsConfigFields().isPatrolFaceNearbyPlayers() && nearestPlayerId != null
                && !PatrolEditor.isEditing(npc) && body != null && body.isValid() && !body.isDead()) {
            Player player = Bukkit.getPlayer(nearestPlayerId);
            double radius = npc.getNPCsConfigFields().getPatrolPauseNearPlayersRadius();
            if (player != null && player.isValid() && !player.isDead()
                    && player.getGameMode() != GameMode.SPECTATOR && player.getWorld() == body.getWorld()
                    && player.getLocation().distanceSquared(body.getLocation()) <= radius * radius)
                target = player.getEyeLocation();
        }
        if (target != null && model != null && model.setLookTarget(target)) {
            facingModel = model;
            return;
        }
        if (facingModel != null) {
            facingModel.setLookTarget(null);
            facingModel = null;
        }
        if (target != null) {
            // Unmodeled NPCs retain a Bukkit-only fallback. FMM handles its own gaze above.
            Location eyes = body.getEyeLocation();
            var direction = target.toVector().subtract(eyes.toVector());
            if (direction.lengthSquared() > 1.0E-8D) {
                eyes.setDirection(direction);
                body.setRotation(eyes.getYaw(), eyes.getPitch());
            }
        }
    }

    @Override
    public boolean prepareBody() {
        npc.enablePatrolMovementSync();
        LivingEntity body = body();
        if (!(body instanceof Mob mob) || !body.isValid() || body.isInsideVehicle()
                || NMSManager.getAdapter() == null) return false;
        mob.setAI(true);
        mob.setAware(true);
        return NMSManager.getAdapter().removeFreeWill(body);
    }

    @Override
    public String ineligibleReason() {
        LivingEntity body = body();
        if (body != null && body.isInsideVehicle()) return "the routed NPC is a passenger";
        return "the NPC body cannot use native pathfinding";
    }

    @Override
    public double currentMovementSpeed() {
        LivingEntity body = body();
        return body == null ? 0D : AttributeManager.getAttributeBaseValue(body, "generic_movement_speed");
    }

    @Override public void updatePersistentLocation(Location safeLocation) {
        npc.updatePatrolPersistentLocation(safeLocation);
    }
    @Override public void materialize() { npc.chunkLoad(); }
    @Override public void syncVisuals() { npc.syncPatrolVisuals(); }
    @Override public Object owner() { return npc; }
}
