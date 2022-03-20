package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.ArenaMenu;
import org.bukkit.entity.Player;

public class ArenaCommands {
    public static void openArenaMenu(Player player, String arenaFilename) {
        ArenaMenu arenaMenu = new ArenaMenu();
        arenaMenu.constructArenaMenu(player, arenaFilename);
    }
}
