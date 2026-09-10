package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Cast ownership and reference counts for one world's temporary gate blocks. */
public final class AbyssalGateOwnershipLedger {
    private final Map<UUID, Set<GateBlockPosition>> blocksByGate = new LinkedHashMap<>();
    private final Map<GateBlockPosition, Set<UUID>> gatesByBlock = new HashMap<>();

    public void reserve(UUID gateId, Collection<GateBlockPosition> blocks) {
        Objects.requireNonNull(gateId, "gateId");
        Objects.requireNonNull(blocks, "blocks");
        if (blocksByGate.containsKey(gateId)) {
            throw new IllegalStateException("Gate is already reserved: " + gateId);
        }
        LinkedHashSet<GateBlockPosition> unique = new LinkedHashSet<>(blocks);
        if (unique.isEmpty()) throw new IllegalArgumentException("A gate needs at least one block");
        blocksByGate.put(gateId, Set.copyOf(unique));
        for (GateBlockPosition block : unique) {
            gatesByBlock.computeIfAbsent(block, ignored -> new LinkedHashSet<>()).add(gateId);
        }
    }

    /** Returns blocks whose final owner was this gate and may therefore be cleared. */
    public Set<GateBlockPosition> release(UUID gateId) {
        Objects.requireNonNull(gateId, "gateId");
        Set<GateBlockPosition> blocks = blocksByGate.remove(gateId);
        if (blocks == null) return Set.of();
        LinkedHashSet<GateBlockPosition> unowned = new LinkedHashSet<>();
        for (GateBlockPosition block : blocks) {
            Set<UUID> owners = gatesByBlock.get(block);
            if (owners == null || !owners.remove(gateId)) continue;
            if (owners.isEmpty()) {
                gatesByBlock.remove(block);
                unowned.add(block);
            }
        }
        return Set.copyOf(unowned);
    }

    public boolean isOwned(GateBlockPosition position) {
        Set<UUID> owners = gatesByBlock.get(Objects.requireNonNull(position, "position"));
        return owners != null && !owners.isEmpty();
    }

    public boolean isEmpty() {
        return blocksByGate.isEmpty() && gatesByBlock.isEmpty();
    }
}
