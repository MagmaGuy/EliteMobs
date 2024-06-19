package com.magmaguy.elitemobs.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class DebugBlockLocation {

    public DebugBlockLocation(Location location) {
        //todo: restore barrier blocks here, block data was required for the snapshot version of this
        location.getWorld().spawnParticle(Particle.BLOCK_MARKER, location.getBlock().getLocation().add(new Vector(0.5, 0.5, 0.5)), 1);
    }

}
