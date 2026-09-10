package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.classes.BuiltInClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.progression.FormProgressSnapshot;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProfileSnapshot;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProgressionCapReason;
import com.magmaguy.elitemobs.experimentalcombat.progression.SelectionResult;
import com.magmaguy.elitemobs.experimentalcombat.progression.UnlockBlocker;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.entity.Player;


final class ExperimentalClassCommandSupport {

    private ExperimentalClassCommandSupport() {
    }

    static boolean requireModule(Player player) {
        if (ExperimentalCombatModule.isInitialized()) return true;
        send(player, "&c[Alpha] Advanced Combat System is disabled on this server.");
        return false;
    }

    static void showForm(Player player, String formId) {
        if (!requireModule(player)) return;
        ClassCatalog catalog = catalog();
        ClassFormDefinition form = catalog.find(formId).orElse(null);
        if (form == null) {
            send(player, "&cUnknown class form: &f" + formId);
            return;
        }
        ProfileSnapshot profile = ExperimentalCombatModule.get().profile(player.getUniqueId()).orElse(null);
        if (profile == null) {
            send(player, "&eYour class profile is still loading. Try again in a moment.");
            return;
        }
        FormProgressSnapshot progress = profile.forms().get(form.id());
        ClassLineage lineage = catalog.lineageOf(form.id());
        send(player, "&6&l" + form.displayName() + " &8(&7" + form.id() + "&8)");
        send(player, "&7Branch: &f" + String.join(" &8> &f", catalog.progressionPathOf(form.id()).stream()
                .map(ClassFormDefinition::displayName).toList()));
        if (progress.unlocked())
            send(player, "&7Level: &f" + progress.effectiveLevel() + " &8(local " + progress.localLevel()
                    + ") &7cap: &f" + progress.effectiveCap());
        else send(player, "&7Level: &8Unavailable until this form is unlocked.");
        send(player, "&7Required skills: &f" + form.foundationSkills().first().getDisplayName() + " "
                + form.requiredFoundationSkillLevel() + " &7and &f"
                + form.foundationSkills().second().getDisplayName() + " " + form.requiredFoundationSkillLevel());
        send(player, "&7Resource: &f" + lineage.resourceType().displayName() + " &8- &7"
                + lineage.resourceType().description());
        if (progress.unlocked()) send(player, "&aUnlocked");
        else for (UnlockBlocker blocker : progress.unlockBlockers()) send(player, formatBlocker(blocker));
        send(player, "&ePassive: &f" + form.passive().description());
        send(player, "&bMobility - " + lineage.mobility().displayName() + ": &f" + lineage.mobility().description());
        send(player, "&cSignature - " + form.signature().displayName() + ": &f" + form.signature().description());
        send(player, "&aUtility - " + form.utility().displayName() + ": &f" + form.utility().description());
        if (!catalog.childrenOf(form.id()).isEmpty()) {
            send(player, "&7Next specializations:");
            for (ClassFormDefinition child : catalog.childrenOf(form.id())) {
                FormProgressSnapshot childProgress = profile.forms().get(child.id());
                String state = childProgress != null && childProgress.unlocked() ? "&aunlocked" : "&clocked";
                send(player, "  &f" + child.displayName() + " &8(&7" + child.id() + "&8) - " + state);
            }
        }
        if (!progress.unlocked()) {
            send(player, "&7Class XP: &8Unavailable until this form is unlocked.");
        } else if (progress.xp() >= progress.xpAtCap()) {
            if (progress.capReason() == ProgressionCapReason.BAND_COMPLETE) {
                send(player, "&7Class XP: &eCAPPED &8- &7choose a specialization to continue; overflow XP is not banked.");
            } else {
                String limitingSkills = String.join(" and ", progress.limitingSkills().stream()
                        .map(skill -> skill.getDisplayName())
                        .toList());
                send(player, "&7Class XP: &eCAPPED &8- &7raise &f" + limitingSkills
                        + " &7to continue; overflow XP is not banked.");
            }
        } else {
            long curveBaseline = SkillXPCalculator.totalXPForLevel(form.band().effectiveStart());
            long currentLevelStart = SkillXPCalculator.totalXPForLevel(progress.effectiveLevel()) - curveBaseline;
            long nextLevelXp = SkillXPCalculator.xpToNextLevel(progress.effectiveLevel());
            send(player, "&7Class XP: &f" + Math.max(0, progress.xp() - currentLevelStart)
                    + "/" + nextLevelXp);
        }
    }

    static void reportSelection(Player player, String requestedId, SelectionResult result) {
        switch (result.status()) {
            case APPLIED -> {
                ClassFormDefinition form = catalog().require(requestedId);
                send(player, "&aSelected &f" + form.displayName() + "&a.");
            }
            case UNCHANGED -> send(player, "&7That class or control profile is already selected.");
            case NOT_READY -> send(player, "&eYour class profile is still loading.");
            case UNKNOWN_FORM -> send(player, "&cUnknown class form: &f" + requestedId);
            case LOCKED_FORM -> {
                send(player, "&cThat selection is locked right now.");
                ProfileSnapshot snapshot = result.snapshot();
                if (snapshot != null && snapshot.forms().containsKey(requestedId)) {
                    for (UnlockBlocker blocker : snapshot.forms().get(requestedId).unlockBlockers())
                        send(player, formatBlocker(blocker));
                }
            }
        }
    }

    private static String formatBlocker(UnlockBlocker blocker) {
        if (blocker.optionalReason().isPresent())
            return "&c" + blocker.reason();
        if (blocker.kind() == UnlockBlocker.Kind.FOUNDATION_SKILL)
            return "&cYou need level &f" + blocker.requiredLevel() + "&c in &f"
                    + blocker.skillType().getDisplayName() + "&c. You are level &f"
                    + blocker.currentLevel() + "&c.";
        ClassFormDefinition parent = catalog().require(blocker.formId());
        return "&cMax out &f" + parent.displayName() + " &cat level &f"
                + parent.band().toEffectiveLevel(blocker.requiredLevel()) + "&c before branching. "
                + "You are level &f" + (blocker.currentLevel() == 0 ? 0
                : parent.band().toEffectiveLevel(blocker.currentLevel())) + "&c.";
    }

    static void send(Player player, String message) {
        player.sendMessage(ChatColorConverter.convert(message));
    }

    static ClassCatalog catalog() {
        return ExperimentalCombatModule.isInitialized()
                ? ExperimentalCombatModule.get().catalog()
                : BuiltInClassCatalog.catalog();
    }
}
