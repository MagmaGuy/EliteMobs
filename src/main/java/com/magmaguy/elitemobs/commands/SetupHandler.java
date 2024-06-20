package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.setup.SetupMenu;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import org.bukkit.entity.Player;

public class SetupHandler {

    public static void setupMenuCommand(Player player) {
        new SetupMenu(player);
    }

    public static void setupMinidungeonCommand(Player player, String minidungeonName) {
        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonName);
        emPackage.install(player, true);
    }
}
