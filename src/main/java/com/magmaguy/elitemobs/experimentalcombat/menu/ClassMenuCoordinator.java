package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProfileSnapshot;
import com.magmaguy.elitemobs.experimentalcombat.progression.SelectionResult;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.entity.Player;

import java.util.Optional;

/** Owns action validation, authoritative refreshes, mutations and presentation routing. */
final class ClassMenuCoordinator {
    private final PlayerActionTokenRegistry tokens;
    private final ClassMenuRenderer dialogs;
    private final ClassMenuRenderer inventories;

    ClassMenuCoordinator(
            PlayerActionTokenRegistry tokens,
            ClassMenuRenderer dialogs,
            ClassMenuRenderer inventories) {
        this.tokens = tokens;
        this.dialogs = dialogs;
        this.inventories = inventories;
    }

    void open(Player player) {
        project(player).ifPresent(view -> {
            tokens.beginPage(player.getUniqueId());
            renderer(player).showOverview(player, view);
        });
    }

    void openForm(Player player, String formId) {
        openForm(player, formId, true);
    }

    void openForm(Player player, String formId, boolean showAllClasses) {
        project(player).ifPresent(view -> {
            ClassMenuView.FormView form = view.forms().get(formId);
            if (form == null) {
                send(player, "&cThat class form no longer exists. The class list has been refreshed.");
                open(player);
                return;
            }
            tokens.beginPage(player.getUniqueId());
            renderer(player).showForm(player, view, form, showAllClasses);
        });
    }

    void openControls(Player player) {
        project(player).ifPresent(view -> {
            tokens.beginPage(player.getUniqueId());
            renderer(player).showControls(player, view);
        });
    }

    void dispatch(Player player, String token) {
        Optional<ClassMenuAction> optionalAction = tokens.consume(player.getUniqueId(), token);
        if (optionalAction.isEmpty()) {
            send(player, "&eThat class-menu action expired. A fresh menu has been opened.");
            open(player);
            return;
        }
        ClassMenuAction action = optionalAction.get();
        switch (action) {
            case ClassMenuAction.OpenOverview ignored -> open(player);
            case ClassMenuAction.OpenControls ignored -> openControls(player);
            case ClassMenuAction.OpenForm openForm -> openForm(player, openForm.formId(), openForm.showAllClasses());
            case ClassMenuAction.SelectForm selectForm -> selectForm(player, selectForm.formId(), selectForm.showAllClasses());
            case ClassMenuAction.Challenge challenge ->
                    com.magmaguy.elitemobs.experimentalcombat.challenges.ClassChallengeInstance.admit(
                            player, challenge.formId(), challenge.quotedFee());
            case ClassMenuAction.DeactivateClass ignored -> deactivateClass(player);
        }
    }

    private void deactivateClass(Player player) {
        if (!ExperimentalCombatModule.isInitialized()) {
            open(player);
            return;
        }
        SelectionResult result = ExperimentalCombatModule.get().clearSelectedForm(player);
        switch (result.status()) {
            case APPLIED -> send(player, "&aClass deactivated. Your levels are kept; pick a class"
                    + " any time with &f/em class&a.");
            case UNCHANGED -> send(player, "&7You have no active class.");
            case NOT_READY -> send(player, "&eYour class profile is still loading.");
            case LOCKED_FORM -> send(player, "&cYour class is locked until this dungeon run ends.");
            default -> send(player, "&cYour class could not be deactivated.");
        }
        open(player);
    }

    private Optional<ClassMenuView> project(Player player) {
        if (!ExperimentalCombatModule.isInitialized()) {
            send(player, "&cExperimental Combat is disabled on this server.");
            return Optional.empty();
        }
        ExperimentalCombatModule module = ExperimentalCombatModule.get();
        ProfileSnapshot profile = module.profile(player.getUniqueId()).orElse(null);
        if (profile == null) {
            send(player, "&eYour class profile is still loading. Try again in a moment.");
            return Optional.empty();
        }
        // Both values are deliberately acquired here, for every render after every action.
        return Optional.of(ClassMenuProjector.project(
                module.catalog(),
                profile,
                skill -> PlayerData.getSkillLevel(player.getUniqueId(), skill)));
    }

    private void selectForm(Player player, String formId, boolean showAllClasses) {
        if (!ExperimentalCombatModule.isInitialized()) {
            open(player);
            return;
        }
        SelectionResult result = ExperimentalCombatModule.get().selectForm(player, formId);
        switch (result.status()) {
            case APPLIED -> {
                // Deliberately three tiny lines: players stop reading anything longer.
                send(player, ClassPresentationTheme.gradient(ClassPresentationTheme.GREEN,
                        "Class active:") + " &f"
                        + ExperimentalCombatModule.get().catalog().require(formId).displayName());
                send(player, ClassPresentationTheme.gradient(ClassPresentationTheme.GOLD,
                        "How to play:") + " &fF,F&7: Mobility | &fF+LMB&7: Signature | &fF+RMB&7: Utility.");
                send(player, ClassPresentationTheme.gradient(ClassPresentationTheme.RED,
                        "Weapons:") + " " + com.magmaguy.elitemobs.experimentalcombat.ClassWeaponAffinity.description(
                        ExperimentalCombatModule.get().catalog().require(formId)));
            }
            case UNCHANGED -> send(player, "&7That is already your active class.");
            case NOT_READY -> send(player, "&eYour class profile is still loading.");
            case UNKNOWN_FORM -> send(player, "&cThat class form no longer exists.");
            case LOCKED_FORM -> send(player, result.snapshot() != null
                    && result.snapshot().lockedRunSelection() != null
                    ? "&cYour class is locked until this dungeon run ends."
                    : "&cThat class form is still locked.");
        }
        openForm(player, formId, showAllClasses);
    }

    private ClassMenuRenderer renderer(Player player) {
        return ClassSelectionMenu.supportsDialogs(player) ? dialogs : inventories;
    }

    private static void send(Player player, String message) {
        player.sendMessage(ChatColorConverter.convert(message));
    }
}
