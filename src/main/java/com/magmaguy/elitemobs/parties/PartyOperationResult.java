package com.magmaguy.elitemobs.parties;

/** Outcome of a synchronous PartyManager operation. No mutable party state is exposed. */
public enum PartyOperationResult {
    SUCCESS,
    DISABLED,
    NO_PERMISSION,
    PLAYER_UNAVAILABLE,
    ALREADY_IN_PARTY,
    SELF_INVITE,
    TARGET_ALREADY_IN_PARTY,
    TARGET_NO_PERMISSION,
    INVITE_ALREADY_PENDING,
    PARTY_FULL,
    NO_PENDING_INVITE,
    INVITE_EXPIRED,
    NOT_IN_PARTY;

    public boolean isSuccess() { return this == SUCCESS; }
}
