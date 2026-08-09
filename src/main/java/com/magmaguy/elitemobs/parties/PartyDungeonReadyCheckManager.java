package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Owns consent and launch reservations for party entry into instanced dungeons.
 * Ready checks are session-only, snapshot an exact roster and deliberately run before
 * any world or match lifecycle event.
 */
public final class PartyDungeonReadyCheckManager {
    private static final Map<UUID, ReadyCheck> checksByParty = new HashMap<>();

    private PartyDungeonReadyCheckManager() {
    }

    /**
     * Requests consent from every member in {@code memberIds}. Solo entry runs immediately.
     * The supplied reservation must be checked at commit boundaries and released when an
     * accepted launch finishes or fails, keeping the party reserved while a world operation
     * is queued or running.
     */
    public static boolean request(Player initiator,
                                  Collection<UUID> memberIds,
                                  String dungeonDescription,
                                  DungeonLaunch launch) {
        List<UUID> roster = List.copyOf(memberIds);
        if (roster.size() <= 1)
            return launch.launch(new LaunchReservation() {
                @Override
                public boolean isValid() {
                    return true;
                }

                @Override
                public void release() {
                }
            });

        Party party = PartyManager.getParty(initiator.getUniqueId());
        if (party == null || !PartyManager.isDungeonEntryRosterCurrent(initiator, roster)) {
            PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyChangedMessage());
            return false;
        }
        if (checksByParty.containsKey(party.getId())) {
            PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonReadyCheckPendingMessage());
            return false;
        }

        UUID token = UUID.randomUUID();
        ReadyCheck check = new ReadyCheck(
                party.getId(),
                token,
                initiator.getUniqueId(),
                roster,
                safeDescription(dungeonDescription),
                launch,
                System.nanoTime() + PartyConfig.getDungeonReadyCheckTimeoutSeconds() * 1_000_000_000L);
        check.readyPlayerIds.add(initiator.getUniqueId());
        checksByParty.put(party.getId(), check);
        check.expirationTask = Bukkit.getScheduler().runTaskLater(
                MetadataHandler.PLUGIN,
                () -> expire(party.getId(), token),
                PartyConfig.getDungeonReadyCheckTimeoutSeconds() * 20L);

        broadcast(check, replaceStatus(PartyConfig.getDungeonReadyCheckStartedMessage(), check, initiator));
        for (UUID memberId : roster) {
            Player member = Bukkit.getPlayer(memberId);
            if (member == null || !member.isOnline()) continue;
            if (memberId.equals(initiator.getUniqueId())) {
                member.spigot().sendMessage(
                        SpigotMessage.simpleMessage(PartyManager.format(
                                PartyConfig.getDungeonReadyCheckCancelPromptMessage())),
                        SpigotMessage.commandHoverMessage(
                                PartyManager.format(PartyConfig.getDungeonReadyCheckCancelButton()),
                                PartyManager.format(PartyConfig.getDungeonReadyCheckDeclineHover()),
                                "/em party decline " + token));
                continue;
            }
            member.spigot().sendMessage(
                    SpigotMessage.simpleMessage(PartyManager.format(PartyConfig.getDungeonReadyCheckPromptMessage()
                            .replace("$dungeon", check.dungeonDescription))),
                    SpigotMessage.commandHoverMessage(
                            PartyManager.format(PartyConfig.getDungeonReadyCheckReadyButton()),
                            PartyManager.format(PartyConfig.getDungeonReadyCheckReadyHover()),
                            "/em party ready " + token),
                    SpigotMessage.commandHoverMessage(
                            PartyManager.format(PartyConfig.getDungeonReadyCheckDeclineButton()),
                            PartyManager.format(PartyConfig.getDungeonReadyCheckDeclineHover()),
                            "/em party decline " + token));
        }
        return true;
    }

    public static void ready(Player player, String rawToken) {
        ReadyCheck check = resolveWaitingCheck(player, rawToken);
        if (check == null) return;
        if (!rosterIsCurrent(check)) {
            cancelForRosterChange(PartyManager.getParty(player.getUniqueId()));
            return;
        }
        if (!check.readyPlayerIds.add(player.getUniqueId())) {
            PartyManager.sendConfiguredMessage(player, PartyConfig.getDungeonReadyCheckAlreadyReadyMessage());
            return;
        }

        broadcast(check, replaceStatus(PartyConfig.getDungeonReadyCheckPlayerReadyMessage(), check, player));
        if (check.readyPlayerIds.containsAll(check.memberIds)) launch(check);
    }

    public static void decline(Player player, String rawToken) {
        ReadyCheck check = resolveWaitingCheck(player, rawToken);
        if (check == null) return;
        remove(check);
        broadcast(check, PartyConfig.getDungeonReadyCheckDeclinedMessage()
                .replace("$player", player.getName())
                .replace("$dungeon", check.dungeonDescription));
    }

    /** Cancels pending consent, or invalidates an in-flight launch while retaining its reservation. */
    static void cancelForRosterChange(Party party) {
        if (party == null) return;
        ReadyCheck check = checksByParty.get(party.getId());
        if (check == null) return;
        // Once cloning has started, retain the reservation until the world operation's
        // terminal callback. Its roster revalidation will reject the changed party, and
        // the retained slot prevents another member from queuing a duplicate clone meanwhile.
        if (check.state == ReadyState.WAITING) remove(check);
        else check.authorized.set(false);
        broadcast(check, PartyConfig.getDungeonPartyChangedMessage());
    }

    static void shutdown() {
        for (ReadyCheck check : List.copyOf(checksByParty.values())) remove(check);
        checksByParty.clear();
    }

    private static ReadyCheck resolveWaitingCheck(Player player, String rawToken) {
        UUID token;
        try {
            token = UUID.fromString(rawToken);
        } catch (IllegalArgumentException | NullPointerException exception) {
            PartyManager.sendConfiguredMessage(player, PartyConfig.getDungeonReadyCheckNoPendingMessage());
            return null;
        }

        Party party = PartyManager.getParty(player.getUniqueId());
        ReadyCheck check = party == null ? null : checksByParty.get(party.getId());
        if (check == null || check.state != ReadyState.WAITING || !check.token.equals(token)
                || !check.memberIdSet.contains(player.getUniqueId())) {
            PartyManager.sendConfiguredMessage(player, PartyConfig.getDungeonReadyCheckNoPendingMessage());
            return null;
        }
        if (check.expiresAtNanos <= System.nanoTime()) {
            expire(check.partyId, check.token);
            return null;
        }
        return check;
    }

    private static boolean rosterIsCurrent(ReadyCheck check) {
        Player initiator = Bukkit.getPlayer(check.initiatorId);
        return initiator != null
                && initiator.isOnline()
                && PartyManager.isDungeonEntryRosterCurrent(initiator, check.memberIds);
    }

    private static void launch(ReadyCheck check) {
        if (checksByParty.get(check.partyId) != check || check.state != ReadyState.WAITING) return;
        if (!rosterIsCurrent(check)) {
            remove(check);
            broadcast(check, PartyConfig.getDungeonPartyChangedMessage());
            return;
        }

        cancelExpiration(check);
        check.state = ReadyState.LAUNCHING;
        broadcast(check, PartyConfig.getDungeonReadyCheckCompleteMessage()
                .replace("$dungeon", check.dungeonDescription));
        LaunchReservation reservation = new LaunchReservation() {
            @Override
            public boolean isValid() {
                return check.state == ReadyState.LAUNCHING && check.authorized.get();
            }

            @Override
            public void release() {
                check.authorized.set(false);
                checksByParty.remove(check.partyId, check);
            }
        };
        try {
            if (!check.launch.launch(reservation)) reservation.release();
        } catch (RuntimeException exception) {
            reservation.release();
            Logger.warn("Failed to launch party dungeon after ready check: " + exception.getMessage());
            throw exception;
        }
    }

    private static void expire(UUID partyId, UUID token) {
        ReadyCheck check = checksByParty.get(partyId);
        if (check == null || check.state != ReadyState.WAITING || !check.token.equals(token)) return;
        remove(check);
        broadcast(check, PartyConfig.getDungeonReadyCheckExpiredMessage()
                .replace("$dungeon", check.dungeonDescription));
    }

    private static void remove(ReadyCheck check) {
        check.authorized.set(false);
        checksByParty.remove(check.partyId, check);
        cancelExpiration(check);
    }

    private static void cancelExpiration(ReadyCheck check) {
        if (check.expirationTask == null) return;
        check.expirationTask.cancel();
        check.expirationTask = null;
    }

    private static String replaceStatus(String message, ReadyCheck check, Player player) {
        return message
                .replace("$player", player.getName())
                .replace("$dungeon", check.dungeonDescription)
                .replace("$ready", String.valueOf(check.readyPlayerIds.size()))
                .replace("$total", String.valueOf(check.memberIds.size()));
    }

    private static void broadcast(ReadyCheck check, String message) {
        for (UUID memberId : check.memberIds) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline())
                PartyManager.sendConfiguredMessage(member, message);
        }
    }

    private static String safeDescription(String description) {
        return description == null || description.isBlank() ? "this dungeon" : description;
    }

    @FunctionalInterface
    public interface DungeonLaunch {
        boolean launch(LaunchReservation reservation);
    }

    public interface LaunchReservation {
        boolean isValid();

        void release();
    }

    private enum ReadyState {
        WAITING,
        LAUNCHING
    }

    private static final class ReadyCheck {
        private final UUID partyId;
        private final UUID token;
        private final UUID initiatorId;
        private final List<UUID> memberIds;
        private final Set<UUID> memberIdSet;
        private final String dungeonDescription;
        private final DungeonLaunch launch;
        private final long expiresAtNanos;
        private final Set<UUID> readyPlayerIds = new HashSet<>();
        private volatile ReadyState state = ReadyState.WAITING;
        private final AtomicBoolean authorized = new AtomicBoolean(true);
        private BukkitTask expirationTask;

        private ReadyCheck(UUID partyId,
                           UUID token,
                           UUID initiatorId,
                           List<UUID> memberIds,
                           String dungeonDescription,
                           DungeonLaunch launch,
                           long expiresAtNanos) {
            this.partyId = partyId;
            this.token = token;
            this.initiatorId = initiatorId;
            this.memberIds = memberIds;
            this.memberIdSet = Set.copyOf(memberIds);
            this.dungeonDescription = dungeonDescription;
            this.launch = launch;
            this.expiresAtNanos = expiresAtNanos;
        }
    }
}
