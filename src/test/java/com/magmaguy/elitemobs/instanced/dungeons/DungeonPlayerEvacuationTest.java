package com.magmaguy.elitemobs.instanced.dungeons;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DungeonPlayerEvacuationTest {

    @Test
    void doesNotClearSpectatorTargetForANonSpectator() {
        AtomicInteger clearCalls = new AtomicInteger();
        Player player = player(GameMode.SURVIVAL, clearCalls);

        DungeonPlayerEvacuation.clearSpectatorTargetIfNeeded(player);

        assertEquals(0, clearCalls.get());
    }

    @Test
    void clearsSpectatorTargetBeforeMovingASpectator() {
        AtomicInteger clearCalls = new AtomicInteger();
        Player player = player(GameMode.SPECTATOR, clearCalls);

        DungeonPlayerEvacuation.clearSpectatorTargetIfNeeded(player);

        assertEquals(1, clearCalls.get());
    }

    private static Player player(GameMode gameMode, AtomicInteger clearCalls) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getGameMode" -> gameMode;
                    case "setSpectatorTarget" -> {
                        clearCalls.incrementAndGet();
                        yield null;
                    }
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
