package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassAbilityProjectileCarrierOriginTest {

    @Test
    void physicalCarrierAndVisualFlightStartExactlyThreeTenthsBelowEyeLevel() {
        Location eyeLocation = new Location(null, 12.5D, 80.25D, -4.5D, 37F, -18F);
        AtomicReference<Location> physicalLocation = new AtomicReference<>();
        Projectile carrier = carrierCapturingTeleport(physicalLocation);

        Location flightStart = ClassAbilityProjectileCarrier
                .positionAtLaunchOrigin(carrier, eyeLocation)
                .orElseThrow();

        assertEquals(12.5D, flightStart.getX(), 1.0E-9D);
        assertEquals(79.95D, flightStart.getY(), 1.0E-9D);
        assertEquals(-4.5D, flightStart.getZ(), 1.0E-9D);
        assertEquals(37F, flightStart.getYaw(), 1.0E-6F);
        assertEquals(-18F, flightStart.getPitch(), 1.0E-6F);
        assertEquals(flightStart, physicalLocation.get());
        assertEquals(80.25D, eyeLocation.getY(), 1.0E-9D);
    }

    private static Projectile carrierCapturingTeleport(AtomicReference<Location> physicalLocation) {
        return (Projectile) Proxy.newProxyInstance(
                Projectile.class.getClassLoader(),
                new Class<?>[]{Projectile.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("teleport")
                            && arguments != null
                            && arguments.length > 0
                            && arguments[0] instanceof Location location) {
                        physicalLocation.set(location.clone());
                        return true;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
