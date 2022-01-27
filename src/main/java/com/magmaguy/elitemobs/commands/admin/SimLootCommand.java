package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.LootTables;
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

    public static void run(Player player, int level) {
        try {
            ItemStack itemStack = LootTables.generateLoot(level, player.getLocation(), player);
            if (itemStack == null)
                player.sendMessage("Your loot simulation resulted in no loot. This is probably normal based on drop chances.");
            else
                new EliteItemLore(itemStack, false);
        } catch (Exception ex) {
            player.sendMessage("Your loot simulation resulted in no loot. This is probably normal based on drop chances.");
        }
    }
}
