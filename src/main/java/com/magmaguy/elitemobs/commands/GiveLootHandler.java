package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class GiveLootHandler {

    public static void giveLootCommand(CommandSender commandSender, String[] args) {

        if (Bukkit.getServer().getPlayer(args[1]) != null) {

            Player receiver = Bukkit.getServer().getPlayer(args[1]);

            if (args[2].equalsIgnoreCase("random") || args[2].equalsIgnoreCase("r")) {
                receiver.getInventory().addItem(CustomItem.getCustomItemStackList().get(ThreadLocalRandom.current().nextInt(CustomItem.getCustomItemStackList().size())));

            } else
                GetLootCommandHandler.getLoot(receiver, args[2]);

        } else
            commandSender.sendMessage("Can't give loot to player - player not found.");

    }

}
