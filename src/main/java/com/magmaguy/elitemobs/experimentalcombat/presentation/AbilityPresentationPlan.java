package com.magmaguy.elitemobs.experimentalcombat.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Pure presentation plan selected from class fantasy and ability mechanics. */
public record AbilityPresentationPlan(
        AbilityPresentationTheme theme,
        Set<AbilityPresentationCue> cues,
        AbilityPresentationBudget budget) {

    public AbilityPresentationPlan {
        theme = Objects.requireNonNull(theme, "theme");
        cues = Set.copyOf(cues);
        budget = Objects.requireNonNull(budget, "budget");
        if (!cues.contains(AbilityPresentationCue.CAST))
            throw new IllegalArgumentException("Every presentation plan needs a cast cue");
    }

    public AbilityPresentationBudgetLedger newBudgetLedger() {
        return new AbilityPresentationBudgetLedger(this);
    }

    /**
     * Returns evenly spaced interior samples at two samples per block. Endpoints are rendered as
     * separate departure and arrival bursts, so this method never returns zero or one.
     */
    public List<Double> traceProgress(double distance) {
        if (!Double.isFinite(distance))
            throw new IllegalArgumentException("Trace distance must be finite");
        if (distance <= 0D) return List.of();
        int count = Math.min(budget.traceSampleLimit(), Math.max(1, (int) Math.ceil(distance * 2D)));
        List<Double> samples = new ArrayList<>(count);
        for (int index = 1; index <= count; index++)
            samples.add(index / (double) (count + 1));
        return List.copyOf(samples);
    }
}
