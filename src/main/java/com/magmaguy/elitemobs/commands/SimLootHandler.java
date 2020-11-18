package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

        simLoot(player, tier);

    }

    public static void simLoot(Player player, int level) {
        ItemStack itemStack = ItemConstructor.constructItem(level, null, player, false);
        player.getWorld().dropItem(player.getLocation(), itemStack);
    }

}
