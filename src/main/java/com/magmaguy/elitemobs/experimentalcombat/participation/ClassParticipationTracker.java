package com.magmaguy.elitemobs.experimentalcombat.participation;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.parties.Party;
import com.magmaguy.elitemobs.parties.PartyManager;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Associates non-damage class contributions with the elite encounters they actually supported.
 * Damage remains authoritative in {@link EliteEntity#getDamagers()}; this tracker adds healing,
 * mitigation and threat without pretending those values are damage shares.
 */
public final class ClassParticipationTracker {
    private static final double SUPPORT_ENCOUNTER_RADIUS_SQUARED = 40D * 40D;

    private final Map<UUID, Set<UUID>> participantsByElite = new HashMap<>();

    public void recordThreat(Player player, Collection<EliteEntity> elites) {
        for (EliteEntity elite : elites) record(elite, player.getUniqueId());
    }

    /**
     * Credits support only to nearby encounters involving the caster or one of their party members.
     * Party membership alone cannot fan one local heal out to a boss fight elsewhere in the world.
     */
    public void recordSupport(Player player) {
        Set<UUID> supportedPlayers = partyMembers(player);
        for (EliteEntity elite : EntityTracker.getEliteMobEntities().values()) {
            if (elite == null || elite.getLivingEntity() == null || !elite.getLivingEntity().isValid()) continue;
            if (!elite.getLivingEntity().getWorld().equals(player.getWorld())) continue;
            if (elite.getLivingEntity().getLocation().distanceSquared(player.getLocation())
                    > SUPPORT_ENCOUNTER_RADIUS_SQUARED) continue;
            boolean engaged = elite.getDamagers().keySet().stream()
                    .map(Player::getUniqueId)
                    .anyMatch(supportedPlayers::contains)
                    || elite.getAggro().keySet().stream()
                    .map(Player::getUniqueId)
                    .anyMatch(supportedPlayers::contains);
            if (engaged) record(elite, player.getUniqueId());
        }
    }

    public Set<Player> participants(EliteEntity elite) {
        LinkedHashSet<Player> players = new LinkedHashSet<>(elite.getDamagers().keySet());
        for (UUID playerId : participantsByElite.getOrDefault(elite.getEliteUUID(), Set.of())) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null) players.add(player);
        }
        return Set.copyOf(players);
    }

    public void clear(EliteEntity elite) {
        participantsByElite.remove(elite.getEliteUUID());
    }

    public void discard(UUID playerId) {
        participantsByElite.values().removeIf(players -> {
            players.remove(playerId);
            return players.isEmpty();
        });
    }

    public void clearAll() {
        participantsByElite.clear();
    }

    private void record(EliteEntity elite, UUID playerId) {
        if (elite == null || elite.getLivingEntity() == null) return;
        participantsByElite.computeIfAbsent(elite.getEliteUUID(), ignored -> new HashSet<>()).add(playerId);
    }

    private static Set<UUID> partyMembers(Player player) {
        Party party = PartyManager.getParty(player.getUniqueId());
        return party == null ? Set.of(player.getUniqueId()) : party.getMembers();
    }
}
