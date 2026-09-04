package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.classes.PassiveDefinition;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveCondition;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveMechanics;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveMechanicTrait;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveProfile;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveTrait;
import com.magmaguy.elitemobs.experimentalcombat.progression.FormProgressSnapshot;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProfileSnapshot;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProgressionCapReason;
import com.magmaguy.elitemobs.experimentalcombat.progression.UnlockBlocker;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;

/** Converts authoritative class state into one UI-neutral tree snapshot. */
final class ClassMenuProjector {
    private ClassMenuProjector() {
    }

    static ClassMenuView project(
            ClassCatalog catalog,
            ProfileSnapshot profile,
            ToIntFunction<SkillType> skillLevels) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(skillLevels, "skillLevels");

        String activeFormId = profile.activeLineage() == null
                ? null : profile.activeLineage().activeFormId();
        String selectedFormId = profile.selectedFormId();
        Map<String, ClassMenuView.FormView> forms = new LinkedHashMap<>();
        for (ClassFormDefinition definition : catalog.forms()) {
            forms.put(definition.id(), projectForm(
                    catalog, profile, definition, skillLevels, selectedFormId, activeFormId));
        }

        String activeSummary = activeFormId == null
                ? "None"
                : catalog.require(activeFormId).displayName();
        String selectedSummary = selectedFormId == null
                ? "None"
                : catalog.require(selectedFormId).displayName();
        boolean runLocked = profile.lockedRunSelection() != null;
        String runLockSummary = runLocked
                ? "Locked to " + catalog.require(profile.lockedRunSelection().formId()).displayName()
                + " until this run ends."
                : "Open until the run starts.";

        return new ClassMenuView(
                activeSummary,
                selectedSummary,
                runLocked,
                runLockSummary,
                profile.selectedInputProfile(),
                profile.activeInputProfile(),
                profile.focusSlot(),
                catalog.roots().stream().map(root -> forms.get(root.id())).toList(),
                forms);
    }

    private static ClassMenuView.FormView projectForm(
            ClassCatalog catalog,
            ProfileSnapshot profile,
            ClassFormDefinition form,
            ToIntFunction<SkillType> skillLevels,
            String selectedFormId,
            String activeFormId) {
        FormProgressSnapshot progress = Objects.requireNonNull(
                profile.forms().get(form.id()), "Missing progression for " + form.id());
        ClassLineage lineage = catalog.lineageOf(form.id());
        List<ClassMenuView.FoundationRequirement> requirements = form.foundationSkills().asList().stream()
                .map(skill -> new ClassMenuView.FoundationRequirement(
                        skill.getDisplayName(), skillLevels.applyAsInt(skill), form.requiredFoundationSkillLevel()))
                .toList();
        List<ClassMenuView.BlockerView> blockers = progress.unlockBlockers().stream()
                .map(blocker -> blockerView(catalog, blocker))
                .toList();
        int displayedEffectiveLevel = progress.unlocked()
                ? progress.effectiveLevel()
                : form.band().effectiveStart();
        Map<String, Integer> contributionLevels = lineage.passiveContributionLevels(displayedEffectiveLevel);
        List<ClassMenuView.PassiveView> passives = new ArrayList<>();
        for (ClassFormDefinition lineageForm : lineage.forms()) {
            PassiveDefinition passive = lineageForm.passive();
            int contributionLevel = contributionLevels.get(lineageForm.id());
            passives.add(new ClassMenuView.PassiveView(
                    lineageForm.displayName(),
                    passive.description(),
                    contributionLevel,
                    !progress.unlocked(),
                    passiveStats(lineageForm.id(), contributionLevel)));
        }

        return new ClassMenuView.FormView(
                form.id(),
                form.displayName(),
                form.band(),
                progress.unlocked(),
                form.id().equals(selectedFormId),
                form.id().equals(activeFormId),
                lineage.forms().stream().map(ClassFormDefinition::displayName).toList(),
                form.parentId(),
                catalog.childrenOf(form.id()).stream()
                        .map(child -> {
                            FormProgressSnapshot childProgress = profile.forms().get(child.id());
                            return new ClassMenuView.FormLink(
                                    child.id(), child.displayName(), childProgress.unlocked(),
                                    childProgress.unlocked() ? childProgress.effectiveLevel() : 0);
                        })
                        .toList(),
                progress.localLevel(),
                progress.effectiveLevel(),
                progress.localCap(),
                progress.effectiveCap(),
                xpSummary(form, progress),
                requirements,
                blockers,
                lineage.resourceType().displayName(),
                lineage.resourceType().description(),
                ability(lineage.mobility()),
                ability(form.signature()),
                ability(form.utility()),
                passives);
    }

    private static ClassMenuView.AbilityView ability(AbilityDefinition definition) {
        return new ClassMenuView.AbilityView(definition.displayName(), definition.description());
    }

    private static ClassMenuView.BlockerView blockerView(ClassCatalog catalog, UnlockBlocker blocker) {
        if (blocker.kind() == UnlockBlocker.Kind.CONTENT_REQUIREMENT) {
            return new ClassMenuView.BlockerView(
                    catalog.require(blocker.formId()).displayName(),
                    0,
                    0,
                    null,
                    blocker.reason());
        }
        if (blocker.kind() == UnlockBlocker.Kind.FOUNDATION_SKILL) {
            return new ClassMenuView.BlockerView(
                    blocker.skillType().getDisplayName(),
                    blocker.requiredLevel(),
                    blocker.currentLevel(),
                    null);
        }
        ClassFormDefinition requiredForm = catalog.require(blocker.formId());
        return new ClassMenuView.BlockerView(
                requiredForm.displayName(),
                blocker.requiredLevel(),
                blocker.currentLevel(),
                catalog.lineageOf(requiredForm.id()).resourceType().displayName());
    }

    static List<ClassMenuView.PassiveStat> passiveStats(
            String formId,
            int contributionLevel) {
        PassiveProfile.Contribution always = BuiltInClassContent.passiveRegistry()
                .require(formId)
                .atContributionLevel(contributionLevel);
        List<ConditionalContribution> conditional = new ArrayList<>();
        for (PassiveTrait trait : BuiltInClassContent.passiveRegistry().traits(formId)) {
            PassiveProfile.Contribution contribution = trait.profile()
                    .atContributionLevel(contributionLevel);
            if (trait.unconditional()) always = always.plus(contribution);
            else conditional.add(new ConditionalContribution(
                    conditionSuffix(trait), contribution));
        }
        PassiveMechanics alwaysMechanics = PassiveMechanics.NEUTRAL;
        List<ConditionalMechanics> conditionalMechanics = new ArrayList<>();
        for (PassiveMechanicTrait trait : BuiltInClassContent.passiveRegistry()
                .mechanicTraits(formId)) {
            PassiveMechanics contribution = trait.atContributionLevel(contributionLevel);
            if (trait.unconditional()) alwaysMechanics = alwaysMechanics.combine(contribution);
            else conditionalMechanics.add(new ConditionalMechanics(
                    conditionSuffix(trait.conditions()), contribution));
        }

        List<ClassMenuView.PassiveStat> stats = new ArrayList<>();
        addContribution(stats, always, "");
        addMechanics(stats, alwaysMechanics, "");
        for (ConditionalContribution contribution : conditional)
            addContribution(stats, contribution.contribution(), contribution.suffix());
        for (ConditionalMechanics contribution : conditionalMechanics)
            addMechanics(stats, contribution.mechanics(), contribution.suffix());
        return List.copyOf(stats);
    }

    private static void addContribution(
            List<ClassMenuView.PassiveStat> stats,
            PassiveProfile.Contribution contribution,
            String suffix) {
        addStat(stats, "Damage" + suffix, contribution.outgoingDamage());
        addStat(stats, "Damage taken" + suffix, contribution.incomingDamage());
        addStat(stats, "Speed" + suffix, contribution.movementSpeed());
        addStat(stats, "Healing" + suffix, contribution.healingDone());
        addStat(stats, "Ability cost reduction" + suffix, contribution.abilityCostReduction());
        addStat(stats, "Healing received" + suffix, contribution.healingReceived());
    }

    private static String conditionSuffix(PassiveTrait trait) {
        return conditionSuffix(trait.conditions());
    }

    private static String conditionSuffix(Set<PassiveCondition> conditions) {
        return " " + conditions.stream()
                .map(ClassMenuProjector::conditionText)
                .sorted()
                .reduce((left, right) -> left + " and " + right)
                .orElseThrow();
    }

    private static void addMechanics(
            List<ClassMenuView.PassiveStat> stats,
            PassiveMechanics mechanics,
            String suffix) {
        addMultiplierStat(stats, "Burst healing" + suffix, mechanics.burstHealingMultiplier());
        addMultiplierStat(stats, "Periodic healing" + suffix, mechanics.periodicHealingMultiplier());
        addMultiplierStat(stats, "Healing against groups" + suffix,
                mechanics.groupedEnemyHealingMultiplier());
        addMultiplierStat(stats, "Periodic duration" + suffix, mechanics.periodicDurationMultiplier());
        addMultiplierStat(stats, "Shield strength" + suffix, mechanics.shieldStrengthMultiplier());
        addMultiplierStat(stats, "Damage taken through links" + suffix,
                mechanics.redirectedDamageMultiplier());
        addMultiplierStat(stats, "Control duration" + suffix, mechanics.controlDurationMultiplier());
        addMultiplierStat(stats, "Control power" + suffix, mechanics.controlPotencyMultiplier());
        addStat(stats, "Ability cost reduction" + suffix, 1D - mechanics.abilityCostMultiplier());
        addStat(stats, "Control resistance" + suffix, mechanics.controlResistanceFraction());
        addStat(stats, "Party speed" + suffix, mechanics.partyMovementSpeedAdjustment());
    }

    private static void addMultiplierStat(
            List<ClassMenuView.PassiveStat> stats,
            String label,
            double multiplier) {
        addStat(stats, label, multiplier - 1D);
    }

    private static String conditionText(PassiveCondition condition) {
        return switch (condition) {
            case ALWAYS -> "always";
            case HEALTH_BELOW_75 -> "under 75% HP";
            case HEALTH_BELOW_50 -> "under 50% HP";
            case HEALTH_BELOW_25 -> "under 25% HP";
            case MOVING -> "while moving";
            case STANDING -> "while still";
            case RECENTLY_HIT -> "after being hit";
            case NOT_RECENTLY_HIT -> "while unhit";
            case GROUPED -> "near allies";
            case SOLO -> "while alone";
            case TARGET_WOUNDED -> "against wounded foes";
            case TARGET_HEALTHY -> "against healthy foes";
            case TARGET_BOSS -> "against bosses";
            case TARGET_ORDINARY -> "against ordinary foes";
            case TARGET_CONTROLLED -> "against controlled foes";
            case TARGET_UNCONTROLLED -> "against uncontrolled foes";
            case TARGET_ISOLATED -> "against lone foes";
            case TARGET_GROUPED -> "against groups";
            case CLOSE_RANGE -> "at close range";
            case LONG_RANGE -> "at long range";
            case CRITICAL_HIT -> "on critical hits";
            case RANGED_ATTACK -> "on ranged attacks";
            case CLASS_ABILITY_DAMAGE -> "on ability damage";
            case AREA_CLASS_ABILITY_DAMAGE -> "on area ability damage";
            case TRAP_CLASS_ABILITY_DAMAGE -> "on trap damage";
            case BLAST_CLASS_ABILITY_DAMAGE -> "on blast damage";
            case SPELL_DAMAGE -> "on spell damage";
            case RECENT_ELITE_KILL -> "after an Elite kill";
            case WARD_BROKEN -> "after your ward breaks";
        };
    }

    private record ConditionalContribution(
            String suffix,
            PassiveProfile.Contribution contribution) {
    }

    private record ConditionalMechanics(
            String suffix,
            PassiveMechanics mechanics) {
    }

    private static void addStat(
            List<ClassMenuView.PassiveStat> stats,
            String label,
            double fraction) {
        if (Math.abs(fraction) > 1.0E-9D)
            stats.add(new ClassMenuView.PassiveStat(label, fraction));
    }

    private static String xpSummary(ClassFormDefinition form, FormProgressSnapshot progress) {
        if (!progress.unlocked()) return "Unavailable until this form is unlocked.";
        if (progress.xp() >= progress.xpAtCap()) {
            if (progress.capReason() == ProgressionCapReason.BAND_COMPLETE) {
                return "Capped. Choose a specialization. No XP is earned.";
            }
            String skills = String.join(" and ", progress.limitingSkills().stream()
                    .map(SkillType::getDisplayName).toList());
            return "Capped. Raise " + skills + ". No XP is earned.";
        }
        long curveBaseline = SkillXPCalculator.totalXPForLevel(form.band().effectiveStart());
        long currentLevelStart = SkillXPCalculator.totalXPForLevel(progress.effectiveLevel()) - curveBaseline;
        long nextLevelXp = SkillXPCalculator.xpToNextLevel(progress.effectiveLevel());
        return Math.max(0, progress.xp() - currentLevelStart) + "/" + nextLevelXp + " XP";
    }
}
