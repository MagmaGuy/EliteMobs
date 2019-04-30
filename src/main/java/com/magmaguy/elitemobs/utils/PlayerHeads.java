package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerHeads {

    public static ItemStack exclamation() {
        ItemStack exclamation;
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.SKULL_SIGNATURE_ITEM)) {
            exclamation = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta exclamationMeta = (SkullMeta) exclamation.getItemMeta();
            exclamationMeta.setOwner("MHF_Exclamation");
            exclamation.setItemMeta(exclamationMeta);
        } else {
            exclamation = new ItemStack(Material.PAPER);
        }

        return exclamation;

    }

}
