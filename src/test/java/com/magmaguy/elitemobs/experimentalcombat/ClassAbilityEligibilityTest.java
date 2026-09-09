package com.magmaguy.elitemobs.experimentalcombat;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassAbilityEligibilityTest {

    @AfterEach
    void clearProvider() {
        ClassAbilityEligibility.clear();
    }

    @Test
    void eligibleCombatContentAndOutsideControlOptInAreIndependentEntryPaths() {
        Player combatContentPlayer = player("combat-content");
        Player outsideOptedPlayer = player("outside-opted");
        Player ordinaryPlayer = player("ordinary");
        ClassAbilityEligibility.install(
                candidate -> candidate == combatContentPlayer,
                candidate -> candidate == outsideOptedPlayer);

        assertTrue(ClassAbilityEligibility.isEligible(combatContentPlayer));
        assertTrue(ClassAbilityEligibility.isEligible(outsideOptedPlayer));
        assertFalse(ClassAbilityEligibility.isEligible(ordinaryPlayer));
    }

    @Test
    void clearingTheClassAbilityPolicyFailsClosed() {
        Player outsideOptedPlayer = player("outside-opted");
        ClassAbilityEligibility.install(ignored -> false, candidate -> candidate == outsideOptedPlayer);
        assertTrue(ClassAbilityEligibility.isEligible(outsideOptedPlayer));

        ClassAbilityEligibility.clear();

        assertFalse(ClassAbilityEligibility.isEligible(outsideOptedPlayer));
    }

    @Test
    void eligibleCombatContentDoesNotConsultSessionOptIn() {
        AtomicBoolean outsideOptInConsulted = new AtomicBoolean();
        ClassAbilityEligibility.install(
                ignored -> true,
                ignored -> {
                    outsideOptInConsulted.set(true);
                    return false;
                });

        assertTrue(ClassAbilityEligibility.isEligible(player("combat-content")));
        assertFalse(outsideOptInConsulted.get());
    }

    private static Player player(String name) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
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
        return 0D;
    }
}
