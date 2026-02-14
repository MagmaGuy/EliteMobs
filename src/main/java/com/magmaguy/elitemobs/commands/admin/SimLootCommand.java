package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class SimLootCommand {

    public static void forcePositiveLoot(CommandSender commandSender, String playerName, int level) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            commandSender.sendMessage(CommandMessagesConfig.getSimLootPlayerNotFound().replace("$player", playerName));
            return;
        }
        int counter = 0;
        while (true) {
            counter++;
            if (run(player, level, false)) break;
            if (counter > 1000) {
                Logger.info("Failed to generate loot within 1000 attempts! This is almost certainly an issue with the way the loot is configured in your server.");
                break;
            }
        }
    }

    public static void runMultipleTimes(Player player, int level, int timesToRun) {
        for (int i = 0; i < timesToRun; i++)
            run(player, level, true);
    }

    public static void runMultipleTimes(CommandSender player, int level, int timesToRun, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            player.sendMessage(CommandMessagesConfig.getSimLootCouldNotSend().replace("$player", playerName));
            return;
        }
        for (int i = 0; i < timesToRun; i++)
            run(targetPlayer, level, true);
        player.sendMessage(CommandMessagesConfig.getSimLootFinishedMultiple()
                .replace("$player", playerName)
                .replace("$times", String.valueOf(timesToRun))
                .replace("$level", String.valueOf(level)));
    }

    public static boolean run(Player player, int level, boolean message) {
        try {
            ItemStack itemStack = LootTables.generateLoot(level, player.getLocation(), player);
            if (itemStack == null) {
                if (message)
                    player.sendMessage(ItemSettingsConfig.getSimlootMessageFailure());
                return false;
            } else {
                EliteItemLore eliteItemLore = new EliteItemLore(itemStack, false);
                if (message)
                    player.sendMessage(ItemSettingsConfig.getSimlootMessageSuccess().replace("$itemName", eliteItemLore.getItemStack().getItemMeta().getDisplayName()));
                return true;
            }
        } catch (Exception ex) {
            if (message)
                player.sendMessage(ItemSettingsConfig.getSimlootMessageFailure());
            return false;
        }
    }

    public static void run(CommandSender player, int level, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            player.sendMessage(CommandMessagesConfig.getSimLootCouldNotSend().replace("$player", playerName));
            return;
        }
        run(targetPlayer, level, true);
        player.sendMessage(CommandMessagesConfig.getSimLootFinishedSingle()
                .replace("$player", playerName)
                .replace("$level", String.valueOf(level)));
    }

    public static void simulateSpecialLoot(Player player, int timesToRun) {
        for (int i = 0; i < timesToRun; i++)
            LootTables.generateSpecialLoot(player, 0, null);
    }
}
