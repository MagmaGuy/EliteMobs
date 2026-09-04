package com.magmaguy.elitemobs.experimentalcombat.progression;

import com.magmaguy.elitemobs.skills.SkillType;

import java.util.List;
import java.util.Objects;

/** Complete no-banking outcome for one requested class-XP award. */
public record AwardResult(
        Status status,
        String formId,
        long requestedXp,
        long appliedXp,
        long discardedXp,
        long previousXp,
        long currentXp,
        int previousLocalLevel,
        int currentLocalLevel,
        int currentEffectiveLevel,
        int localCap,
        int effectiveCap,
        ProgressionCapReason capReason,
        List<SkillType> limitingSkills) {

    public enum Status {
        APPLIED,
        PARTIALLY_APPLIED,
        AT_CAP,
        INVALID_AMOUNT,
        NOT_READY,
        NO_SELECTED_FORM,
        FORM_LOCKED
    }

    public AwardResult {
        capReason = Objects.requireNonNull(capReason, "capReason");
        limitingSkills = List.copyOf(limitingSkills);
        if (capReason == ProgressionCapReason.BAND_COMPLETE && !limitingSkills.isEmpty())
            throw new IllegalArgumentException("A completed band has no limiting foundation skill");
        if (capReason == ProgressionCapReason.NOT_APPLICABLE && !limitingSkills.isEmpty())
            throw new IllegalArgumentException("An outcome without a cap cannot have limiting skills");
    }

    /** Compatibility constructor for outcomes that do not have a resolved cap reason. */
    public AwardResult(
            Status status,
            String formId,
            long requestedXp,
            long appliedXp,
            long discardedXp,
            long previousXp,
            long currentXp,
            int previousLocalLevel,
            int currentLocalLevel,
            int currentEffectiveLevel,
            int localCap,
            int effectiveCap,
            List<SkillType> limitingSkills) {
        this(
                status,
                formId,
                requestedXp,
                appliedXp,
                discardedXp,
                previousXp,
                currentXp,
                previousLocalLevel,
                currentLocalLevel,
                currentEffectiveLevel,
                localCap,
                effectiveCap,
                ProgressionCapReason.NOT_APPLICABLE,
                limitingSkills);
    }

    public boolean changed() {
        return appliedXp > 0;
    }
}
