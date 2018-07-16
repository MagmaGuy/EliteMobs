package com.magmaguy.elitemobs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.items.CustomItemConstructor.customItemList;

public class GiveLootHandler {

    public static void giveLootCommand(CommandSender commandSender, String[] args) {

        if (Bukkit.getServer().getPlayer(args[1]) != null) {

            Player receiver = Bukkit.getServer().getPlayer(args[1]);

            if (args[2].equalsIgnoreCase("random") || args[2].equalsIgnoreCase("r")) {

                int index = ThreadLocalRandom.current().nextInt(customItemList.size());
                ItemStack itemStack = new ItemStack(customItemList.get(index));
                receiver.getInventory().addItem(itemStack);

            } else
                GetLootCommandHandler.getLoot(receiver, args[2]);

        } else
            commandSender.sendMessage("Can't give loot to player - player not found.");

    }

}
