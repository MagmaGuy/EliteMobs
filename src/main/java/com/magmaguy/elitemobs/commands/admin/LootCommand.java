package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
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
            player.sendMessage(CommandMessagesConfig.getInvalidItemFilenameMessage().replace("$filename", args1));
            return;
        }
        player.getInventory().addItem(customItem.generateDefaultsItemStack(player, false, null, true));
    }

    public static void give(CommandSender commandSender, String playerString, String args1) {
        CustomItem customItem = CustomItem.getCustomItem(args1);
        if (customItem == null) {
            commandSender.sendMessage(CommandMessagesConfig.getInvalidItemFilenameMessage().replace("$filename", args1));
            return;
        }
        Player player = Bukkit.getPlayer(playerString);
        if (player == null)
            commandSender.sendMessage(CommandMessagesConfig.getInvalidPlayerForItemMessage());
        else {
            player.getInventory().addItem(customItem.generateDefaultsItemStack(player, false, null));
            commandSender.sendMessage(CommandMessagesConfig.getGaveItemMessage().replace("$player", player.getName()).replace("$item", customItem.getCustomItemsConfigFields().getName()));
        }
    }

}
