package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;

/** Shared color and status language for both class-menu renderers. */
final class ClassMenuStyle {
    static final String ELITE = ClassPresentationTheme.ELITE;
    static final String GOLD = ClassPresentationTheme.GOLD;
    static final String ORANGE = ClassPresentationTheme.ORANGE;
    static final String BLUE = ClassPresentationTheme.BLUE;
    static final String TEAL = ClassPresentationTheme.TEAL;
    static final String GREEN = ClassPresentationTheme.GREEN;
    static final String RED = ClassPresentationTheme.RED;
    static final String PURPLE = ClassPresentationTheme.PURPLE;

    private ClassMenuStyle() {
    }

    static String title(String text) {
        return gradient(ELITE, text);
    }

    static String themed(ClassMenuView.FormView form, String text) {
        return gradient(theme(form.resourceName()), text);
    }

    static String themedResource(String resourceName, String text) {
        return gradient(theme(resourceName), text);
    }

    static String formButtonLabel(ClassMenuView.FormView form) {
        if (!form.unlocked()) {
            return gradient(RED, form.displayName()) + " &8• " + gradient(RED, "Locked");
        }

        String status;
        if (form.active()) status = gradient(GOLD, "Active");
        else status = gradient(TEAL, "Lv " + form.effectiveLevel());
        return themed(form, form.displayName()) + " &8• " + status;
    }

    static String formTooltip(ClassMenuView.FormView form) {
        if (!form.unlocked()) return lockTooltip(form);
        ClassMenuView.PassiveView ownPassive = form.passives().get(form.passives().size() - 1);
        return "&7" + ownPassive.description();
    }

    static String lockTooltip(ClassMenuView.FormView form) {
        StringBuilder tooltip = new StringBuilder(gradient(RED, "Locked"));
        for (ClassMenuView.BlockerView blocker : form.blockers()) {
            tooltip.append('\n').append(blockerText(blocker));
        }
        tooltip.append('\n').append(trialInstructions(form));
        return tooltip.toString();
    }

    static String trialInstructions(ClassMenuView.FormView form) {
        String root = form.lineage().getFirst();
        String fee = com.magmaguy.elitemobs.economy.EconomyHandler.formatCurrency(form.challengeFee());
        return "&eVisit the " + root + " trainer in the Adventurer's Guild.\n"
                + "&7Meet the training requirements, then select Challenge Instructor.\n"
                + "&7Each solo attempt costs " + fee + " coins when combat begins.\n"
                + "&7Defeat the instructor to unlock and activate " + form.displayName() + ".";
    }

    static String blockerText(ClassMenuView.BlockerView blocker) {
        if (blocker.contentRequirement()) return "&c" + blocker.reason();
        String name = blocker.classRequirement()
                ? themedResource(blocker.classResourceName(), blocker.displayName())
                : "&f" + blocker.displayName();
        if (blocker.classRequirement()) {
            return "&cYou need to be level &f" + blocker.requiredLevel()
                    + " &cin " + name + "&c. &7You are level &f"
                    + blocker.currentLevel() + "&7.";
        }
        return "&cYou need level &f" + blocker.requiredLevel()
                + " &cin " + name + "&c. &7You are level &f"
                + blocker.currentLevel() + "&7.";
    }

    static String state(ClassMenuView.FormView form) {
        if (!form.unlocked()) return gradient(RED, "Locked");
        if (form.active()) return gradient(GOLD, "Active");
        return gradient(TEAL, "Unlocked");
    }

    static String section(String colors, String text) {
        return gradient(colors, text);
    }

    static String gradient(String colors, String text) {
        return ClassPresentationTheme.gradient(colors, text);
    }

    static String theme(String resourceName) {
        return switch (resourceName) {
            case "Resolve" -> GOLD;
            case "Fury" -> ORANGE;
            case "Focus" -> GREEN;
            case "Grace" -> BLUE;
            case "Mana" -> PURPLE;
            default -> ELITE;
        };
    }
}
