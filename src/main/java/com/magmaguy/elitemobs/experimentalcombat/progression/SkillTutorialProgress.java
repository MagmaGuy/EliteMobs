package com.magmaguy.elitemobs.experimentalcombat.progression;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;

/** Player-wide tutorial milestones; bit assignments are persisted and must remain stable. */
public record SkillTutorialProgress(int usedSkills) {
    public SkillTutorialProgress {
        usedSkills &= 7;
    }

    public boolean complete() { return usedSkills == 7; }

    public boolean hasUsed(AbilitySlot slot) { return (usedSkills & bit(slot)) != 0; }

    public SkillTutorialProgress withUsed(AbilitySlot slot) {
        return new SkillTutorialProgress(usedSkills | bit(slot));
    }

    private static int bit(AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> 1;
            case SIGNATURE -> 2;
            case UTILITY -> 4;
        };
    }
}
