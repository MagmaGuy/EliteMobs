package com.magmaguy.elitemobs.experimentalcombat;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Server-local, non-database dismissal state for the [Alpha] Advanced Combat System tester notice. */
final class ExperimentalCombatSuggestionState {
    private static final String DISMISSED_PLAYERS = "dismissedPlayers";

    private final Path file;
    private final Set<UUID> dismissedPlayers;

    ExperimentalCombatSuggestionState(Path file) throws IOException {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath().normalize();
        this.dismissedPlayers = read(this.file);
    }

    synchronized boolean isDismissed(UUID playerId) {
        return dismissedPlayers.contains(Objects.requireNonNull(playerId, "playerId"));
    }

    synchronized void dismiss(UUID playerId) throws IOException {
        Objects.requireNonNull(playerId, "playerId");
        if (dismissedPlayers.contains(playerId)) return;
        Set<UUID> updated = new LinkedHashSet<>(dismissedPlayers);
        updated.add(playerId);
        write(updated);
        dismissedPlayers.add(playerId);
    }

    private static Set<UUID> read(Path file) throws IOException {
        if (!Files.exists(file)) return new LinkedHashSet<>();
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(Files.readString(file, StandardCharsets.UTF_8));
        } catch (org.bukkit.configuration.InvalidConfigurationException exception) {
            throw new IOException("Invalid [Alpha] Advanced Combat System suggestion state", exception);
        }
        Set<UUID> result = new LinkedHashSet<>();
        for (String raw : yaml.getStringList(DISMISSED_PLAYERS)) {
            try {
                result.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                // Unknown stale entries do not prevent valid dismissals from loading.
            }
        }
        return result;
    }

    private void write(Set<UUID> updated) throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);
        YamlConfiguration yaml = new YamlConfiguration();
        List<String> serialized = updated.stream().map(UUID::toString).sorted().toList();
        yaml.set(DISMISSED_PLAYERS, serialized);

        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(
                temporary,
                yaml.saveToString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        try {
            Files.move(temporary, file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
