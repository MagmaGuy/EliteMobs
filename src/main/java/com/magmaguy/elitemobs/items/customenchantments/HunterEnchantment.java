package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class HunterEnchantment extends CustomEnchantment {

    public static String key = "hunter";

    public HunterEnchantment() {
        super(key);
    }

    private static final int range = Bukkit.getServer().getViewDistance() * 16;

    public static double getHuntingGearBonus(ArrayList<Player> players) {
        double huntingGearChanceAdder = 0;
        for (Player player : players)
            huntingGearChanceAdder += ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getHunterChance(true);
        return huntingGearChanceAdder;
    }

}
