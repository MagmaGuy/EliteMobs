package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassMenuPresenterTest {
    private static final int MAX_DESCRIPTION_ROW_LENGTH = 68;
    private static final Pattern MINI_MESSAGE_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern LEGACY_HEX = Pattern.compile(
            "(?i)(?:[&§]x(?:[&§][0-9a-f]){6}|[&§]#[0-9a-f]{6})");
    private static final Pattern LEGACY_FORMAT = Pattern.compile("(?i)[&§][0-9a-fk-or]");

    @Test
    void detailPagesHaveOneAllClassesActionAndControlsExistOnlyOnTheOverview() {
        ClassMenuView view = catalogView(true);
        ClassMenuPresentation overview = ClassMenuPresenter.overview(view);

        assertEquals(1, countActions(overview, ClassMenuAction.OpenControls.class));
        assertEquals(1, countLabelsContaining(overview, "Ability Controls"));
        for (ClassMenuView.FormView form : view.forms().values()) {
            ClassMenuPresentation page = ClassMenuPresenter.form(view, form);
            assertEquals(0, countActions(page, ClassMenuAction.OpenControls.class));
            assertEquals(1, countActions(page, ClassMenuAction.OpenOverview.class));
            assertEquals(1, countLabelsContaining(page, "All Classes"));
        }
    }

    @Test
    void controlsUseTheFAbilityLayerAndNameTheActiveAbilities() {
        ClassMenuPresentation controls = ClassMenuPresenter.controls(
                withActiveForm(catalogView(true), "berserker"));
        String copy = visibleText(String.join("\n", playerFacingCopy(controls)));

        assertTrue(copy.contains("F then 1 • Crater Leap"));
        assertTrue(copy.contains("F then 2 • Rampage"));
        assertTrue(copy.contains("F then 3 • War Cry"));
        assertFalse(copy.contains("Double F"));
        assertFalse(copy.contains("F then right-click"));
        assertFalse(copy.contains("Shift+F"));
        assertFalse(copy.contains("Selected"));
        assertFalse(copy.contains("next run"));
        assertFalse(copy.contains("Hotbar Layer"));
        assertTrue(copy.contains("Press F, then choose ability 1, 2 or 3."));
    }

    @Test
    void everyLockedClassButtonIsRedAndExplainsItsBlockers() {
        ClassMenuView view = catalogView(false);
        int lockedActions = 0;

        for (ClassMenuPresentation page : pages(view)) {
            lockedActions += assertLockedActionsExplainBlockers(page, view);
        }
        assertTrue(lockedActions > 0, "The fixture must expose locked class actions");
    }

    @Test
    void everyRenderedRowFitsTheSingleLineBudget() {
        Set<String> overlongRows = new LinkedHashSet<>();
        for (ClassMenuView view : List.of(catalogView(true), catalogView(false))) {
            for (ClassMenuPresentation page : pages(view)) {
                for (String row : playerFacingCopy(page)) {
                    String visible = visibleText(row);
                    if (visible.length() > MAX_DESCRIPTION_ROW_LENGTH) {
                        overlongRows.add(visible.length() + " characters: " + visible);
                    }
                }
            }
        }
        assertTrue(overlongRows.isEmpty(), () -> "Description rows exceed "
                + MAX_DESCRIPTION_ROW_LENGTH + " visible characters:\n"
                + String.join("\n", overlongRows));
    }

    @Test
    void passiveRowsShowCurrentLevelTypedMechanics() {
        List<ClassMenuView.PassiveStat> levelOne = ClassMenuProjector.passiveStats("aegis", 1);
        List<ClassMenuView.PassiveStat> levelThirty = ClassMenuProjector.passiveStats("aegis", 30);

        assertEquals(.1065D, stat(levelOne, "Shield strength"), 1.0E-9D);
        assertEquals(.15D, stat(levelThirty, "Shield strength"), 1.0E-9D);
        assertEquals(-.0852D, stat(levelOne, "Damage taken through links"), 1.0E-9D);
        assertEquals(-.12D, stat(levelThirty, "Damage taken through links"), 1.0E-9D);
    }

    @Test
    void conditionalTypedMechanicsExplainTheirRuntimeCondition() {
        List<ClassMenuView.PassiveStat> stats = ClassMenuProjector.passiveStats("strategist", 1);

        assertEquals(.0213D, stat(stats, "Ability cost reduction near allies"), 1.0E-9D);
        assertEquals(-.0213D, stat(stats, "Ability cost reduction while alone"), 1.0E-9D);
    }

    private static double stat(List<ClassMenuView.PassiveStat> stats, String label) {
        return stats.stream()
                .filter(stat -> stat.label().equals(label))
                .mapToDouble(ClassMenuView.PassiveStat::fraction)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing passive stat " + label));
    }

    private static List<ClassMenuPresentation> pages(ClassMenuView view) {
        List<ClassMenuPresentation> pages = new ArrayList<>();
        pages.add(ClassMenuPresenter.overview(view));
        pages.add(ClassMenuPresenter.controls(view));
        for (ClassMenuView.FormView form : view.forms().values()) {
            pages.add(ClassMenuPresenter.form(view, form));
        }
        return pages;
    }

    private static List<String> playerFacingCopy(ClassMenuPresentation page) {
        List<String> copy = new ArrayList<>();
        copy.add(page.title());
        copy.addAll(page.bodyLines());
        for (ClassMenuPresentation.ActionView action : page.actions()) {
            copy.add(action.label());
            copy.addAll(action.tooltip().lines().toList());
        }
        copy.removeIf(String::isBlank);
        return copy;
    }

    private static String visibleText(String text) {
        String visible = MINI_MESSAGE_TAG.matcher(text).replaceAll("");
        visible = LEGACY_HEX.matcher(visible).replaceAll("");
        return LEGACY_FORMAT.matcher(visible).replaceAll("");
    }

    private static long countLabelsContaining(ClassMenuPresentation page, String text) {
        return page.actions().stream()
                .map(ClassMenuPresentation.ActionView::label)
                .map(ClassMenuPresenterTest::visibleText)
                .filter(label -> label.contains(text))
                .count();
    }

    private static ClassMenuView catalogView(boolean unlocked) {
        return catalogView(unlocked, 1);
    }

    private static ClassMenuView catalogView(boolean unlocked, int rootLevel) {
        ClassCatalog catalog = BuiltInClassContent.catalog();
        Map<String, ClassMenuView.FormView> forms = new LinkedHashMap<>();
        for (ClassFormDefinition definition : catalog.forms()) {
            ClassLineage lineage = catalog.lineageOf(definition.id());
            int effectiveLevel = unlocked
                    ? (definition.parentId() == null ? rootLevel : definition.band().effectiveStart())
                    : 0;
            int effectiveCap = unlocked
                    ? (definition.band().isTerminal() ? 100 : definition.band().effectiveEnd())
                    : 0;
            int localCap = unlocked ? definition.band().toLocalLevel(effectiveCap) : 0;
            List<ClassMenuView.FoundationRequirement> requirements = definition.foundationSkills().asList().stream()
                    .map(skill -> new ClassMenuView.FoundationRequirement(
                            skill.getDisplayName(),
                            unlocked ? definition.requiredFoundationSkillLevel() : 0,
                            definition.requiredFoundationSkillLevel()))
                    .toList();
            List<ClassMenuView.BlockerView> blockers = unlocked
                    ? List.of()
                    : blockers(catalog, definition);
            int displayedEffectiveLevel = unlocked
                    ? effectiveLevel
                    : definition.band().effectiveStart();
            Map<String, Integer> passiveLevels = lineage.passiveContributionLevels(displayedEffectiveLevel);
            forms.put(definition.id(), new ClassMenuView.FormView(
                    definition.id(),
                    definition.displayName(),
                    definition.band(),
                    unlocked,
                    false,
                    0D,
                    false,
                    false,
                    lineage.forms().stream().map(ClassFormDefinition::displayName).toList(),
                    definition.parentId(),
                    catalog.childrenOf(definition.id()).stream()
                            .map(child -> new ClassMenuView.FormLink(
                                    child.id(), child.displayName(), unlocked,
                                    unlocked ? child.band().effectiveStart() : 0))
                            .toList(),
                    unlocked ? 1 : 0,
                    effectiveLevel,
                    localCap,
                    effectiveCap,
                    unlocked ? "0/100 XP" : "Unavailable until this form is unlocked.",
                    requirements,
                    blockers,
                    lineage.resourceType().displayName(),
                    lineage.resourceType().description(),
                    ability(lineage.mobility()),
                    ability(definition.signature()),
                    ability(definition.utility()),
                    lineage.forms().stream()
                            .map(lineageForm -> passiveView(
                                    lineageForm,
                                    passiveLevels.get(lineageForm.id()),
                                    !unlocked))
                            .toList()));
        }
        return new ClassMenuView(
                "None",
                "None",
                false,
                "Open until the run starts.",
                InputProfile.JAVA_HOTBAR_LAYER,
                InputProfile.JAVA_HOTBAR_LAYER,
                8,
                catalog.roots().stream().map(root -> forms.get(root.id())).toList(),
                forms);
    }

    private static ClassMenuView withActiveForm(ClassMenuView view, String formId) {
        Map<String, ClassMenuView.FormView> forms = new LinkedHashMap<>();
        for (ClassMenuView.FormView form : view.forms().values()) {
            forms.put(form.id(), new ClassMenuView.FormView(
                    form.id(),
                    form.displayName(),
                    form.band(),
                    form.unlocked(),
                    form.challengeEligible(),
                    form.challengeFee(),
                    form.id().equals(formId),
                    form.id().equals(formId),
                    form.lineage(),
                    form.parentId(),
                    form.children(),
                    form.localLevel(),
                    form.effectiveLevel(),
                    form.localCap(),
                    form.effectiveCap(),
                    form.xpSummary(),
                    form.requirements(),
                    form.blockers(),
                    form.resourceName(),
                    form.resourceDescription(),
                    form.mobility(),
                    form.signature(),
                    form.utility(),
                    form.passives()));
        }
        return new ClassMenuView(
                forms.get(formId).displayName(),
                forms.get(formId).displayName(),
                view.runLocked(),
                view.runLockSummary(),
                view.selectedInput(),
                view.activeInput(),
                view.focusSlot(),
                view.roots().stream().map(root -> forms.get(root.id())).toList(),
                forms);
    }

    private static List<ClassMenuView.BlockerView> blockers(
            ClassCatalog catalog,
            ClassFormDefinition definition) {
        List<ClassMenuView.BlockerView> blockers = new ArrayList<>();
        if (definition.parentId() != null) {
            ClassFormDefinition parent = catalog.require(definition.parentId());
            blockers.add(new ClassMenuView.BlockerView(
                    parent.displayName(),
                    30,
                    0,
                    catalog.lineageOf(parent.id()).resourceType().displayName()));
        }
        definition.foundationSkills().asList().forEach(skill -> blockers.add(new ClassMenuView.BlockerView(
                skill.getDisplayName(),
                definition.requiredFoundationSkillLevel(),
                0,
                null)));
        return blockers;
    }

    private static ClassMenuView.PassiveView passiveView(
            ClassFormDefinition form,
            int contributionLevel,
            boolean preview) {
        return new ClassMenuView.PassiveView(
                form.displayName(),
                form.passive().description(),
                contributionLevel,
                preview,
                ClassMenuProjector.passiveStats(form.id(), contributionLevel));
    }

    private static ClassMenuView.AbilityView ability(AbilityDefinition definition) {
        return new ClassMenuView.AbilityView(definition.displayName(), definition.description());
    }

    private static long countActions(
            ClassMenuPresentation page,
            Class<? extends ClassMenuAction> actionType) {
        return page.actions().stream().filter(action -> actionType.isInstance(action.action())).count();
    }

    private static int assertLockedActionsExplainBlockers(
            ClassMenuPresentation page,
            ClassMenuView view) {
        int lockedActions = 0;
        for (ClassMenuPresentation.ActionView action : page.actions()) {
            if (action.kind() != ClassMenuPresentation.ActionKind.FORM) continue;
            if (!(action.action() instanceof ClassMenuAction.OpenForm openForm)) continue;
            ClassMenuView.FormView form = view.requireForm(openForm.formId());
            if (form.unlocked()) continue;
            lockedActions++;
            assertEquals(ClassMenuPresentation.Tone.LOCKED, action.tone());
            assertTrue(action.label().contains(ClassMenuStyle.RED));
            for (ClassMenuView.BlockerView blocker : form.blockers()) {
                String expected = visibleText(ClassMenuStyle.blockerText(blocker));
                assertTrue(visibleText(action.tooltip()).contains(expected));
            }
        }
        return lockedActions;
    }

}
