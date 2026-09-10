package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbilityStateOwnershipTest {

    @Test
    void sourceExitAndRuntimeCloseVisitEveryOwnedStateBucket() {
        Map<AbilityStateOwnership.Bucket, AtomicInteger> sourceClears = counters();
        Map<AbilityStateOwnership.Bucket, AtomicInteger> closeClears = counters();
        AbilityStateOwnership.Builder builder = AbilityStateOwnership.builder();
        for (AbilityStateOwnership.Bucket bucket : AbilityStateOwnership.Bucket.values())
            builder.bind(bucket,
                    ignored -> sourceClears.get(bucket).incrementAndGet(),
                    () -> closeClears.get(bucket).incrementAndGet());

        AbilityStateOwnership ownership = builder.build();
        ownership.clearSource(UUID.randomUUID());
        ownership.clearAll();

        sourceClears.values().forEach(count -> assertEquals(1, count.get()));
        closeClears.values().forEach(count -> assertEquals(1, count.get()));
    }

    @Test
    void constructionFailsClosedWhenANewBucketHasNoCleanupBinding() {
        AbilityStateOwnership.Builder builder = AbilityStateOwnership.builder();
        AbilityStateOwnership.Bucket[] buckets = AbilityStateOwnership.Bucket.values();
        for (int index = 0; index < buckets.length - 1; index++)
            builder.bind(buckets[index], ignored -> {
            }, () -> {
            });

        assertThrows(IllegalStateException.class, builder::build);
    }

    private static Map<AbilityStateOwnership.Bucket, AtomicInteger> counters() {
        Map<AbilityStateOwnership.Bucket, AtomicInteger> counters =
                new EnumMap<>(AbilityStateOwnership.Bucket.class);
        for (AbilityStateOwnership.Bucket bucket : AbilityStateOwnership.Bucket.values())
            counters.put(bucket, new AtomicInteger());
        return counters;
    }
}
