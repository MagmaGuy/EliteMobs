package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.progression.ActiveLineageSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Resolved sum of every inherited passive on the active branch. */
public record PassiveAggregate(
        double outgoingDamageMultiplier,
        double incomingDamageMultiplier,
        double movementSpeedAdjustment,
        double healingDoneMultiplier,
        double abilityCostReductionFraction,
        double healingReceivedMultiplier,
        PassiveMechanics mechanics,
        List<ResolvedTrait> conditionalTraits,
        List<ResolvedMechanicTrait> conditionalMechanicTraits) {

    public static final PassiveAggregate NEUTRAL = new PassiveAggregate(
            1D, 1D, 0D, 1D, 0D, 1D, PassiveMechanics.NEUTRAL, List.of(), List.of());

    public PassiveAggregate {
        Objects.requireNonNull(mechanics, "mechanics");
        conditionalTraits = List.copyOf(conditionalTraits);
        conditionalMechanicTraits = List.copyOf(conditionalMechanicTraits);
    }

    public static PassiveAggregate resolve(
            ClassLineage lineage,
            ActiveLineageSnapshot snapshot,
            FixedPassiveRegistry registry) {
        PassiveProfile.Contribution total = new PassiveProfile.Contribution(0, 0, 0, 0, 0, 0);
        PassiveMechanics mechanics = PassiveMechanics.NEUTRAL;
        List<ResolvedTrait> conditional = new ArrayList<>();
        List<ResolvedMechanicTrait> conditionalMechanics = new ArrayList<>();
        for (String formId : lineage.formIds()) {
            Integer contributionLevel = snapshot.contributionLevels().get(formId);
            if (contributionLevel == null) continue;
            total = total.plus(registry.require(formId).atContributionLevel(contributionLevel));
            for (PassiveTrait trait : registry.traits(formId)) {
                PassiveProfile.Contribution contribution = trait.profile().atContributionLevel(contributionLevel);
                if (trait.unconditional()) total = total.plus(contribution);
                else conditional.add(new ResolvedTrait(trait.conditions(), contribution));
            }
            for (PassiveMechanicTrait trait : registry.mechanicTraits(formId)) {
                PassiveMechanics contribution = trait.atContributionLevel(contributionLevel);
                if (trait.unconditional()) mechanics = mechanics.combine(contribution);
                else conditionalMechanics.add(new ResolvedMechanicTrait(trait.conditions(), contribution));
            }
        }
        return new PassiveAggregate(
                boundedMultiplier(1D + total.outgoingDamage()),
                boundedMultiplier(1D + total.incomingDamage()),
                Math.max(-.5D, Math.min(.5D, total.movementSpeed())),
                boundedMultiplier(1D + total.healingDone()),
                Math.max(0D, Math.min(.5D, total.abilityCostReduction())),
                boundedMultiplier(1D + total.healingReceived()),
                mechanics,
                conditional,
                conditionalMechanics);
    }

    public Evaluation evaluate(PassiveConditionContext context) {
        Objects.requireNonNull(context, "context");
        double outgoing = outgoingDamageMultiplier;
        double incoming = incomingDamageMultiplier;
        double movement = movementSpeedAdjustment;
        double healingDone = healingDoneMultiplier;
        double costReduction = abilityCostReductionFraction;
        double healingReceived = healingReceivedMultiplier;
        PassiveMechanics evaluatedMechanics = mechanics;
        for (ResolvedTrait trait : conditionalTraits) {
            if (!trait.matches(context)) continue;
            PassiveProfile.Contribution contribution = trait.contribution();
            outgoing += contribution.outgoingDamage();
            incoming += contribution.incomingDamage();
            movement += contribution.movementSpeed();
            healingDone += contribution.healingDone();
            costReduction += contribution.abilityCostReduction();
            healingReceived += contribution.healingReceived();
        }
        for (ResolvedMechanicTrait trait : conditionalMechanicTraits)
            if (trait.matches(context)) evaluatedMechanics = evaluatedMechanics.combine(trait.mechanics());
        return new Evaluation(
                boundedMultiplier(outgoing),
                boundedMultiplier(incoming),
                Math.max(-.5D, Math.min(.5D, movement)),
                boundedMultiplier(healingDone),
                Math.max(0D, Math.min(.5D, costReduction)),
                boundedMultiplier(healingReceived),
                evaluatedMechanics);
    }

    public boolean requires(PassiveCondition condition) {
        Objects.requireNonNull(condition, "condition");
        return conditionalTraits.stream().anyMatch(trait -> trait.conditions().contains(condition))
                || conditionalMechanicTraits.stream().anyMatch(trait -> trait.conditions().contains(condition));
    }

    private static double boundedMultiplier(double value) {
        return Math.max(.25D, Math.min(3D, value));
    }

    public record ResolvedTrait(
            Set<PassiveCondition> conditions,
            PassiveProfile.Contribution contribution) {
        public ResolvedTrait {
            conditions = Set.copyOf(conditions);
            Objects.requireNonNull(contribution, "contribution");
        }

        boolean matches(PassiveConditionContext context) {
            return conditions.stream().allMatch(condition -> condition.matches(context));
        }
    }

    public record Evaluation(
            double outgoingDamageMultiplier,
            double incomingDamageMultiplier,
            double movementSpeedAdjustment,
            double healingDoneMultiplier,
            double abilityCostReductionFraction,
            double healingReceivedMultiplier,
            PassiveMechanics mechanics) {
        public Evaluation {
            Objects.requireNonNull(mechanics, "mechanics");
        }
    }

    public record ResolvedMechanicTrait(
            Set<PassiveCondition> conditions,
            PassiveMechanics mechanics) {
        public ResolvedMechanicTrait {
            conditions = Set.copyOf(conditions);
            Objects.requireNonNull(mechanics, "mechanics");
        }

        boolean matches(PassiveConditionContext context) {
            return conditions.stream().allMatch(condition -> condition.matches(context));
        }
    }
}
