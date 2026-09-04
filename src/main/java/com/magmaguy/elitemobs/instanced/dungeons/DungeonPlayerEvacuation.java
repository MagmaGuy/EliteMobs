package com.magmaguy.elitemobs.instanced.dungeons;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Objects;

final class DungeonPlayerEvacuation {
    private DungeonPlayerEvacuation() {
    }

    static void clearSpectatorTargetIfNeeded(Player player) {
        Objects.requireNonNull(player, "player");
        if (player.getGameMode() == GameMode.SPECTATOR)
            player.setSpectatorTarget(null);
    }
}
