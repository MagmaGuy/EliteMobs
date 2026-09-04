package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.experimentalcombat.abilities.ClassControlAttribution;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassPassiveRuntimeTest {

    @Test
    void ordinaryAggroIsNotClassControlButAuthoritativeControlIs() throws Exception {
        Player player = proxy(Player.class, null);
        Mob target = proxy(Mob.class, player);

        assertSame(player, target.getTarget(), "The fixture must represent ordinary mob aggro");
        assertFalse(ClassPassiveRuntime.targetControlled(player, target));

        try (AutoCloseable ignored = ClassControlAttribution.install(
                (owner, controlled) -> owner == player && controlled == target)) {
            assertTrue(ClassPassiveRuntime.targetControlled(player, target));
        }

        assertFalse(ClassPassiveRuntime.targetControlled(player, target));
    }

    @Test
    void classAbilityArchetypesRemainOrthogonalToShapeAndSummonDelivery() {
        ClassPassiveRuntime.AbilityDamageFacts trap = ClassPassiveRuntime.abilityDamageFacts(
                true, CombatDamageContext.ClassAbilityDamageDomain.AREA_TRAP);
        assertTrue(trap.classAbilityDamage());
        assertTrue(trap.nonSummonClassAbilityDamage());
        assertTrue(trap.areaClassAbilityDamage());
        assertTrue(trap.trapClassAbilityDamage());
        assertFalse(trap.blastClassAbilityDamage());

        ClassPassiveRuntime.AbilityDamageFacts blast = ClassPassiveRuntime.abilityDamageFacts(
                true, CombatDamageContext.ClassAbilityDamageDomain.AREA_BLAST);
        assertTrue(blast.areaClassAbilityDamage());
        assertFalse(blast.trapClassAbilityDamage());
        assertTrue(blast.blastClassAbilityDamage());

        ClassPassiveRuntime.AbilityDamageFacts summon = ClassPassiveRuntime.abilityDamageFacts(
                true, CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_SUMMON);
        assertTrue(summon.classAbilityDamage());
        assertFalse(summon.nonSummonClassAbilityDamage());
        assertFalse(summon.areaClassAbilityDamage());
        assertFalse(summon.trapClassAbilityDamage());
        assertFalse(summon.blastClassAbilityDamage());

        ClassPassiveRuntime.AbilityDamageFacts inactive = ClassPassiveRuntime.abilityDamageFacts(
                false, CombatDamageContext.ClassAbilityDamageDomain.AREA_TRAP);
        assertFalse(inactive.classAbilityDamage());
        assertFalse(inactive.nonSummonClassAbilityDamage());
        assertFalse(inactive.areaClassAbilityDamage());
        assertFalse(inactive.trapClassAbilityDamage());
        assertFalse(inactive.blastClassAbilityDamage());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Player aggroTarget) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (instance, method, arguments) -> switch (method.getName()) {
                    case "getTarget" -> aggroTarget;
                    case "equals" -> instance == arguments[0];
                    case "hashCode" -> System.identityHashCode(instance);
                    case "toString" -> type.getSimpleName() + "Fixture";
                    default -> primitiveDefault(method.getReturnType());
                });
    }

    private static Object primitiveDefault(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        throw new AssertionError("Unknown primitive " + type);
    }
}
