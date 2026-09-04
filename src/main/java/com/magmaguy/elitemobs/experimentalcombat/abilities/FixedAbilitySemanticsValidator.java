package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Fails registry construction when authored tuning has no executable consumer. */
final class FixedAbilitySemanticsValidator {
    private static final Set<AbilityEffect> DISPLACEMENT_EFFECTS = EnumSet.of(
            AbilityEffect.KNOCKBACK, AbilityEffect.PULL, AbilityEffect.LAUNCH);
    private static final Set<AbilityEffect> MODIFIER_EFFECTS = EnumSet.of(
            AbilityEffect.PARTY_DAMAGE_MARK, AbilityEffect.STRENGTH,
            AbilityEffect.SPELL_STRENGTH, AbilityEffect.SELF_VULNERABLE,
            AbilityEffect.ALLY_PROTECT);
    private static final Set<AbilityMechanic> SHIELD_STATE_MECHANICS = EnumSet.of(
            AbilityMechanic.DEATH_GUARD, AbilityMechanic.BARRIER_BREAK_HEAL,
            AbilityMechanic.THREAT_TRIGGERED_PROTECTION);

    private FixedAbilitySemanticsValidator() {
    }

    static void validate(Collection<FixedAbilitySpec> specs) {
        List<FixedAbilitySpec> definitions = List.copyOf(specs);
        List<String> failures = new ArrayList<>(definitions.stream()
                .flatMap(spec -> problems(spec).stream().map(problem -> spec.id() + ": " + problem))
                .toList());
        failures.addAll(healEchoReachabilityProblems(definitions));
        if (!failures.isEmpty()) {
            throw new IllegalStateException("Fixed ability semantic mismatch: "
                    + String.join("; ", failures));
        }
    }

    private static List<String> healEchoReachabilityProblems(
            Collection<FixedAbilitySpec> specs) {
        List<String> failures = new ArrayList<>();
        for (FixedAbilitySpec echo : specs) {
            if (!echo.executionTraits().mechanics().contains(AbilityMechanic.HEAL_ECHO)) continue;
            String formId = formId(echo.id());
            boolean reachable = specs.stream().anyMatch(candidate ->
                    !candidate.id().equals(echo.id())
                            && formId(candidate.id()).equals(formId)
                            && candidate.effects().contains(AbilityEffect.HEAL)
                            && candidate.tuning().healingFraction() > 0D
                            && !candidate.executionTraits().mechanics().contains(AbilityMechanic.HEAL_ECHO)
                            && echo.resourceCost() + candidate.resourceCost() <= 100D);
            if (!reachable) failures.add(echo.id()
                    + ": HEAL_ECHO has no reachable same-form healing ability");
        }
        return List.copyOf(failures);
    }

    private static String formId(String abilityId) {
        int separator = abilityId.lastIndexOf('.');
        return separator < 0 ? abilityId : abilityId.substring(0, separator);
    }

    static List<String> problems(FixedAbilitySpec spec) {
        AbilityTuning tuning = spec.tuning();
        Set<AbilityEffect> effects = spec.effects();
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        List<String> problems = new ArrayList<>();
        if (tuning.damageMultiplier() > 0D && !effects.contains(AbilityEffect.DAMAGE))
            problems.add("damageMultiplier has no DAMAGE effect");
        if (tuning.healingFraction() > 0D && !effects.contains(AbilityEffect.HEAL))
            problems.add("healingFraction has no HEAL effect");
        if (tuning.shieldFraction() > 0D
                && !effects.contains(AbilityEffect.SHIELD)
                && SHIELD_STATE_MECHANICS.stream().noneMatch(mechanics::contains))
            problems.add("shieldFraction has no shield consumer");
        if (tuning.displacement() > 0D
                && spec.family() != AbilityFamily.BALLISTIC_LEAP
                && effects.stream().noneMatch(DISPLACEMENT_EFFECTS::contains))
            problems.add("displacement has no movement or displacement effect");
        if (Double.compare(tuning.modifierMultiplier(), 1D) != 0
                && effects.stream().noneMatch(MODIFIER_EFFECTS::contains)
                && !mechanics.contains(AbilityMechanic.MISSING_HEALTH_SCALING))
            problems.add("modifierMultiplier has no modifier consumer");
        if (effects.contains(AbilityEffect.WEAKEN)
                && Double.compare(tuning.weakenMultiplier(), 1D) == 0)
            problems.add("WEAKEN must author a weakenMultiplier below 1");
        if (!effects.contains(AbilityEffect.WEAKEN)
                && Double.compare(tuning.weakenMultiplier(), 1D) != 0)
            problems.add("weakenMultiplier has no WEAKEN effect");
        return List.copyOf(problems);
    }
}
