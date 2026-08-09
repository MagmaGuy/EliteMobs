package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Owns all session-only party state. Nothing in this manager is serialized; quitting immediately removes a member.
 */
public final class PartyManager implements Listener {
    private static final Map<UUID, Party> parties = new HashMap<>();
    private static final Map<UUID, UUID> partyByPlayer = new HashMap<>();
    private static final Map<UUID, PendingInvite> pendingInvites = new HashMap<>();

    public static void initialize() {
        shutdown();
        if (PartyConfig.isEnabled()) PartySidebar.initialize();
    }

    public static void shutdown() {
        List<Player> onlineMembers = partyByPlayer.keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(java.util.Objects::nonNull)
                .toList();
        PartyDungeonReadyCheckManager.shutdown();
        PartyInteractionHint.shutdown();
        PartyInventoryMenu.shutdown();
        parties.clear();
        partyByPlayer.clear();
        pendingInvites.clear();
        onlineMembers.forEach(PartySidebar::clearPlayer);
        PartySidebar.shutdown();
    }

    public static Map<UUID, Party> getParties() {
        return Collections.unmodifiableMap(parties);
    }

    public static Party getParty(UUID playerId) {
        UUID partyId = partyByPlayer.get(playerId);
        return partyId == null ? null : parties.get(partyId);
    }

    public static boolean isInParty(UUID playerId) {
        return getParty(playerId) != null;
    }

    /** Returns the currently online players this owner can meaningfully invite. */
    public static List<Player> getInvitablePlayers(Player owner) {
        Party ownerParty = getParty(owner.getUniqueId());
        if (ownerParty != null && ownerParty.isFull()) return List.of();

        List<Player> targets = new ArrayList<>();
        for (Player candidate : Bukkit.getOnlinePlayers()) {
            if (candidate.equals(owner)
                    || candidate.hasMetadata("NPC")
                    || !candidate.hasPermission("elitemobs.party")
                    || isInParty(candidate.getUniqueId())
                    || hasActivePendingInvite(candidate.getUniqueId())) continue;
            targets.add(candidate);
        }
        targets.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(targets);
    }

    /**
     * Freezes the party roster used for a dungeon-entry attempt. The initiating player is first so
     * launch ownership and feedback remain deterministic even when they are not the party leader.
     */
    public static List<UUID> getDungeonEntryMemberIds(Player initiator) {
        UUID initiatorId = initiator.getUniqueId();
        Party party = getParty(initiatorId);
        if (!PartyConfig.isEnabled() || party == null) return List.of(initiatorId);

        List<UUID> memberIds = new ArrayList<>(party.getMembersInDisplayOrder());
        memberIds.remove(initiatorId);
        memberIds.add(0, initiatorId);
        return List.copyOf(memberIds);
    }

    /** Ensures nobody left or switched parties while an instanced world was being prepared. */
    public static boolean isDungeonEntryRosterCurrent(Player initiator, Collection<UUID> memberIds) {
        Set<UUID> snapshot = new HashSet<>(memberIds);
        if (snapshot.size() != memberIds.size() || !snapshot.contains(initiator.getUniqueId())) return false;
        if (!PartyConfig.isEnabled()) return snapshot.equals(Set.of(initiator.getUniqueId()));

        Party party = getParty(initiator.getUniqueId());
        if (party == null) return snapshot.equals(Set.of(initiator.getUniqueId()));
        return party.getMembers().equals(snapshot)
                && snapshot.stream().allMatch(memberId -> getParty(memberId) == party);
    }

    public static void create(Player creator) {
        if (!requireEnabled(creator)) return;
        invalidatePendingInvite(creator.getUniqueId());
        if (isInParty(creator.getUniqueId())) {
            send(creator, PartyConfig.getAlreadyInPartyMessage());
            return;
        }
        Party party = new Party(creator.getUniqueId());
        parties.put(party.getId(), party);
        partyByPlayer.put(creator.getUniqueId(), party.getId());
        send(creator, PartyConfig.getPartyCreatedMessage());
        PartySidebar.refresh(creator);
    }

    public static void invite(Player inviter, String targetName) {
        if (!requireEnabled(inviter)) return;
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            send(inviter, PartyConfig.getPlayerUnavailableMessage());
            return;
        }
        if (target.getUniqueId().equals(inviter.getUniqueId())) {
            send(inviter, PartyConfig.getSelfInviteMessage());
            return;
        }
        if (isInParty(target.getUniqueId())) {
            send(inviter, PartyConfig.getPlayerAlreadyInPartyMessage());
            return;
        }
        if (!target.hasPermission("elitemobs.party")) {
            send(inviter, PartyConfig.getPlayerCannotUsePartiesMessage().replace("$player", target.getName()));
            return;
        }
        if (hasActivePendingInvite(target.getUniqueId())) {
            send(inviter, PartyConfig.getInviteAlreadyPendingMessage().replace("$player", target.getName()));
            return;
        }

        Party party = getParty(inviter.getUniqueId());
        if (party == null) {
            create(inviter);
            party = getParty(inviter.getUniqueId());
            if (party == null) return;
        }
        if (party.isFull()) {
            send(inviter, PartyConfig.getPartyFullMessage());
            return;
        }

        pendingInvites.put(target.getUniqueId(), new PendingInvite(
                party.getId(),
                inviter.getUniqueId(),
                System.nanoTime() + PartyConfig.getInviteTimeoutSeconds() * 1_000_000_000L));
        send(inviter, PartyConfig.getInviteSentMessage().replace("$player", target.getName()));
        target.spigot().sendMessage(
                SpigotMessage.simpleMessage(format(PartyConfig.getInviteReceivedMessage()
                        .replace("$player", inviter.getName()))),
                SpigotMessage.commandHoverMessage(
                        format(PartyConfig.getInviteAcceptButton()),
                        format(PartyConfig.getInviteAcceptHover()),
                        "/em party accept"));
        if (PartyInventoryMenu.usesInventoryFallback(target))
            PartyInventoryMenu.openInvitePrompt(target, inviter);
    }

    static void ignoreInvite(Player player) {
        invalidatePendingInvite(player.getUniqueId());
    }

    static boolean openPendingInviteInventory(Player player) {
        PendingInvite invite = pendingInvites.get(player.getUniqueId());
        if (!isPendingInviteValid(player.getUniqueId(), invite)) {
            invalidatePendingInvite(player.getUniqueId());
            return false;
        }
        Player inviter = Bukkit.getPlayer(invite.inviterId());
        PartyInventoryMenu.openInvitePrompt(player, inviter);
        return true;
    }

    static boolean hasActivePendingInvite(UUID playerId) {
        PendingInvite invite = pendingInvites.get(playerId);
        if (isPendingInviteValid(playerId, invite)) return true;
        if (invite != null) invalidatePendingInvite(playerId);
        return false;
    }

    public static void accept(Player player) {
        if (!requireEnabled(player)) return;
        if (isInParty(player.getUniqueId())) {
            invalidatePendingInvite(player.getUniqueId());
            send(player, PartyConfig.getAlreadyInPartyMessage());
            return;
        }

        PendingInvite invite = invalidatePendingInvite(player.getUniqueId());
        if (invite == null) {
            send(player, PartyConfig.getNoPendingInviteMessage());
            return;
        }
        if (invite.expiresAtNanos() <= System.nanoTime()) {
            send(player, PartyConfig.getInviteExpiredMessage());
            return;
        }
        Party party = parties.get(invite.partyId());
        Player inviter = Bukkit.getPlayer(invite.inviterId());
        Party inviterParty = getParty(invite.inviterId());
        if (party == null || inviter == null || !inviter.isOnline()
                || inviterParty == null || !inviterParty.getId().equals(party.getId())) {
            send(player, PartyConfig.getInviteExpiredMessage());
            return;
        }
        if (!party.addMember(player.getUniqueId())) {
            if (party.isFull()) invalidatePendingInvites(inviteEntry -> inviteEntry.partyId().equals(party.getId()));
            send(player, PartyConfig.getPartyFullMessage());
            return;
        }

        if (party.isFull()) invalidatePendingInvites(inviteEntry -> inviteEntry.partyId().equals(party.getId()));

        PartyDungeonReadyCheckManager.cancelForRosterChange(party);
        partyByPlayer.put(player.getUniqueId(), party.getId());
        send(player, PartyConfig.getJoinedPartyMessage());
        broadcast(party, PartyConfig.getMemberJoinedMessage().replace("$player", player.getName()), player.getUniqueId());
        refresh(party);
    }

    public static void leave(Player player) {
        leave(player.getUniqueId(), true);
    }

    public static List<Player> getNearbyMembers(Player partyMember, Location location) {
        Party party = getParty(partyMember.getUniqueId());
        return party == null ? List.of() : getNearbyMembers(party, location);
    }

    public static List<Player> getNearbyMembers(Party party, Location location) {
        if (!PartyConfig.isEnabled() || party == null || location == null || location.getWorld() == null)
            return List.of();
        double maxDistanceSquared = PartyConfig.getSharedProgressRange() * PartyConfig.getSharedProgressRange();
        List<Player> players = new ArrayList<>();
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member == null || !member.isOnline() || !member.isValid()) continue;
            if (!member.getWorld().equals(location.getWorld())) continue;
            if (member.getLocation().distanceSquared(location) > maxDistanceSquared) continue;
            if (!PlayerData.isInMemory(memberId)) continue;
            MatchInstance matchInstance = PlayerData.getMatchInstance(member);
            if (matchInstance != null
                    && matchInstance.getState() != MatchInstance.InstancedRegionState.WAITING
                    && matchInstance.getState() != MatchInstance.InstancedRegionState.STARTING
                    && !matchInstance.getStartingParticipantIds().contains(memberId)) continue;
            players.add(member);
        }
        return players;
    }

    /**
     * Expands actual combat contributors to nearby party members while preserving one credit per UUID.
     */
    public static List<Player> expandSharedCreditParticipants(Collection<Player> contributors, EliteEntity eliteEntity) {
        return expandSharedCreditParticipants(contributors, eliteEntity == null ? null : eliteEntity.getLocation());
    }

    public static List<Player> expandSharedCreditParticipants(Collection<Player> contributors, Location creditLocation) {
        LinkedHashMap<UUID, Player> eligible = new LinkedHashMap<>();
        for (Player contributor : contributors) {
            if (contributor == null || contributor.hasMetadata("NPC")) continue;
            eligible.put(contributor.getUniqueId(), contributor);
            Party party = getParty(contributor.getUniqueId());
            if (party == null) continue;
            for (Player partyMember : getNearbyMembers(party, creditLocation))
                eligible.put(partyMember.getUniqueId(), partyMember);
        }
        return List.copyOf(eligible.values());
    }

    public static boolean shouldUsePartyLoot(Player player, EliteEntity eliteEntity) {
        if (!PartyConfig.isEnabled() || player == null || eliteEntity == null) return false;
        Party party = getParty(player.getUniqueId());
        return party != null && getNearbyMembers(party, eliteEntity.getLocation()).size() > 1;
    }

    private static void leave(UUID playerId, boolean notifyPlayer) {
        invalidatePendingInvite(playerId);
        invalidatePendingInvites(invite -> invite.inviterId().equals(playerId));
        Party party = getParty(playerId);
        if (party == null) {
            if (notifyPlayer) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) send(player, PartyConfig.getNotInPartyMessage());
            }
            return;
        }

        UUID oldLeader = party.getLeader();
        String playerName = playerName(playerId);
        PartyDungeonReadyCheckManager.cancelForRosterChange(party);
        SharedLootTable.onPartyMemberLeave(party.getId(), playerId);
        party.removeMember(playerId);
        partyByPlayer.remove(playerId);
        Player leavingPlayer = Bukkit.getPlayer(playerId);
        if (leavingPlayer != null) {
            if (notifyPlayer) send(leavingPlayer, PartyConfig.getLeftPartyMessage());
            PartySidebar.clearPlayer(leavingPlayer);
        }

        if (party.isEmpty()) {
            parties.remove(party.getId());
            invalidatePendingInvites(invite -> invite.partyId().equals(party.getId()));
            return;
        }

        broadcast(party, PartyConfig.getMemberLeftMessage().replace("$player", playerName), null);
        if (oldLeader.equals(playerId) && party.getLeader() != null)
            broadcast(party, PartyConfig.getLeaderChangedMessage()
                    .replace("$player", playerName(party.getLeader())), null);
        refresh(party);
    }

    private static boolean requireEnabled(Player player) {
        if (PartyConfig.isEnabled()) return true;
        send(player, PartyConfig.getDisabledMessage());
        return false;
    }

    private static void refresh(Party party) {
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) PartySidebar.refresh(member);
        }
    }

    private static void broadcast(Party party, String message, UUID excludedPlayer) {
        for (UUID memberId : party.getMembers()) {
            if (memberId.equals(excludedPlayer)) continue;
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) send(member, message);
        }
    }

    private static void send(Player player, String message) {
        Logger.sendSimpleMessage(player, format(message));
    }

    public static void sendConfiguredMessage(Player player, String message) {
        if (player != null) send(player, message);
    }

    static String format(String message) {
        return (message == null ? "" : message).replace("$prefix", PartyConfig.getPrefix());
    }

    private static String playerName(UUID playerId) {
        String name = Bukkit.getOfflinePlayer(playerId).getName();
        return name == null ? PartyConfig.getUnknownPlayerName() : name;
    }

    static void cleanupExpiredInvites() {
        long now = System.nanoTime();
        invalidatePendingInvites(invite -> invite.expiresAtNanos() <= now);
    }

    private static boolean isPendingInviteValid(UUID invitedPlayerId, PendingInvite invite) {
        if (invite == null || invite.expiresAtNanos() <= System.nanoTime() || isInParty(invitedPlayerId))
            return false;
        Party invitedParty = parties.get(invite.partyId());
        if (invitedParty == null || invitedParty.isFull()) return false;
        Player inviter = Bukkit.getPlayer(invite.inviterId());
        Party inviterParty = getParty(invite.inviterId());
        return inviter != null && inviter.isOnline()
                && inviterParty != null && inviterParty.getId().equals(invitedParty.getId());
    }

    private static PendingInvite invalidatePendingInvite(UUID invitedPlayerId) {
        PendingInvite removed = pendingInvites.remove(invitedPlayerId);
        PartyInventoryMenu.closeInvitePrompt(invitedPlayerId);
        return removed;
    }

    private static void invalidatePendingInvites(Predicate<PendingInvite> predicate) {
        List<UUID> invitedPlayerIds = pendingInvites.entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();
        invitedPlayerIds.forEach(PartyManager::invalidatePendingInvite);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        leave(event.getPlayer().getUniqueId(), false);
    }

    private record PendingInvite(UUID partyId, UUID inviterId, long expiresAtNanos) {
    }
}
