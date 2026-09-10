package com.magmaguy.elitemobs.advancedcombat.constructs;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketConstructLayerIndexTest {

    @Test
    void removingTheTopConstructRevealsTheStillActiveLayerBelowIt() {
        PacketConstructLayerIndex<String, String> index = new PacketConstructLayerIndex<>();
        UUID standard = UUID.randomUUID();
        UUID snare = UUID.randomUUID();

        index.replace(standard, Map.of("0,64,0", "banner"));
        index.replace(snare, Map.of("0,64,0", "cobweb"));

        assertEquals(Optional.of("cobweb"), index.visible("0,64,0"));
        assertEquals(List.of(new PacketConstructLayerIndex.VisibilityChange<>(
                        "0,64,0", Optional.of("cobweb"), Optional.of("banner"))),
                index.remove(snare));
        assertEquals(Optional.of("banner"), index.visible("0,64,0"));
    }

    @Test
    void movingAConstructAtomicallyRestoresOldBlocksAndShowsNewBlocks() {
        PacketConstructLayerIndex<String, String> index = new PacketConstructLayerIndex<>();
        UUID aura = UUID.randomUUID();

        index.replace(aura, Map.of("0,64,0", "banner"));

        assertEquals(List.of(
                        new PacketConstructLayerIndex.VisibilityChange<>(
                                "0,64,0", Optional.of("banner"), Optional.empty()),
                        new PacketConstructLayerIndex.VisibilityChange<>(
                                "1,64,0", Optional.empty(), Optional.of("banner"))),
                index.replace(aura, Map.of("1,64,0", "banner")));
        assertEquals(Optional.empty(), index.visible("0,64,0"));
        assertEquals(Optional.of("banner"), index.visible("1,64,0"));
    }

    @Test
    void updatingAnObscuredLayerDoesNotSendAFalseVisibleChange() {
        PacketConstructLayerIndex<String, String> index = new PacketConstructLayerIndex<>();
        UUID lower = UUID.randomUUID();
        UUID upper = UUID.randomUUID();

        index.replace(lower, Map.of("0,64,0", "banner"));
        index.replace(upper, Map.of("0,64,0", "cobweb"));

        assertTrue(index.replace(lower, Map.of("0,64,0", "totem")).isEmpty());
        assertEquals(Optional.of("cobweb"), index.visible("0,64,0"));
    }

    @Test
    void removingUnknownConstructIsIdempotent() {
        PacketConstructLayerIndex<String, String> index = new PacketConstructLayerIndex<>();

        assertTrue(index.remove(UUID.randomUUID()).isEmpty());
        assertTrue(index.isEmpty());
    }
}
