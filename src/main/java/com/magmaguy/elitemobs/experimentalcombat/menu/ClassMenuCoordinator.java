package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityInputRouter;
import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;
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
        project(player).ifPresent(view -> {
            ClassMenuView.FormView form = view.forms().get(formId);
            if (form == null) {
                send(player, "&cThat class form no longer exists. The class list has been refreshed.");
                open(player);
                return;
            }
            tokens.beginPage(player.getUniqueId());
            renderer(player).showForm(player, view, form);
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
            case ClassMenuAction.OpenForm openForm -> openForm(player, openForm.formId());
            case ClassMenuAction.SelectForm selectForm -> selectForm(player, selectForm.formId());
            case ClassMenuAction.Challenge challenge ->
                    com.magmaguy.elitemobs.experimentalcombat.challenges.ClassChallengeInstance.admit(
                            player, challenge.formId(), challenge.quotedFee());
            case ClassMenuAction.SelectInput selectInput -> selectInput(player, selectInput.profile());
            case ClassMenuAction.GiveFocusItem ignored -> giveFocusItem(player);
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

    private void selectForm(Player player, String formId) {
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
                        "How to play:") + " &7Press &fF&7 for abilities &8(&7then &f1/2/3&7,"
                        + " &fclick&7 or &fjump&8)&7. Kill elites to level up.");
                send(player, ClassPresentationTheme.gradient(ClassPresentationTheme.RED,
                        "Weapons:") + " &7class weapons deal &a+10%&7, all others &c-10%&7.");
            }
            case UNCHANGED -> send(player, "&7That is already your active class.");
            case NOT_READY -> send(player, "&eYour class profile is still loading.");
            case UNKNOWN_FORM -> send(player, "&cThat class form no longer exists.");
            case LOCKED_FORM -> send(player, result.snapshot() != null
                    && result.snapshot().lockedRunSelection() != null
                    ? "&cYour class is locked until this dungeon run ends."
                    : "&cThat class form is still locked.");
            case INVALID_FOCUS_SLOT -> send(player, "&cThat class could not be activated.");
        }
        openForm(player, formId);
    }

    private void selectInput(Player player, InputProfile inputProfile) {
        if (!ExperimentalCombatModule.isInitialized()) {
            open(player);
            return;
        }
        SelectionResult result = ExperimentalCombatModule.get().selectInput(player, inputProfile);
        switch (result.status()) {
            case APPLIED -> send(player, "&aClass controls set to &f"
                    + ClassMenuText.inputName(result.snapshot().selectedInputProfile()) + "&a.");
            case UNCHANGED -> send(player, "&7You are already using that control scheme.");
            case NOT_READY -> send(player, "&eYour class profile is still loading.");
            case LOCKED_FORM -> send(player, "&cYour controls are locked until this dungeon run ends.");
            case UNKNOWN_FORM, INVALID_FOCUS_SLOT -> send(player,
                    "&cThat control scheme could not be applied. Please report this to the developer.");
        }
        openControls(player);
    }

    private void giveFocusItem(Player player) {
        if (!ExperimentalCombatModule.isInitialized()) {
            open(player);
            return;
        }
        if (!ClassAbilityEligibility.isEligible(player)) {
            send(player, "&cClass controls are not active here.");
            openControls(player);
            return;
        }
        ClassAbilityInputRouter.FocusItemGiveResult result = ExperimentalCombatModule.get().giveFocusItem(player);
        switch (result.status()) {
            case GIVEN_TO_PREFERRED_SLOT -> send(player,
                    "&aClass Focus placed in hotbar slot &f" + (result.slot() + 1) + "&a.");
            case GIVEN_TO_FALLBACK_SLOT -> send(player,
                    "&eYour preferred slot was occupied; Class Focus was placed in inventory slot &f"
                            + (result.slot() + 1) + "&e without replacing anything.");
            case ALREADY_PRESENT -> send(player, result.slot() < 0
                    ? "&7You already have a Class Focus on your cursor."
                    : "&7You already have a Class Focus in inventory slot &f" + (result.slot() + 1) + "&7.");
            case INVENTORY_FULL -> send(player,
                    "&cYour inventory is full. Free a slot and use &f/em class focus&c again.");
            case INVALID_PREFERRED_SLOT -> send(player,
                    "&cYour saved Focus slot is invalid. Please report this to the developer.");
        }
        openControls(player);
    }

    private ClassMenuRenderer renderer(Player player) {
        return ClassSelectionMenu.supportsDialogs(player) ? dialogs : inventories;
    }

    private static void send(Player player, String message) {
        player.sendMessage(ChatColorConverter.convert(message));
    }
}
