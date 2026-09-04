package com.magmaguy.elitemobs.mobconstructor.custombosses;

import org.bukkit.Location;

final class MusicPlaybackEligibility {
    private MusicPlaybackEligibility() {
    }

    static boolean isWithinRange(Location listener, Location source, double range) {
        if (listener == null || source == null || range < 0) return false;
        if (listener.getWorld() == null || source.getWorld() == null) return false;
        if (!listener.getWorld().equals(source.getWorld())) return false;
        return listener.distanceSquared(source) <= range * range;
    }
}
