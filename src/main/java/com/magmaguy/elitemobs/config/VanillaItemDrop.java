package com.magmaguy.elitemobs.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class VanillaItemDrop {
    @Getter
    @Setter
    private ItemStack itemStack;
    @Getter
    @Setter
    private double chance;

    public VanillaItemDrop(ItemStack itemStack, double chance) {
        this.itemStack = itemStack;
        this.chance = chance;
    }
}
