package com.magmaguy.elitemobs.items.uniqueitems;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class HuntingLeggings extends UniqueItem{

    @Override
    public String definePath() {
        return "Hunting Leggings";
    }

    @Override
    public String defineType() {
        return Material.DIAMOND_LEGGINGS.toString();
    }

    @Override
    public String defineName() {
        return "&4Elite Mob Hunting Leggings";
    }


    @Override
    public List<String> defineLore() {
        return Arrays.asList(
                "Wearing these leggings will",
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
        return Arrays.asList("JUMP,2,self,continuous");
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
