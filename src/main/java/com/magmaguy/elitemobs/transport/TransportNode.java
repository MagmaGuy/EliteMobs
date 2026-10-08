package com.magmaguy.elitemobs.transport;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.util.Set;

/** A point in a flight network. A name makes it a destination that menus offer. */
public record TransportNode(String id, Vector location, String name, String permission, String lockedHint,
                            float arrivalYaw, Set<String> neighbors, boolean placeholder) {
    public TransportNode {
        if (id == null || !id.matches("[a-z0-9_-]{1,64}")) throw new IllegalArgumentException("Invalid node id: " + id);
        if (location == null) throw new IllegalArgumentException("Node " + id + " needs a location");
        if (!Double.isFinite(location.getX()) || !Double.isFinite(location.getY()) || !Double.isFinite(location.getZ()))
            throw new IllegalArgumentException("Node " + id + " needs finite coordinates");
        if (!Float.isFinite(arrivalYaw)) throw new IllegalArgumentException("Node " + id + " has an invalid arrival yaw");
        location = location.clone();
        neighbors = Set.copyOf(neighbors);
        if (name != null && name.isBlank()) name = null;
        if (permission != null && permission.isBlank()) permission = null;
        if (lockedHint != null && lockedHint.isBlank()) lockedHint = null;
    }
    @Override public Vector location() { return location.clone(); }
    public boolean destination() { return name != null; }
    public boolean unlockedFor(Player player) {
        return com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns.mayEnter(player, permission);
    }

    TransportNode withLocation(Vector next) { return new TransportNode(id, next, name, permission, lockedHint, arrivalYaw, neighbors, placeholder); }
    TransportNode withNeighbors(Set<String> next) { return new TransportNode(id, location, name, permission, lockedHint, arrivalYaw, next, placeholder); }
    TransportNode withDestination(String nextName, float nextYaw) { return new TransportNode(id, location, nextName, permission, lockedHint, nextYaw, neighbors, false); }
    TransportNode withPermission(String next) { return new TransportNode(id, location, name, next, lockedHint, arrivalYaw, neighbors, placeholder); }
    TransportNode withHint(String next) { return new TransportNode(id, location, name, permission, next, arrivalYaw, neighbors, placeholder); }
}
