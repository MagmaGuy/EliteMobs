package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimedOutgoingDamageReductionTest {

    @Test
    void appliesAuthoredWeakeningAsALevelScaledPercentage() {
        AtomicLong now = new AtomicLong(1_000L);
        TimedOutgoingDamageReduction reductions = new TimedOutgoingDamageReduction(now::get);
        UUID caster = UUID.randomUUID();
        UUID elite = UUID.randomUUID();

        double multiplier = reductions.register(
                caster, elite, "justicar.utility", .85D, 1, 80);

        assertEquals(.849625D, multiplier, 1.0E-9D);
        TimedOutgoingDamageReduction.Application largeHit = reductions.apply(
                elite, 100D, true, ignored -> true).orElseThrow();
        TimedOutgoingDamageReduction.Application smallHit = reductions.apply(
                elite, 8D, true, ignored -> true).orElseThrow();
        assertEquals(84.9625D, largeHit.modifiedDamage(), 1.0E-9D);
        assertEquals(15.0375D, largeHit.reducedDamage(), 1.0E-9D);
        assertEquals(6.797D, smallHit.modifiedDamage(), 1.0E-9D);
        assertEquals(1.203D, smallHit.reducedDamage(), 1.0E-9D);
    }

    @Test
    void choosesTheStrongestLiveReductionWithoutStacking() {
        AtomicLong now = new AtomicLong(1_000L);
        TimedOutgoingDamageReduction reductions = new TimedOutgoingDamageReduction(now::get);
        UUID weakerCaster = UUID.randomUUID();
        UUID strongerCaster = UUID.randomUUID();
        UUID elite = UUID.randomUUID();
        reductions.register(weakerCaster, elite, "minor.weaken", .90D, 1, 80);
        reductions.register(strongerCaster, elite, "major.weaken", .70D, 1, 80);

        TimedOutgoingDamageReduction.Application application = reductions.apply(
                elite, 100D, true, ignored -> true).orElseThrow();

        assertEquals(strongerCaster, application.sourceId());
        assertEquals("major.weaken", application.abilityId());
        assertEquals(69.925D, application.modifiedDamage(), 1.0E-9D);
    }

    @Test
    void sourceTargetAndModuleCleanupRemoveOnlyTheirOwnedLeases() {
        AtomicLong now = new AtomicLong(1_000L);
        TimedOutgoingDamageReduction reductions = new TimedOutgoingDamageReduction(now::get);
        UUID firstCaster = UUID.randomUUID();
        UUID secondCaster = UUID.randomUUID();
        UUID firstElite = UUID.randomUUID();
        UUID secondElite = UUID.randomUUID();
        reductions.register(firstCaster, firstElite, "first", .80D, 1, 80);
        reductions.register(firstCaster, secondElite, "first", .80D, 1, 80);
        reductions.register(secondCaster, firstElite, "second", .90D, 1, 80);

        reductions.clearSource(firstCaster);

        assertEquals(secondCaster, reductions.apply(firstElite, 10D, true, ignored -> true)
                .orElseThrow().sourceId());
        assertTrue(reductions.apply(secondElite, 10D, true, ignored -> true).isEmpty());

        reductions.clearTarget(firstElite);
        assertTrue(reductions.apply(firstElite, 10D, true, ignored -> true).isEmpty());

        reductions.register(secondCaster, secondElite, "second", .90D, 1, 80);
        reductions.close();
        assertTrue(reductions.apply(secondElite, 10D, true, ignored -> true).isEmpty());
    }

    @Test
    void prunesExpiredAndInactiveSources() {
        AtomicLong now = new AtomicLong(1_000L);
        TimedOutgoingDamageReduction reductions = new TimedOutgoingDamageReduction(now::get);
        UUID caster = UUID.randomUUID();
        UUID expiredElite = UUID.randomUUID();
        UUID abandonedElite = UUID.randomUUID();
        reductions.register(caster, expiredElite, "expired", .85D, 1, 2);
        reductions.register(caster, abandonedElite, "abandoned", .85D, 1, 80);

        now.addAndGet(100_000_000L);

        assertTrue(reductions.apply(expiredElite, 10D, true, ignored -> true).isEmpty());
        assertTrue(reductions.apply(abandonedElite, 10D, true, ignored -> false).isEmpty());
        assertTrue(reductions.apply(abandonedElite, 10D, true, ignored -> true).isEmpty());
    }

    @Test
    void controlPotencyScalesTheAuthoredReductionBeforeClassLevel() {
        TimedOutgoingDamageReduction reductions = new TimedOutgoingDamageReduction(() -> 1_000L);
        UUID caster = UUID.randomUUID();
        UUID elite = UUID.randomUUID();

        double multiplier = reductions.register(
                caster, elite, "potent", .80D, 100, 1.5D, 80);

        assertEquals(.625D, multiplier, 1.0E-9D);
    }

    @Test
    void doesNotLeakAnEligibleCastersDebuffOntoAnIneligibleVictim() {
        TimedOutgoingDamageReduction reductions = new TimedOutgoingDamageReduction(() -> 1_000L);
        UUID caster = UUID.randomUUID();
        UUID elite = UUID.randomUUID();
        reductions.register(caster, elite, "outside-world.weaken", .80D, 30, 80);

        assertTrue(reductions.apply(elite, 100D, false, ignored -> true).isEmpty());
        assertTrue(reductions.apply(elite, 100D, true, ignored -> true).isPresent(),
                "Skipping an ineligible victim must not consume the debuff affecting the Elite");
    }
}
