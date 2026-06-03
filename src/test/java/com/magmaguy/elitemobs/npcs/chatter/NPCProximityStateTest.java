package com.magmaguy.elitemobs.npcs.chatter;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NPCProximityStateTest {

    @Test
    void enterOnlyFiresOnceForSameNpcPlayerPair() {
        NPCProximityState state = new NPCProximityState();
        NPCProximityKey pair = new NPCProximityKey(UUID.randomUUID(), UUID.randomUUID());

        NPCProximityState.ProximityChanges firstScan = state.update(Set.of(pair));
        NPCProximityState.ProximityChanges secondScan = state.update(Set.of(pair));

        assertEquals(Set.of(pair), firstScan.entered());
        assertTrue(firstScan.left().isEmpty());
        assertTrue(secondScan.entered().isEmpty());
        assertTrue(secondScan.left().isEmpty());
    }

    @Test
    void leaveFiresWhenPairDisappears() {
        NPCProximityState state = new NPCProximityState();
        NPCProximityKey pair = new NPCProximityKey(UUID.randomUUID(), UUID.randomUUID());

        state.update(Set.of(pair));
        NPCProximityState.ProximityChanges secondScan = state.update(Set.of());

        assertTrue(secondScan.entered().isEmpty());
        assertEquals(Set.of(pair), secondScan.left());
    }

    @Test
    void samePlayerNearDifferentNpcsProducesDistinctPairs() {
        NPCProximityState state = new NPCProximityState();
        UUID playerUuid = UUID.randomUUID();
        NPCProximityKey firstNpcPair = new NPCProximityKey(UUID.randomUUID(), playerUuid);
        NPCProximityKey secondNpcPair = new NPCProximityKey(UUID.randomUUID(), playerUuid);

        state.update(Set.of(firstNpcPair));
        NPCProximityState.ProximityChanges secondScan = state.update(Set.of(firstNpcPair, secondNpcPair));

        assertEquals(Set.of(secondNpcPair), secondScan.entered());
        assertTrue(secondScan.left().isEmpty());
    }
}
