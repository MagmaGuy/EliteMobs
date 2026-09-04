package com.magmaguy.elitemobs.experimentalcombat.classes;

/**
 * Root-class resource shown through the client-side XP bar while Experimental Combat is active.
 */
public enum ClassResourceType {
    RESOLVE("Resolve", "Resolve recovers slowly and grows in frontline combat."),
    FURY("Fury", "Dealing or taking damage grants Fury; it fades out of combat."),
    FOCUS("Focus", "Focus recovers over time; taking damage delays recovery."),
    GRACE("Grace", "Gain Grace over time and from real healing, not overhealing."),
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
