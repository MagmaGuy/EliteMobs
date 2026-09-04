package com.magmaguy.elitemobs.skills;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/** Pure text seam for the single packet-backed identity label above a player. */
public final class PlayerIdentityLabelRenderer {

    private static Function<UUID, Optional<String>> classLabelProvider = ignored -> Optional.empty();

    private PlayerIdentityLabelRenderer() {
    }

    public static String render(UUID playerId) {
        return classLabel(playerId)
                .orElseGet(() -> CombatLevelCalculator.getFormattedCombatLevel(playerId));
    }

    public static boolean hasClassLabel(UUID playerId) {
        return classLabel(playerId).isPresent();
    }

    private static Optional<String> classLabel(UUID playerId) {
        return classLabelProvider.apply(playerId).filter(label -> !label.isBlank());
    }

    public static void installClassLabelProvider(Function<UUID, Optional<String>> provider) {
        classLabelProvider = Objects.requireNonNull(provider, "provider");
    }

    public static void clearClassLabelProvider() {
        classLabelProvider = ignored -> Optional.empty();
    }
}
