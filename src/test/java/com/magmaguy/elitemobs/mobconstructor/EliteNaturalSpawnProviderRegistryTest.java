package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnClaim;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnContext;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnSuppression;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EliteNaturalSpawnProviderRegistryTest {

    @Test
    void selectsOneOwnerClaimAndRemovesItWithTheOwner() {
        EliteNaturalSpawnProviderRegistry registry = new EliteNaturalSpawnProviderRegistry();
        Plugin owner = plugin("RagnaBlock");
        EliteNaturalSpawnClaim claim = new EliteNaturalSpawnSuppression("profile is progression-locked");
        registry.register(owner, context -> Optional.of(claim));

        assertEquals(claim, registry.select(context()).orElseThrow().claim());

        registry.unregister(owner);
        assertTrue(registry.select(context()).isEmpty());
    }

    @Test
    void rejectsAmbiguousClaimsInsteadOfChoosingByListenerOrder() {
        EliteNaturalSpawnProviderRegistry registry = new EliteNaturalSpawnProviderRegistry();
        registry.register(
                plugin("FirstMode"),
                context -> Optional.of(new EliteNaturalSpawnSuppression("first")));
        registry.register(
                plugin("SecondMode"),
                context -> Optional.of(new EliteNaturalSpawnSuppression("second")));

        assertThrows(IllegalStateException.class, () -> registry.select(context()));
    }

    private static EliteNaturalSpawnContext context() {
        return new EliteNaturalSpawnContext(
                (LivingEntity) Proxy.newProxyInstance(
                        LivingEntity.class.getClassLoader(),
                        new Class<?>[]{LivingEntity.class},
                        (proxy, method, arguments) -> defaultValue(method.getReturnType())),
                CreatureSpawnEvent.SpawnReason.NATURAL,
                7);
    }

    private static Plugin plugin(String name) {
        return (Plugin) Proxy.newProxyInstance(
                Plugin.class.getClassLoader(),
                new Class<?>[]{Plugin.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getName" -> name;
                    case "isEnabled" -> true;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    case "toString" -> name;
                    default -> defaultValue(method.getReturnType());
                });
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
        throw new IllegalArgumentException("Unsupported primitive " + type);
    }
}
