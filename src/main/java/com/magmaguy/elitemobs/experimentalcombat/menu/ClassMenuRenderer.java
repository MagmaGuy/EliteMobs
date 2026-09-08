package com.magmaguy.elitemobs.experimentalcombat.menu;

import org.bukkit.entity.Player;

interface ClassMenuRenderer {
    void showOverview(Player player, ClassMenuView view);

    default void showForm(Player player, ClassMenuView view, ClassMenuView.FormView form) {
        showForm(player, view, form, true);
    }

    void showForm(Player player, ClassMenuView view, ClassMenuView.FormView form, boolean showAllClasses);

    void showControls(Player player, ClassMenuView view);
}
