package com.magmaguy.elitemobs.experimentalcombat.classes;

/**
 * Root-class resource shown in the Experimental Combat action-bar HUD.
 */
public enum ClassResourceType {
    RESOLVE("Resolve", "Resolve recovers faster near elites and grows in frontline combat."),
    FURY("Fury", "Dealing or taking damage grants Fury; it fades out of combat."),
    FOCUS("Focus", "Focus recovers over time; taking damage delays recovery."),
    GRACE("Grace", "Grace recovers faster near other players and grows from effective healing."),
    MANA("Mana", "Mana recovers steadily in and out of combat.");

    private final String displayName;
    private final String description;

    ClassResourceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }
}
