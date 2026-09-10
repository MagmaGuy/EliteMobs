package com.magmaguy.elitemobs.advancedcombat.abilities;

/** Pure rising-edge detector for Guardian Flight cancel inputs. */
record GuardianFlightInputState(boolean jumpHeld, boolean sneakHeld) {

    Transition update(boolean jump, boolean sneak) {
        Intent intent;
        if (jump && !jumpHeld) intent = Intent.FORWARD_CANCEL;
        else if (sneak && !sneakHeld) intent = Intent.UPWARD_CANCEL;
        else intent = Intent.NONE;
        return new Transition(new GuardianFlightInputState(jump, sneak), intent);
    }

    enum Intent {
        NONE,
        FORWARD_CANCEL,
        UPWARD_CANCEL
    }

    record Transition(GuardianFlightInputState next, Intent intent) {
    }
}
