package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.dungeons.Minidungeon;
import org.bukkit.entity.Player;

public class TreasureChestCommands {
    private TreasureChestCommands() {
    }

    public static void addRelativeTreasureChest(Player player, String treasureChestFilename, String minidungeonName) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonName);
        if (minidungeon == null) {
            player.sendMessage("Invalid minidungeon name!");
            return;
        }
        minidungeon.addRelativeTreasureChest(treasureChestFilename, player.getLocation().getBlock().getLocation(), player);
    }
}
