package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.LootTables;
import org.bukkit.entity.Player;

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
            new EliteItemLore(LootTables.generateLoot(level, player.getLocation(), player).getItemStack(), false);
        } catch (Exception ex) {
            player.sendMessage("Your loot simulation resulted in no loot. This is probably normal based on drop chances.");
        }
    }
}
