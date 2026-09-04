package com.magmaguy.elitemobs.combatsystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatDamageContextTest {
    @Test
    void classAbilityOriginExistsOnlyInsideItsSynchronousDamageScope() {
        assertFalse(CombatDamageContext.isClassAbilityDamageActive());
        CombatDamageContext.runClassAbilityDamage(() -> {
            assertTrue(CombatDamageContext.isClassAbilityDamageActive());
            assertTrue(CombatDamageContext.isPlayerToEliteBypassActive());
        });
        assertFalse(CombatDamageContext.isClassAbilityDamageActive());
        assertFalse(CombatDamageContext.isPlayerToEliteBypassActive());
    }

    @Test
    void ordinaryCustomAndMagicBypassDoNotMasqueradeAsClassAbilities() {
        CombatDamageContext.runPlayerToEliteBypass(() ->
                assertFalse(CombatDamageContext.isClassAbilityDamageActive()));
    }

    @Test
    void classDamageDomainSupportsAreaPeriodicAndSingleTargetProjectile() {
        CombatDamageContext.ClassAbilityDamageDomain areaPeriodic =
                new CombatDamageContext.ClassAbilityDamageDomain(
                        CombatDamageContext.ClassAbilityTargetShape.AREA,
                        CombatDamageContext.ClassAbilityDelivery.PERIODIC,
                        CombatDamageContext.ClassAbilityArchetype.TRAP);
        CombatDamageContext.ClassAbilityDamageDomain singleProjectile =
                new CombatDamageContext.ClassAbilityDamageDomain(
                        CombatDamageContext.ClassAbilityTargetShape.SINGLE_TARGET,
                        CombatDamageContext.ClassAbilityDelivery.PROJECTILE);

        CombatDamageContext.runClassAbilityDamage(areaPeriodic, () -> assertEquals(
                areaPeriodic, CombatDamageContext.currentClassAbilityDamageDomain().orElseThrow()));
        CombatDamageContext.runClassAbilityDamage(singleProjectile, () -> assertEquals(
                singleProjectile, CombatDamageContext.currentClassAbilityDamageDomain().orElseThrow()));
        assertTrue(CombatDamageContext.currentClassAbilityDamageDomain().isEmpty());
    }

    @Test
    void compatibilityDomainDefaultsToOtherArchetype() {
        CombatDamageContext.ClassAbilityDamageDomain domain =
                new CombatDamageContext.ClassAbilityDamageDomain(
                        CombatDamageContext.ClassAbilityTargetShape.AREA,
                        CombatDamageContext.ClassAbilityDelivery.DIRECT);

        assertEquals(CombatDamageContext.ClassAbilityArchetype.OTHER, domain.archetype());
        assertEquals(CombatDamageContext.ClassAbilityStrikeQuality.NORMAL, domain.strikeQuality());
    }

    @Test
    void guaranteedCriticalIdentitySurvivesTheSynchronousDamageScope() {
        CombatDamageContext.ClassAbilityDamageDomain critical =
                new CombatDamageContext.ClassAbilityDamageDomain(
                        CombatDamageContext.ClassAbilityTargetShape.SINGLE_TARGET,
                        CombatDamageContext.ClassAbilityDelivery.PROJECTILE,
                        CombatDamageContext.ClassAbilityArchetype.OTHER,
                        CombatDamageContext.ClassAbilityStrikeQuality.GUARANTEED_CRITICAL);

        CombatDamageContext.runClassAbilityDamage(critical, () -> assertEquals(
                CombatDamageContext.ClassAbilityStrikeQuality.GUARANTEED_CRITICAL,
                CombatDamageContext.currentClassAbilityDamageDomain().orElseThrow().strikeQuality()));
    }

    @Test
    void nestedClassDamageRestoresTheOuterTypedDomain() {
        CombatDamageContext.ClassAbilityDamageDomain outer =
                CombatDamageContext.ClassAbilityDamageDomain.AREA_PERIODIC;
        CombatDamageContext.ClassAbilityDamageDomain inner =
                CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_PROJECTILE;

        CombatDamageContext.runClassAbilityDamage(outer, () -> {
            CombatDamageContext.runClassAbilityDamage(inner, () -> assertEquals(
                    inner, CombatDamageContext.currentClassAbilityDamageDomain().orElseThrow()));
            assertEquals(outer, CombatDamageContext.currentClassAbilityDamageDomain().orElseThrow());
        });
        assertFalse(CombatDamageContext.isClassAbilityDamageActive());
        assertTrue(CombatDamageContext.currentClassAbilityDamageDomain().isEmpty());
    }
}
