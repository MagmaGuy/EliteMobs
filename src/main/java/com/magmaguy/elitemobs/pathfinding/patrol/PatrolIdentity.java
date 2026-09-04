package com.magmaguy.elitemobs.pathfinding.patrol;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

final class PatrolIdentity {
    private PatrolIdentity() {
    }

    static String canonical(String kind, String filename, PatrolOrigin origin) {
        return kind.toLowerCase(Locale.ROOT) + '|'
                + filename.toLowerCase(Locale.ROOT) + '|'
                + origin.identityFragment();
    }

    static String storageKey(String canonicalIdentity) {
        return UUID.nameUUIDFromBytes(canonicalIdentity.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
