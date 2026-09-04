package com.magmaguy.elitemobs.experimentalcombat.presentation;

import java.util.Objects;

/** Per-cast budget ledger used by the Bukkit renderer and tested without a server. */
public final class AbilityPresentationBudgetLedger {
    private final AbilityPresentationPlan plan;
    private int remainingParticles;
    private int remainingSounds;
    private int remainingTargetBursts;

    AbilityPresentationBudgetLedger(AbilityPresentationPlan plan) {
        this.plan = Objects.requireNonNull(plan, "plan");
        remainingParticles = plan.budget().particleLimit();
        remainingSounds = plan.budget().soundLimit();
        remainingTargetBursts = plan.budget().targetBurstLimit();
    }

    /**
     * Claims work for one cue. Unsupported cues and exhausted target budgets return no work.
     * Particle and sound grants are independently clamped to their remaining cast budgets.
     */
    public Grant claim(AbilityPresentationCue cue, int desiredParticles, int desiredSounds) {
        Objects.requireNonNull(cue, "cue");
        if (desiredParticles < 0 || desiredSounds < 0)
            throw new IllegalArgumentException("Requested presentation work must not be negative");
        if (!plan.cues().contains(cue)) return Grant.NONE;
        if (cue.targetScoped() && remainingTargetBursts == 0) return Grant.NONE;

        int particles = Math.min(desiredParticles, remainingParticles);
        int sounds = Math.min(desiredSounds, remainingSounds);
        if (particles == 0 && sounds == 0) return Grant.NONE;

        remainingParticles -= particles;
        remainingSounds -= sounds;
        if (cue.targetScoped()) remainingTargetBursts--;
        return new Grant(particles, sounds);
    }

    public record Grant(int particles, int sounds) {
        public static final Grant NONE = new Grant(0, 0);

        public Grant {
            if (particles < 0 || sounds < 0)
                throw new IllegalArgumentException("Granted presentation work must not be negative");
        }

        public boolean granted() {
            return particles > 0 || sounds > 0;
        }
    }
}
