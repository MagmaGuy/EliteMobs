package com.magmaguy.elitemobs.api.power;

/** Classloader-safe world position accepted in a semantic power-action payload. */
public record ElitePowerActionPosition(String world, double x, double y, double z) {
    public ElitePowerActionPosition {
        if (world == null || world.isBlank()) throw new IllegalArgumentException("world cannot be blank");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Power action positions must be finite");
        }
    }
}
