package com.magmaguy.elitemobs.experimentalcombat.progression;

import com.magmaguy.elitemobs.skills.SkillType;

import java.util.List;

/** Result of the permission-gated tester/admin class progression fixture. */
public record ClassProgressionSetResult(
        Status status,
        String formId,
        int requestedEffectiveLevel,
        String blockingFormId,
        int effectiveCap,
        List<SkillType> limitingSkills,
        ProfileSnapshot snapshot) {

    public ClassProgressionSetResult {
        limitingSkills = List.copyOf(limitingSkills);
    }

    public boolean applied() {
        return status == Status.APPLIED;
    }

    public enum Status {
        APPLIED,
        NOT_READY,
        UNKNOWN_FORM,
        LEVEL_OUTSIDE_FORM_BAND,
        FOUNDATION_SKILL_CAP,
        RUN_LOCKED
    }
}
