package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.SchematicDungeonPackage;
import org.bukkit.entity.Player;

public class TreasureChestCommands {
    private TreasureChestCommands() {
    }

    public static void addRelativeTreasureChest(Player player, String treasureChestFilename, String minidungeonName) {
        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonName);
        if (emPackage == null) {
            player.sendMessage("Invalid minidungeon name!");
            return;
        }
        if (!(emPackage instanceof SchematicDungeonPackage)) {
            player.sendMessage("The dungeon needs to be a schematic dungeon package!");
            return;
        }
        ((SchematicDungeonPackage) emPackage).addChest(treasureChestFilename, player.getLocation().getBlock().getLocation());
    }
}
