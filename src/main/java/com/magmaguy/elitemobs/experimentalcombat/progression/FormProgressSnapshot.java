package com.magmaguy.elitemobs.experimentalcombat.progression;

import com.magmaguy.elitemobs.skills.SkillType;

import java.util.List;
import java.util.Objects;

/** Derived progression state for one independently persisted class form. */
public record FormProgressSnapshot(
        String formId,
        boolean unlocked,
        List<UnlockBlocker> unlockBlockers,
        long xp,
        int localLevel,
        int effectiveLevel,
        int localCap,
        int effectiveCap,
        long xpAtCap,
        ProgressionCapReason capReason,
        List<SkillType> limitingSkills) {

    public FormProgressSnapshot {
        Objects.requireNonNull(formId, "formId");
        unlockBlockers = List.copyOf(unlockBlockers);
        if (unlocked != unlockBlockers.isEmpty())
            throw new IllegalArgumentException("Unlocked state must match the blocker list");
        Objects.requireNonNull(capReason, "capReason");
        limitingSkills = List.copyOf(limitingSkills);
        if (!unlocked && (xp != 0 || localLevel != 0 || effectiveLevel != 0
                || localCap != 0 || effectiveCap != 0 || xpAtCap != 0
                || capReason != ProgressionCapReason.NOT_APPLICABLE || !limitingSkills.isEmpty()))
            throw new IllegalArgumentException("Locked forms must not expose progression levels or caps");
        if (unlocked && (localLevel < 1 || effectiveLevel < 1
                || localLevel > localCap || effectiveLevel > effectiveCap))
            throw new IllegalArgumentException("Unlocked form level must fit within its visible cap");
        if (capReason == ProgressionCapReason.BAND_COMPLETE && !limitingSkills.isEmpty())
            throw new IllegalArgumentException("A completed band has no limiting foundation skill");
    }
}
