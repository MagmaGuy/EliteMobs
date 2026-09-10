package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.api.PlayerJoinDungeonEvent;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
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
import org.bukkit.event.EventPriority;
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
    private static final Map<UUID, UUID> dungeonParties = new HashMap<>();

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
        dungeonParties.clear();
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
        create(creator, true);
    }

    /** Main-thread integration entry point. False suppresses operation messages and prompts. */
    public static PartyOperationResult create(Player creator, boolean notify) {
        PartyOperationResult validation = validateActor(creator);
        if (!validation.isSuccess()) return finish(creator, validation, notify, "");
        invalidatePendingInvite(creator.getUniqueId());
        if (isInParty(creator.getUniqueId())) {
            return finish(creator, PartyOperationResult.ALREADY_IN_PARTY, notify, "");
        }
        Party party = new Party(creator.getUniqueId());
        parties.put(party.getId(), party);
        partyByPlayer.put(creator.getUniqueId(), party.getId());
        if (notify) send(creator, PartyConfig.getPartyCreatedMessage());
        PartySidebar.refresh(creator);
        return PartyOperationResult.SUCCESS;
    }

    public static void invite(Player inviter, String targetName) {
        invite(inviter, targetName, true);
    }

    /** Uses the ordinary invitation rules, including automatic creation of the inviter's party. */
    public static PartyOperationResult invite(Player inviter, String targetName, boolean notify) {
        PartyOperationResult validation = validateActor(inviter);
        if (!validation.isSuccess()) return finish(inviter, validation, notify, "");
        java.util.Objects.requireNonNull(targetName, "targetName");
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            return finish(inviter, PartyOperationResult.PLAYER_UNAVAILABLE, notify, targetName);
        }
        if (target.getUniqueId().equals(inviter.getUniqueId())) {
            return finish(inviter, PartyOperationResult.SELF_INVITE, notify, targetName);
        }
        if (isInParty(target.getUniqueId())) {
            return finish(inviter, PartyOperationResult.TARGET_ALREADY_IN_PARTY, notify, targetName);
        }
        if (!target.hasPermission("elitemobs.party")) {
            return finish(inviter, PartyOperationResult.TARGET_NO_PERMISSION, notify, target.getName());
        }
        if (hasActivePendingInvite(target.getUniqueId())) {
            return finish(inviter, PartyOperationResult.INVITE_ALREADY_PENDING, notify, target.getName());
        }

        Party party = getParty(inviter.getUniqueId());
        if (party == null) {
            PartyOperationResult created = create(inviter, notify);
            if (!created.isSuccess()) return created;
            party = getParty(inviter.getUniqueId());
        }
        if (party.isFull()) {
            return finish(inviter, PartyOperationResult.PARTY_FULL, notify, targetName);
        }

        pendingInvites.put(target.getUniqueId(), new PendingInvite(
                party.getId(),
                inviter.getUniqueId(),
                System.nanoTime() + PartyConfig.getInviteTimeoutSeconds() * 1_000_000_000L));
        if (notify) {
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
        return PartyOperationResult.SUCCESS;
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
        accept(player, true);
    }

    /** Accepts the current invite; roster changes still invalidate native ready checks. */
    public static PartyOperationResult accept(Player player, boolean notify) {
        PartyOperationResult validation = validateActor(player);
        if (!validation.isSuccess()) return finish(player, validation, notify, "");
        if (isInParty(player.getUniqueId())) {
            invalidatePendingInvite(player.getUniqueId());
            return finish(player, PartyOperationResult.ALREADY_IN_PARTY, notify, "");
        }

        PendingInvite invite = invalidatePendingInvite(player.getUniqueId());
        if (invite == null) {
            return finish(player, PartyOperationResult.NO_PENDING_INVITE, notify, "");
        }
        if (invite.expiresAtNanos() <= System.nanoTime()) {
            return finish(player, PartyOperationResult.INVITE_EXPIRED, notify, "");
        }
        Party party = parties.get(invite.partyId());
        Player inviter = Bukkit.getPlayer(invite.inviterId());
        Party inviterParty = getParty(invite.inviterId());
        if (party == null || inviter == null || !inviter.isOnline()
                || inviterParty == null || !inviterParty.getId().equals(party.getId())) {
            return finish(player, PartyOperationResult.INVITE_EXPIRED, notify, "");
        }
        if (!party.addMember(player.getUniqueId())) {
            if (party.isFull()) invalidatePendingInvites(inviteEntry -> inviteEntry.partyId().equals(party.getId()));
            return finish(player, PartyOperationResult.PARTY_FULL, notify, "");
        }

        if (party.isFull()) invalidatePendingInvites(inviteEntry -> inviteEntry.partyId().equals(party.getId()));

        PartyDungeonReadyCheckManager.cancelForRosterChange(party);
        partyByPlayer.put(player.getUniqueId(), party.getId());
        if (notify) {
            send(player, PartyConfig.getJoinedPartyMessage());
            broadcast(party, PartyConfig.getMemberJoinedMessage().replace("$player", player.getName()), player.getUniqueId());
        }
        refresh(party);
        return PartyOperationResult.SUCCESS;
    }

    public static void leave(Player player) {
        leave(player, true);
    }

    /** Leaving remains possible if parties are disabled or the player's permission was revoked. */
    public static PartyOperationResult leave(Player player, boolean notify) {
        requirePrimaryThread();
        java.util.Objects.requireNonNull(player, "player");
        boolean wasInParty = isInParty(player.getUniqueId());
        leave(player.getUniqueId(), notify, notify);
        return wasInParty ? PartyOperationResult.SUCCESS : PartyOperationResult.NOT_IN_PARTY;
    }

    /** Post-admission only: cancelled or rejected entry attempts never change party membership. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDungeonJoin(PlayerJoinDungeonEvent event) {
        if (!PartyConfig.isEnabled()) return;
        DungeonInstance dungeon = event.getDungeonInstance();
        // Visitors in spectator mode are not members of the dungeon's combat party.
        List<Player> roster = dungeon.getParticipants().stream()
                .filter(member -> !dungeon.isSpectator(member) && member.isOnline())
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        // Solo admission leaves party membership alone; group the dungeon when a second player joins.
        if (roster.size() < 2) return;
        Set<UUID> rosterIds = new HashSet<>();
        roster.forEach(member -> rosterIds.add(member.getUniqueId()));
        Party party = parties.get(dungeonParties.get(dungeon.getRuntimeId()));
        if (party != null && !rosterIds.containsAll(party.getMembers())) party = null;
        if (party == null) {
            // Retain a preformed party if all its members were admitted to this dungeon.
            party = getParty(roster.get(0).getUniqueId());
            if (party == null || !rosterIds.containsAll(party.getMembers())) {
                Player leader = roster.get(0);
                leave(leader.getUniqueId(), false);
                party = new Party(leader.getUniqueId());
                parties.put(party.getId(), party);
                partyByPlayer.put(leader.getUniqueId(), party.getId());
            }
            dungeonParties.put(dungeon.getRuntimeId(), party.getId());
        }
        for (Player member : roster) {
            if (getParty(member.getUniqueId()) == party) continue;
            leave(member.getUniqueId(), false);
            party.addAdmittedDungeonMember(member.getUniqueId());
            partyByPlayer.put(member.getUniqueId(), party.getId());
            send(member, PartyConfig.getJoinedPartyMessage());
        }
        refresh(party);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDungeonRemoved(InstancedDungeonRemoveEvent event) {
        // The party itself remains session-scoped so the group can choose another dungeon.
        dungeonParties.remove(event.getDungeonInstance().getRuntimeId());
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
        leave(playerId, notifyPlayer, true);
    }

    private static void leave(UUID playerId, boolean notifyPlayer, boolean notifyParty) {
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

        if (notifyParty) {
            broadcast(party, PartyConfig.getMemberLeftMessage().replace("$player", playerName), null);
            if (oldLeader.equals(playerId) && party.getLeader() != null)
                broadcast(party, PartyConfig.getLeaderChangedMessage()
                        .replace("$player", playerName(party.getLeader())), null);
        }
        refresh(party);
    }

    private static void requirePrimaryThread() {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("Party operations must run on the server thread");
    }

    private static PartyOperationResult validateActor(Player player) {
        requirePrimaryThread();
        java.util.Objects.requireNonNull(player, "player");
        if (!PartyConfig.isEnabled()) return PartyOperationResult.DISABLED;
        if (!player.isOnline()) return PartyOperationResult.PLAYER_UNAVAILABLE;
        if (!player.hasPermission("elitemobs.party")) return PartyOperationResult.NO_PERMISSION;
        return PartyOperationResult.SUCCESS;
    }

    private static PartyOperationResult finish(Player player, PartyOperationResult result, boolean notify, String target) {
        if (notify) {
            String message = switch (result) {
                case DISABLED -> PartyConfig.getDisabledMessage();
                case NO_PERMISSION -> PartyConfig.getInventoryNoPermissionMessage();
                case PLAYER_UNAVAILABLE -> PartyConfig.getPlayerUnavailableMessage();
                case ALREADY_IN_PARTY -> PartyConfig.getAlreadyInPartyMessage();
                case SELF_INVITE -> PartyConfig.getSelfInviteMessage();
                case TARGET_ALREADY_IN_PARTY -> PartyConfig.getPlayerAlreadyInPartyMessage();
                case TARGET_NO_PERMISSION -> PartyConfig.getPlayerCannotUsePartiesMessage();
                case INVITE_ALREADY_PENDING -> PartyConfig.getInviteAlreadyPendingMessage();
                case PARTY_FULL -> PartyConfig.getPartyFullMessage();
                case NO_PENDING_INVITE -> PartyConfig.getNoPendingInviteMessage();
                case INVITE_EXPIRED -> PartyConfig.getInviteExpiredMessage();
                case NOT_IN_PARTY -> PartyConfig.getNotInPartyMessage();
                case SUCCESS -> throw new IllegalArgumentException("Success feedback belongs to its operation");
            };
            send(player, (message == null ? "" : message).replace("$player", target));
        }
        return result;
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
