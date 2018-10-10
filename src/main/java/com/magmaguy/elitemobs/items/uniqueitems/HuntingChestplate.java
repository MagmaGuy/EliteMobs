package com.magmaguy.elitemobs.items.uniqueitems;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class HuntingChestplate extends UniqueItem {

    @Override
    public String definePath() {
        return "Hunting Chestplate";
    }

    @Override
    public String defineType() {
        return Material.DIAMOND_CHESTPLATE.toString();
    }

    @Override
    public String defineName() {
        return "&4Elite Mob Hunting Chestplate";
    }


    @Override
    public List<String> defineLore() {
        return Arrays.asList(
                "Wearing this chestplate will",
                "increase the number of",
                "high level Elite Mobs",
                "that spawn around you!");
    }

    @Override
    public List<String> defineEnchantments() {
        return Arrays.asList("VANISHING_CURSE,1");
    }

    @Override
    public List<String> definePotionEffects() {
        return Arrays.asList("SATURATION,1,self,continuous");
    }

    @Override
    public String defineDropWeight() {
        return "1";
    }

    @Override
    public void assembleConfigItem(Configuration configuration) {
        super.assembleConfigItem(configuration);
    }

    @Override
    public void constructItemStack() {
        super.constructItemStack();
    }

    @Override
    public ItemStack getItemStack() {
        return super.getItemStack();
    }

}
