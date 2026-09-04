package com.magmaguy.elitemobs.mobconstructor.custombosses;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicPlaybackEligibilityTest {

    @Test
    void rejectsCrossWorldLocationsWithoutAskingBukkitForTheirDistance() {
        Location listener = new Location(world("listener"), 0, 64, 0);
        Location source = new Location(world("source"), 0, 64, 0);

        assertFalse(MusicPlaybackEligibility.isWithinRange(listener, source, 32));
    }

    @Test
    void acceptsOnlyLocationsInsideTheConfiguredRangeInTheSameWorld() {
        World world = world("shared");

        assertTrue(MusicPlaybackEligibility.isWithinRange(
                new Location(world, 0, 64, 0), new Location(world, 3, 64, 4), 5));
        assertFalse(MusicPlaybackEligibility.isWithinRange(
                new Location(world, 0, 64, 0), new Location(world, 3, 64, 4), 4.99));
    }

    @Test
    void rejectsMissingLocationsAndWorlds() {
        World world = world("present");

        assertFalse(MusicPlaybackEligibility.isWithinRange(null, new Location(world, 0, 0, 0), 5));
        assertFalse(MusicPlaybackEligibility.isWithinRange(new Location(world, 0, 0, 0), null, 5));
        assertFalse(MusicPlaybackEligibility.isWithinRange(
                new Location(null, 0, 0, 0), new Location(world, 0, 0, 0), 5));
    }

    private static World world(String name) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getName", "toString" -> name;
                    case "equals" -> proxy == arguments[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        throw new IllegalArgumentException("Unsupported primitive " + type);
    }
}
