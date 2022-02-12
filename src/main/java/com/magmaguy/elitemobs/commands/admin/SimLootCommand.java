package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.LootTables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class SimLootCommand {
    public static void runMultipleTimes(Player player, int level, int timesToRun) {
        for (int i = 0; i < timesToRun; i++)
            run(player, level);
    }

    public static void runMultipleTimes(CommandSender player, int level, int timesToRun, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "[EliteMobs] Could not send item to player " + playerName + " - player with this name was not found!");
            return;
        }
        for (int i = 0; i < timesToRun; i++)
            run(targetPlayer, level);
        player.sendMessage("[EliteMobs] Finished running simulation command for player " + playerName + " " + timesToRun + " times at level " + level + " .");
    }

    public static void run(Player player, int level) {
        try {
            ItemStack itemStack = LootTables.generateLoot(level, player.getLocation(), player);
            if (itemStack == null)
                player.sendMessage(ChatColorConverter.convert(ItemSettingsConfig.getSimlootMessageFailure()));
            else {
                EliteItemLore eliteItemLore = new EliteItemLore(itemStack, false);
                player.sendMessage(ChatColorConverter.convert(ItemSettingsConfig.getSimlootMessageSuccess().replace("$itemName", eliteItemLore.getItemStack().getItemMeta().getDisplayName())));
            }
        } catch (Exception ex) {
            player.sendMessage(ChatColorConverter.convert(ItemSettingsConfig.getSimlootMessageFailure()));
        }
    }

    public static void run(CommandSender player, int level, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "[EliteMobs] Could not send item to player " + playerName + " - player with this name was not found!");
            return;
        }
        run(targetPlayer, level);
        player.sendMessage("[EliteMobs] Finished running simulation command for player " + playerName + " at level " + level + " .");
    }
}
