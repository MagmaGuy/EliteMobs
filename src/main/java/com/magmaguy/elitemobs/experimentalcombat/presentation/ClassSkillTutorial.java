package com.magmaguy.elitemobs.experimentalcombat.presentation;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.progression.ClassProgressionModule;
import com.magmaguy.elitemobs.experimentalcombat.progression.SkillTutorialProgress;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Chat onboarding; only the prompt throttle is session-scoped, milestones live in the class store. */
public final class ClassSkillTutorial {
    public static final String CONTROLS = "&7F,F: Mobility | F+LMB: Signature | F+RMB: Utility";
    private final Set<UUID> prompted = new HashSet<>();

    public void prompt(Player player, ClassProgressionModule progression, boolean waiting) {
        if (prompted.contains(player.getUniqueId())) return;
        progression.tutorialProgress(player.getUniqueId()).ifPresent(progress -> {
            if (progress.complete()) return;
            prompted.add(player.getUniqueId());
            message(player, "&eTry all three class skills! &7Press and release your swap-hands key"
                    + " (default F), then quickly use the second input.");
            if (waiting) message(player, "&aYour class resource stays full while waiting. Try your skills here!");
            showRemaining(player, progress);
        });
    }

    public void successfulCast(Player player, ClassProgressionModule progression, AbilitySlot slot) {
        progression.recordTutorialSkill(player.getUniqueId(), slot).ifPresent(progress -> {
            if (progress.complete()) {
                message(player, "&aYou've used all three class skills! Tutorial complete; these reminders are now dismissed.");
            } else {
                message(player, "&a" + name(slot) + " practiced! &7Try the remaining skills:");
                showRemaining(player, progress);
            }
        });
    }

    private static void showRemaining(Player player, SkillTutorialProgress progress) {
        if (!progress.hasUsed(AbilitySlot.MOBILITY)) message(player, "&fF, then F &8— &bMobility");
        if (!progress.hasUsed(AbilitySlot.SIGNATURE)) message(player, "&fF, then left-click &8— &bSignature");
        if (!progress.hasUsed(AbilitySlot.UTILITY)) message(player, "&fF, then right-click &8— &bUtility");
        message(player, "&7A successful cast checks off a skill. Targeted skills may need a suitable target.");
    }

    private static String name(AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> "Mobility";
            case SIGNATURE -> "Signature";
            case UTILITY -> "Utility";
        };
    }

    private static void message(Player player, String text) {
        player.sendMessage(ChatColorConverter.convert("&6[Class Skills] &r" + text));
    }

    public void discard(UUID playerId) { prompted.remove(playerId); }
    public void clear() { prompted.clear(); }
}
