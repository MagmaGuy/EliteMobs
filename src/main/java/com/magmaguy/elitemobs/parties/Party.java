package com.magmaguy.elitemobs.parties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * One in-memory party. Parties deliberately contain UUIDs instead of Player references so reconnecting or
 * disconnecting players cannot leave stale Bukkit objects behind.
 */
public final class Party {
    public static final int MAX_MEMBERS = 5;

    private final UUID id = UUID.randomUUID();
    private final LinkedHashSet<UUID> members = new LinkedHashSet<>();
    private UUID leader;

    Party(UUID creator) {
        leader = creator;
        members.add(creator);
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public List<UUID> getMembersInDisplayOrder() {
        List<UUID> ordered = new ArrayList<>();
        ordered.add(leader);
        for (UUID member : members)
            if (!member.equals(leader)) ordered.add(member);
        return ordered;
    }

    public boolean isFull() {
        return members.size() >= MAX_MEMBERS;
    }

    boolean addMember(UUID playerId) {
        return !isFull() && members.add(playerId);
    }

    boolean removeMember(UUID playerId) {
        boolean removed = members.remove(playerId);
        if (!removed) return false;
        if (playerId.equals(leader))
            leader = members.stream().findFirst().orElse(null);
        return true;
    }

    boolean isEmpty() {
        return members.isEmpty();
    }
}
