package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.LootTables;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class SimLootHandler {

    public static void simLoot(CommandSender commandSender, String[] args) {
        if (args.length < 3) {
            commandSender.sendMessage("[EM] Not enough arguments! Correct syntax:");
            commandSender.sendMessage("- /elitemobs simloot [playername] [tier]");
        }

        Player player;

        try {
            player = Bukkit.getPlayer(args[1]);
        } catch (Exception ex) {
            commandSender.sendMessage("[EM]" + args[1] + " is not a valid player name!");
            return;
        }

        int tier = 0;

        try {
            tier = Integer.parseInt(args[2]);
        } catch (Exception ex) {
            commandSender.sendMessage("[EM]" + args[2] + " is not a valid integer value!");
        }

        simLoot(player, tier, 1);

    }

    public static void simLoot(Player player, int level, int timesToRun) {
        for (int i = 0; i < timesToRun; i++)
            try {
                new EliteItemLore(LootTables.generateLoot(level, player.getLocation(), player).getItemStack(), false);
            } catch (Exception ex) {
                player.sendMessage("Your loot simulation resulted in no loot. This is probably normal based on drop chances.");
            }
    }

}
