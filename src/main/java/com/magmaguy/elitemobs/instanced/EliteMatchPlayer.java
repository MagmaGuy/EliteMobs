package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.magmacore.match.MatchPlayer;
import com.magmaguy.magmacore.match.MoveReason;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/** An EliteMobs instance participant. */
final class EliteMatchPlayer extends MatchPlayer {
    private final MatchInstance instance;

    EliteMatchPlayer(Player player, MatchInstance instance) {
        super(player, instance);
        this.instance = instance;
    }

    @Override
    protected boolean teleport(Location destination, MoveReason reason) {
        if (reason != MoveReason.ENTRY) return super.teleport(destination, reason);
        // Entry goes through EliteMobs' own teleport event, which starts dungeon music.
        boolean moved = PlayerTeleportEvent.teleportPlayer(getPlayer(), destination);
        // The lives counter doubles as EliteMobs' "has entered" marker.
        if (moved) instance.playerLives.put(getPlayer(), getLives());
        return moved;
    }
}
