package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.UUID;

final class NpcPatrolActor implements PatrolActor {
    private final NPCEntity npc;
    private final PatrolOrigin origin;
    private final String canonicalIdentity;
    private long nextProximityCheck;
    private boolean nearPlayer;

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
            nearPlayer = false;
            nextProximityCheck = 0L;
            return false;
        }
        if (tick < nextProximityCheck) return nearPlayer;
        nextProximityCheck = tick + 20L;
        nearPlayer = false;
        Location location = body.getLocation();
        for (var entity : body.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player player && player.isValid() && !player.isDead()
                    && player.getGameMode() != GameMode.SPECTATOR
                    && player.getLocation().distanceSquared(location) <= radius * radius) {
                nearPlayer = true;
                break;
            }
        }
        return nearPlayer;
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
