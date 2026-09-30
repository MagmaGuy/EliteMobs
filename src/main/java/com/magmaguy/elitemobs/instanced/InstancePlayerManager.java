package com.magmaguy.elitemobs.instanced;

import com.magmaguy.magmacore.match.AdmissionResult;
import com.magmaguy.magmacore.match.LeaveReason;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * EliteMobs' player lifecycle entry points, kept for existing callers. Admission, leaving,
 * death and spectating run in MagmaCore's match core through {@link MatchInstance}.
 */
public class InstancePlayerManager {

    static void restoreFullHealth(Player player) {
        if (player == null || !player.isOnline() || player.isDead()) return;
        player.setHealth(player.getMaxHealth());
    }

    public static boolean addNewPlayer(Player player, MatchInstance matchInstance) {
        return addNewPlayers(List.of(player), matchInstance);
    }

    /**
     * Atomically admits a set of players. Every check and every cancellable join event runs for
     * the whole group before any state changes, so a party is never split by a full instance,
     * missing permission, another active match, or an API veto.
     */
    public static boolean addNewPlayers(Collection<Player> requestedPlayers, MatchInstance matchInstance) {
        return addNewPlayers(requestedPlayers, matchInstance, () -> true);
    }

    /**
     * Atomically admits players while an external authorization remains valid. The predicate is
     * rechecked after the cancellable join events so they cannot revive a cancelled party launch.
     */
    public static boolean addNewPlayers(Collection<Player> requestedPlayers,
                                        MatchInstance matchInstance,
                                        BooleanSupplier authorization) {
        if (authorization == null) return false;
        return matchInstance.admit(requestedPlayers, authorization) == AdmissionResult.ADMITTED;
    }

    /** Clears a completed admission and balances its public join notifications. */
    public static void rollbackAdmissions(Collection<Player> admittedPlayers, MatchInstance matchInstance) {
        for (Player player : admittedPlayers) matchInstance.leave(player, LeaveReason.QUIT);
    }

    public static void removePlayer(Player player, MatchInstance matchInstance) {
        matchInstance.leave(player, LeaveReason.QUIT);
    }

    public static void playerDeath(MatchInstance matchInstance, Player player) {
        matchInstance.playerDeath(player);
    }

    public static void revivePlayer(MatchInstance matchInstance, Player player, InstanceDeathLocation deathLocation) {
        matchInstance.revivePlayer(player, deathLocation);
    }

    public static void addSpectator(MatchInstance matchInstance, Player player, boolean wasPlayer) {
        matchInstance.addSpectator(player, wasPlayer);
    }

    public static void removeSpectator(MatchInstance matchInstance, Player player) {
        matchInstance.leave(player, LeaveReason.QUIT);
    }

    public static void removeAnyKind(MatchInstance matchInstance, Player player) {
        matchInstance.leave(player, LeaveReason.QUIT);
    }
}
