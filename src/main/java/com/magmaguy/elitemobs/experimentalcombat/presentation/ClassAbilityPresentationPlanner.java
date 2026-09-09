package com.magmaguy.elitemobs.experimentalcombat.presentation;

import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

/** Selects one bounded audiovisual plan from a class lineage and fixed ability mechanics. */
public final class ClassAbilityPresentationPlanner {
    private static final Map<String, AbilityPresentationTheme> BRANCH_THEMES = Map.ofEntries(
            Map.entry("artillerist", AbilityPresentationTheme.ENGINEERING),
            Map.entry("saboteur", AbilityPresentationTheme.ENGINEERING),
            Map.entry("trapper", AbilityPresentationTheme.ENGINEERING),
            Map.entry("demolitionist", AbilityPresentationTheme.ENGINEERING),
            Map.entry("tempest_archer", AbilityPresentationTheme.STORM),
            Map.entry("shaman", AbilityPresentationTheme.NATURE),
            Map.entry("lifewarden", AbilityPresentationTheme.NATURE),
            Map.entry("grovekeeper", AbilityPresentationTheme.NATURE),
            Map.entry("shepherd", AbilityPresentationTheme.NATURE),
            Map.entry("spiritcaller", AbilityPresentationTheme.SPIRIT),
            Map.entry("mistweaver", AbilityPresentationTheme.SPIRIT),
            Map.entry("soulwarden", AbilityPresentationTheme.SPIRIT),
            Map.entry("pyromancer", AbilityPresentationTheme.FLAME),
            Map.entry("cryomancer", AbilityPresentationTheme.FROST),
            Map.entry("occultist", AbilityPresentationTheme.SHADOW),
            Map.entry("necromancer", AbilityPresentationTheme.SHADOW),
            Map.entry("lich", AbilityPresentationTheme.SHADOW),
            Map.entry("plaguebringer", AbilityPresentationTheme.SHADOW),
            Map.entry("demonologist", AbilityPresentationTheme.SHADOW),
            Map.entry("spiritbinder", AbilityPresentationTheme.SPIRIT));

    private ClassAbilityPresentationPlanner() {
    }

    public static AbilityPresentationPlan plan(ClassLineage lineage, FixedAbilitySpec spec) {
        Objects.requireNonNull(lineage, "lineage");
        Objects.requireNonNull(spec, "spec");
        EnumSet<AbilityPresentationCue> cues = EnumSet.of(AbilityPresentationCue.CAST);
        if (isMobility(spec.family())) cues.add(AbilityPresentationCue.MOBILITY);
        if (spec.effects().contains(AbilityEffect.DAMAGE)) cues.add(AbilityPresentationCue.IMPACT);
        if (spec.effects().contains(AbilityEffect.HEAL)
                || spec.effects().contains(AbilityEffect.LIFESTEAL))
            cues.add(AbilityPresentationCue.HEAL);
        if (spec.effects().stream().anyMatch(ClassAbilityPresentationPlanner::isBuff))
            cues.add(AbilityPresentationCue.BUFF);
        if (spec.effects().stream().anyMatch(ClassAbilityPresentationPlanner::isControl))
            cues.add(AbilityPresentationCue.CONTROL);
        return new AbilityPresentationPlan(theme(lineage), cues, AbilityPresentationBudget.STANDARD);
    }

    private static AbilityPresentationTheme theme(ClassLineage lineage) {
        AbilityPresentationTheme branch = BRANCH_THEMES.get(lineage.activeForm().id());
        if (branch != null) return branch;
        return switch (lineage.resourceType()) {
            case STAMINA, RESOLVE -> AbilityPresentationTheme.VALOR;
            case FURY -> AbilityPresentationTheme.FURY;
            case FOCUS -> AbilityPresentationTheme.HUNT;
            case GRACE -> AbilityPresentationTheme.RADIANT;
            case MANA -> AbilityPresentationTheme.ARCANE;
        };
    }

    private static boolean isMobility(AbilityFamily family) {
        return switch (family) {
            case MOUNTED_CHARGE, BALLISTIC_LEAP, SAFE_DASH, SAFE_BLINK, ALLY_FLIGHT -> true;
            default -> false;
        };
    }

    private static boolean isBuff(AbilityEffect effect) {
        return switch (effect) {
            case SHIELD, CLEANSE, SELF_PROTECT, ALLY_PROTECT, SPEED, STRENGTH,
                    SPELL_STRENGTH, SELF_VULNERABLE -> true;
            default -> false;
        };
    }

    private static boolean isControl(AbilityEffect effect) {
        return switch (effect) {
            case KNOCKBACK, PULL, LAUNCH, SLOW, WEAKEN, GLOW, PARTY_DAMAGE_MARK,
                    TAUNT, INTERRUPT, FEAR, ROOT -> true;
            default -> false;
        };
    }
}
