package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class LootCommand {

    public static void get(Player player, String args1) {
        CustomItem customItem = CustomItem.getCustomItem(args1);
        if (customItem == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cFile name " + args1 + " &cis not a valid custom item file name!"));
            return;
        }
        player.getInventory().addItem(customItem.generateDefaultsItemStack(player, false));
    }

    public static void give(CommandSender commandSender, String playerString, String args1) {
        CustomItem customItem = CustomItem.getCustomItem(args1);
        if (customItem == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cFile name " + args1 + " &cis not a valid custom item file name!"));
            return;
        }
        Player player = Bukkit.getPlayer(playerString);
        if (player == null)
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cTried to give item to invalid player!"));
        else
            player.getInventory().addItem(customItem.generateDefaultsItemStack(player, false));
    }

}
