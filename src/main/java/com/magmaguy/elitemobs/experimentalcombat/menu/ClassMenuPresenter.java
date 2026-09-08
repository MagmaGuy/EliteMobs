package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/** Owns concise copy and navigation so both presentation adapters stay in sync. */
final class ClassMenuPresenter {
    private static final int BODY_WIDTH = 420;
    private static final int BUTTON_WIDTH = 205;

    private ClassMenuPresenter() {
    }

    static ClassMenuPresentation overview(ClassMenuView view) {
        ClassMenuView.FormView activeForm = view.forms().values().stream()
                .filter(ClassMenuView.FormView::active)
                .findFirst()
                .orElse(null);
        String activeClass = activeForm == null
                ? "&7None"
                : ClassMenuStyle.themed(activeForm, activeForm.displayName());
        List<String> body = List.of(
                ClassMenuStyle.section(ClassMenuStyle.GOLD, "Active class:")
                        + " " + activeClass);

        List<ClassMenuPresentation.ActionView> actions = new ArrayList<>();
        for (ClassMenuView.FormView root : view.roots()) {
            actions.add(formAction(root, ClassMenuPresentation.ActionKind.FORM));
        }
        actions.add(new ClassMenuPresentation.ActionView(
                ClassMenuPresentation.ActionKind.CONTROLS,
                ClassMenuPresentation.Tone.CONTROL,
                ClassMenuStyle.section(ClassMenuStyle.TEAL, "Ability Controls"),
                "&7Choose how your three abilities are activated.",
                new ClassMenuAction.OpenControls()));
        if (activeForm != null) {
            actions.add(new ClassMenuPresentation.ActionView(
                    ClassMenuPresentation.ActionKind.DEACTIVATE,
                    ClassMenuPresentation.Tone.CONTROL,
                    ClassMenuStyle.section(ClassMenuStyle.RED, "Deactivate Class"),
                    "&7Play without any class. Your levels are kept.",
                    new ClassMenuAction.DeactivateClass()));
        }
        return new ClassMenuPresentation(
                ClassMenuStyle.title("Classes"), body, 2, BUTTON_WIDTH, actions);
    }

    static ClassMenuPresentation form(ClassMenuView view, ClassMenuView.FormView form) {
        List<String> body = new ArrayList<>();
        if (form.lineage().size() > 1) {
            body.add("&7Path &8• " + String.join(" &8> ", form.lineage().stream()
                    .map(name -> ClassMenuStyle.themed(form, name))
                    .toList()));
        }
        if (form.unlocked()) {
            body.add(ClassMenuStyle.state(form) + " &8• &fLv "
                    + form.effectiveLevel() + "/" + form.effectiveCap());
            body.add("&7XP &8• &f" + form.xpSummary());
        } else {
            body.add(ClassMenuStyle.state(form));
        }
        body.add(skillsLine(form));
        if (!form.unlocked()) {
            for (ClassMenuView.BlockerView blocker : form.blockers())
                body.add(ClassMenuStyle.blockerText(blocker));
        }
        body.add(ClassMenuStyle.themed(form, form.resourceName())
                + " &8• &7" + form.resourceDescription());
        body.add("");
        body.add(abilityLine(ClassMenuStyle.BLUE, "Mobility", form.mobility()));
        body.add(abilityLine(ClassMenuStyle.ORANGE, "Signature", form.signature()));
        body.add(abilityLine(ClassMenuStyle.GREEN, "Utility", form.utility()));
        for (ClassMenuView.PassiveView passive : form.passives()) {
            body.addAll(ClassMenuBonusText.lines(form, passive));
        }
        if (view.runLocked() && !form.selected()) {
            body.add("");
            body.add("&cYour active class is locked for this run.");
        }

        List<ClassMenuPresentation.ActionView> actions = new ArrayList<>();
        if (form.parentId() != null) {
            ClassMenuView.FormView parent = view.requireForm(form.parentId());
            actions.add(new ClassMenuPresentation.ActionView(
                    ClassMenuPresentation.ActionKind.PARENT,
                    ClassMenuPresentation.Tone.NAVIGATION,
                    "← " + ClassMenuStyle.themed(parent, parent.displayName()),
                    "&7Back to the previous class.",
                    new ClassMenuAction.OpenForm(parent.id())));
        }
        for (ClassMenuView.FormLink childLink : form.children()) {
            ClassMenuView.FormView child = view.requireForm(childLink.id());
            actions.add(formAction(child, ClassMenuPresentation.ActionKind.FORM));
        }
        if (form.unlocked() && !form.selected() && !view.runLocked()) {
            actions.add(new ClassMenuPresentation.ActionView(
                    ClassMenuPresentation.ActionKind.SELECT,
                    ClassMenuPresentation.Tone.CONTROL,
                    ClassMenuStyle.section(ClassMenuStyle.GREEN, "Activate " + form.displayName()),
                    "&7Make this your active class.",
                    new ClassMenuAction.SelectForm(form.id())));
        }
        actions.add(new ClassMenuPresentation.ActionView(
                ClassMenuPresentation.ActionKind.OVERVIEW,
                ClassMenuPresentation.Tone.NAVIGATION,
                "← " + ClassMenuStyle.section(ClassMenuStyle.GOLD, "All Classes"),
                "&7Return to all classes.",
                new ClassMenuAction.OpenOverview()));

        return new ClassMenuPresentation(
                ClassMenuStyle.themed(form, form.displayName()), body, 2, BUTTON_WIDTH, actions);
    }

    static ClassMenuPresentation controls(ClassMenuView view) {
        ClassMenuView.FormView active = view.forms().values().stream()
                .filter(ClassMenuView.FormView::active)
                .findFirst()
                .orElse(null);
        String mobility = active == null ? "Mobility" : active.mobility().displayName();
        String signature = active == null ? "Signature" : active.signature().displayName();
        String utility = active == null ? "Utility" : active.utility().displayName();
        List<String> body = List.of(
                ClassMenuStyle.section(ClassMenuStyle.BLUE, "F, F")
                        + " &8• &f" + mobility,
                ClassMenuStyle.section(ClassMenuStyle.ORANGE, "F + LMB")
                        + " &8• &f" + signature,
                ClassMenuStyle.section(ClassMenuStyle.GREEN, "F + RMB")
                        + " &8• &f" + utility,
                ClassMenuStyle.section(ClassMenuStyle.PURPLE, "Class Focus")
                        + " &8• &7The item fallback uses the same three abilities.");

        List<ClassMenuPresentation.ActionView> actions = List.of(
                inputAction(view, InputProfile.JAVA_HOTBAR_LAYER, "Use F Ability Layer",
                        "&7Press F, then F, left-click or right-click."),
                inputAction(view, InputProfile.FOCUS_ITEM, "Use Class Focus",
                        "&7Use one item to choose an ability."),
                new ClassMenuPresentation.ActionView(
                        ClassMenuPresentation.ActionKind.RECOVER_FOCUS,
                        ClassMenuPresentation.Tone.CONTROL,
                        ClassMenuStyle.section(ClassMenuStyle.TEAL, "Recover Class Focus"),
                        "&7Returns the Focus without replacing another item.",
                        new ClassMenuAction.GiveFocusItem()),
                new ClassMenuPresentation.ActionView(
                        ClassMenuPresentation.ActionKind.OVERVIEW,
                        ClassMenuPresentation.Tone.NAVIGATION,
                        "← " + ClassMenuStyle.section(ClassMenuStyle.GOLD, "All Classes"),
                        "&7Return to all classes.",
                        new ClassMenuAction.OpenOverview()));
        return new ClassMenuPresentation(
                ClassMenuStyle.title("Ability Controls"), body, 1, BODY_WIDTH, actions);
    }

    private static ClassMenuPresentation.ActionView formAction(
            ClassMenuView.FormView form,
            ClassMenuPresentation.ActionKind kind) {
        ClassMenuPresentation.Tone tone;
        if (!form.unlocked()) tone = ClassMenuPresentation.Tone.LOCKED;
        else if (form.active()) tone = ClassMenuPresentation.Tone.ACTIVE;
        else tone = ClassMenuPresentation.Tone.UNLOCKED;
        return new ClassMenuPresentation.ActionView(
                kind,
                tone,
                ClassMenuStyle.formButtonLabel(form),
                ClassMenuStyle.formTooltip(form),
                new ClassMenuAction.OpenForm(form.id()));
    }

    private static ClassMenuPresentation.ActionView inputAction(
            ClassMenuView view,
            InputProfile profile,
            String label,
            String tooltip) {
        boolean active = view.activeInput() == profile;
        String profileName = label.startsWith("Use ") ? label.substring(4) : label;
        return new ClassMenuPresentation.ActionView(
                ClassMenuPresentation.ActionKind.INPUT,
                active ? ClassMenuPresentation.Tone.ACTIVE : ClassMenuPresentation.Tone.CONTROL,
                active
                        ? ClassMenuStyle.section(ClassMenuStyle.GREEN, "Using " + profileName)
                        : ClassMenuStyle.section(ClassMenuStyle.TEAL, label),
                tooltip,
                new ClassMenuAction.SelectInput(profile));
    }

    private static String skillsLine(ClassMenuView.FormView form) {
        StringJoiner skills = new StringJoiner(" &8• ");
        for (ClassMenuView.FoundationRequirement requirement : form.requirements()) {
            skills.add((requirement.met() ? "&a" : "&c")
                    + requirement.displayName() + " "
                    + requirement.currentLevel() + "&8/&f" + requirement.requiredLevel());
        }
        return ClassMenuStyle.section(ClassMenuStyle.GOLD, "Skills") + " &8• " + skills;
    }

    private static String abilityLine(
            String colors,
            String slotName,
            ClassMenuView.AbilityView ability) {
        return ClassMenuStyle.section(colors, slotName + " " + ability.displayName())
                + " &8• &7" + ability.description();
    }

}
