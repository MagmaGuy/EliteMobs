package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable per-viewer copies of party state for PlaceholderAPI, which scoreboard plugins often call off the main
 * thread. The main thread rebuilds it every second from the party sidebar task and after every roster change, so
 * placeholders never touch the party maps or Bukkit entities themselves.
 */
public final class PartyPlaceholderSnapshot {
    private static volatile Map<UUID, PartyView> views = Map.of();

    private PartyPlaceholderSnapshot() {
    }

    /** One member, leader first, with the line the viewer's party sidebar would show. */
    public record MemberView(String line, PartyMemberDisplay.MemberState state) {
    }

    public record PartyView(String leader, List<MemberView> members) {
    }

    /** The viewer's party, or null when they are not in one. Safe from any thread. */
    public static PartyView get(UUID viewerId) {
        return views.get(viewerId);
    }

    /** Main thread only. */
    static void rebuild() {
        Map<UUID, Party> parties = PartyManager.getParties();
        if (parties.isEmpty()) {
            views = Map.of();
            return;
        }
        Map<UUID, PartyView> rebuilt = new HashMap<>();
        for (Party party : parties.values()) {
            List<UUID> memberIds = party.getMembersInDisplayOrder();
            for (UUID viewerId : memberIds) {
                Player viewer = Bukkit.getPlayer(viewerId);
                if (viewer == null) continue;
                DungeonInstance dungeonInstance = PartyMemberDisplay.getDungeonInstance(viewer);
                List<MemberView> members = new ArrayList<>(memberIds.size());
                for (UUID memberId : memberIds) {
                    PartyMemberDisplay.MemberState state =
                            PartyMemberDisplay.state(Bukkit.getPlayer(memberId), dungeonInstance);
                    members.add(new MemberView(
                            PartyMemberDisplay.renderLine(PartyMemberDisplay.template(party, memberId), state), state));
                }
                rebuilt.put(viewerId, new PartyView(members.get(0).state().name(), List.copyOf(members)));
            }
        }
        views = Map.copyOf(rebuilt);
    }

    static void clear() {
        views = Map.of();
    }
}
