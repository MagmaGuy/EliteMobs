package com.magmaguy.elitemobs.mobconstructor.custombosses;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTaskRegistryTest {

    @Test
    void ownsEveryTaskRoleForEveryPlayer() {
        PlayerTaskRegistry registry = new PlayerTaskRegistry();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();
        AtomicInteger firstLoopCancellations = new AtomicInteger();
        AtomicInteger firstDelayCancellations = new AtomicInteger();
        AtomicInteger secondLoopCancellations = new AtomicInteger();

        registry.replace(firstPlayer, PlayerTaskRegistry.Role.PLAYBACK_LOOP,
                firstLoopCancellations::incrementAndGet);
        registry.replace(firstPlayer, PlayerTaskRegistry.Role.DELAYED_START,
                firstDelayCancellations::incrementAndGet);
        registry.replace(secondPlayer, PlayerTaskRegistry.Role.PLAYBACK_LOOP,
                secondLoopCancellations::incrementAndGet);

        registry.cancelAll();

        assertEquals(1, firstLoopCancellations.get());
        assertEquals(1, firstDelayCancellations.get());
        assertEquals(1, secondLoopCancellations.get());
        assertTrue(registry.isEmpty());
    }

    @Test
    void replacingATaskCancelsOnlyThePreviousTaskInThatSlot() {
        PlayerTaskRegistry registry = new PlayerTaskRegistry();
        UUID player = UUID.randomUUID();
        AtomicInteger oldLoopCancellations = new AtomicInteger();
        AtomicInteger delayedStartCancellations = new AtomicInteger();
        AtomicInteger newLoopCancellations = new AtomicInteger();

        registry.replace(player, PlayerTaskRegistry.Role.PLAYBACK_LOOP,
                oldLoopCancellations::incrementAndGet);
        registry.replace(player, PlayerTaskRegistry.Role.DELAYED_START,
                delayedStartCancellations::incrementAndGet);
        registry.replace(player, PlayerTaskRegistry.Role.PLAYBACK_LOOP,
                newLoopCancellations::incrementAndGet);

        assertEquals(1, oldLoopCancellations.get());
        assertEquals(0, delayedStartCancellations.get());
        assertEquals(0, newLoopCancellations.get());
        assertTrue(registry.contains(player, PlayerTaskRegistry.Role.PLAYBACK_LOOP));
        assertTrue(registry.contains(player, PlayerTaskRegistry.Role.DELAYED_START));

        registry.cancelPlayer(player);

        assertEquals(1, delayedStartCancellations.get());
        assertEquals(1, newLoopCancellations.get());
        assertFalse(registry.contains(player, PlayerTaskRegistry.Role.PLAYBACK_LOOP));
    }
}
