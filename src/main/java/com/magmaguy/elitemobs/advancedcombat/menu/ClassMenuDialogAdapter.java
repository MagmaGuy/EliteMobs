package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.magmacore.dialog.DialogManager;
import org.bukkit.entity.Player;

/** Modern Java Edition adapter for the shared class-menu presentation. */
final class ClassMenuDialogAdapter implements ClassMenuRenderer {
    private static final int BODY_WIDTH = 420;
    private final PlayerActionTokenRegistry tokens;

    ClassMenuDialogAdapter(PlayerActionTokenRegistry tokens) {
        this.tokens = tokens;
    }

    @Override
    public void showOverview(Player player, ClassMenuView view) {
        show(player, ClassMenuPresenter.overview(view));
    }

    @Override
    public void showForm(Player player, ClassMenuView view, ClassMenuView.FormView form, boolean showAllClasses) {
        show(player, ClassMenuPresenter.form(view, form, showAllClasses));
    }

    @Override
    public void showControls(Player player, ClassMenuView view) {
        show(player, ClassMenuPresenter.controls(view));
    }

    private void show(Player player, ClassMenuPresentation page) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
                .title(page.title())
                .columns(page.columns())
                .afterAction(DialogManager.AfterAction.CLOSE);
        if (!page.bodyLines().isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(page.bodyText()).width(BODY_WIDTH));
        }
        for (ClassMenuPresentation.ActionView action : page.actions()) {
            builder.addAction(button(action, page.buttonWidth(), player));
        }
        DialogManager.sendDialog(player, builder);
    }

    private DialogManager.ActionButton button(
            ClassMenuPresentation.ActionView action,
            int width,
            Player player) {
        String token = tokens.issue(player.getUniqueId(), action.action());
        return DialogManager.ActionButton.of(
                        action.label(),
                        new DialogManager.RunCommandAction("/elitemobs _classmenu " + token))
                .tooltip(action.tooltip())
                .width(width);
    }
}
