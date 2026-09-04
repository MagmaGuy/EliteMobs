package com.magmaguy.elitemobs.experimentalcombat.menu;

import org.bukkit.entity.Player;

interface ClassMenuRenderer {
    void showOverview(Player player, ClassMenuView view);

    void showForm(Player player, ClassMenuView view, ClassMenuView.FormView form);

    void showControls(Player player, ClassMenuView view);
}
