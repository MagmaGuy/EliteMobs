package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class BossHealthBarTargetTest {

    @Test
    void bossBarFollowsOnlyTheLivePlayerTarget() {
        World world = world();
        AtomicBoolean playerOnline = new AtomicBoolean(true);
        Player firstPlayer = player(world, playerOnline);
        Player secondPlayer = player(world, new AtomicBoolean(true));
        AtomicReference<LivingEntity> target = new AtomicReference<>(firstPlayer);
        TestEliteEntity eliteEntity = new TestEliteEntity(mob(world, target));

        assertNull(BossHealthBarManager.currentAggroTarget(eliteEntity));

        eliteEntity.setInCombat(true);
        assertSame(firstPlayer, BossHealthBarManager.currentAggroTarget(eliteEntity));

        target.set(secondPlayer);
        assertSame(secondPlayer, BossHealthBarManager.currentAggroTarget(eliteEntity));

        target.set(null);
        assertNull(BossHealthBarManager.currentAggroTarget(eliteEntity));

        target.set(firstPlayer);
        playerOnline.set(false);
        assertNull(BossHealthBarManager.currentAggroTarget(eliteEntity));
    }

    private static World world() {
        UUID uniqueId = UUID.randomUUID();
        return proxy(World.class, (instance, method, arguments) -> switch (method.getName()) {
            case "getUID" -> uniqueId;
            case "hashCode" -> System.identityHashCode(instance);
            case "equals" -> instance == arguments[0];
            case "toString" -> "TestWorld[" + uniqueId + "]";
            default -> defaultValue(method.getReturnType());
        });
    }

    private static Player player(World world, AtomicBoolean online) {
        UUID uniqueId = UUID.randomUUID();
        return proxy(Player.class, (instance, method, arguments) -> switch (method.getName()) {
            case "getUniqueId" -> uniqueId;
            case "getWorld" -> world;
            case "isOnline", "isValid" -> online.get();
            case "isDead" -> false;
            case "hashCode" -> System.identityHashCode(instance);
            case "equals" -> instance == arguments[0];
            case "toString" -> "TestPlayer[" + uniqueId + "]";
            default -> defaultValue(method.getReturnType());
        });
    }

    private static Mob mob(World world, AtomicReference<LivingEntity> target) {
        return proxy(Mob.class, (instance, method, arguments) -> switch (method.getName()) {
            case "getTarget" -> target.get();
            case "getWorld" -> world;
            case "isValid" -> true;
            case "isDead" -> false;
            case "hashCode" -> System.identityHashCode(instance);
            case "equals" -> instance == arguments[0];
            case "toString" -> "TestMob";
            default -> defaultValue(method.getReturnType());
        });
    }

    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        throw new IllegalArgumentException("Unsupported primitive: " + type);
    }

    private static final class TestEliteEntity extends EliteEntity {
        private TestEliteEntity(LivingEntity livingEntity) {
            this.livingEntity = livingEntity;
        }
    }
}
