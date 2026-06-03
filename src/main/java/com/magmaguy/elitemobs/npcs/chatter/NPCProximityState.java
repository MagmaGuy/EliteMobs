package com.magmaguy.elitemobs.npcs.chatter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class NPCProximityState {

    private final Set<NPCProximityKey> nearbyPairs = new HashSet<>();

    ProximityChanges update(Set<NPCProximityKey> visiblePairs) {
        Set<NPCProximityKey> entered = new HashSet<>();
        for (NPCProximityKey visiblePair : visiblePairs) {
            if (!nearbyPairs.contains(visiblePair)) {
                entered.add(visiblePair);
            }
        }

        Set<NPCProximityKey> left = new HashSet<>();
        Iterator<NPCProximityKey> iterator = nearbyPairs.iterator();
        while (iterator.hasNext()) {
            NPCProximityKey trackedPair = iterator.next();
            if (!visiblePairs.contains(trackedPair)) {
                left.add(trackedPair);
                iterator.remove();
            }
        }
        nearbyPairs.addAll(visiblePairs);

        return new ProximityChanges(entered, left);
    }

    void clear() {
        nearbyPairs.clear();
    }

    record ProximityChanges(Set<NPCProximityKey> entered, Set<NPCProximityKey> left) {
    }
}
