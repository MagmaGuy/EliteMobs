package com.magmaguy.elitemobs.items.uniqueitems;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.Arrays;
import java.util.List;

public class HuntingBow extends UniqueItem {

    @Override
    public String definePath() {
        return "Hunting Bow";
    }

    @Override
    public String defineType() {
        return Material.BOW.toString();
    }

    @Override
    public String defineName() {
        return "&4Elite Mob Hunting Bow";
    }


    @Override
    public List<String> defineLore() {
        return Arrays.asList("Only for natural-born hunters.");
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

}
