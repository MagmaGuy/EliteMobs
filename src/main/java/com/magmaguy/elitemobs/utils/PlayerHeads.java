package com.magmaguy.elitemobs.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerHeads {

    public static ItemStack exclamation() {
        ItemStack exclamation;
        exclamation = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta exclamationMeta = (SkullMeta) exclamation.getItemMeta();
        exclamationMeta.setOwner("MHF_Exclamation");
        exclamation.setItemMeta(exclamationMeta);

        return exclamation;

    }

}
