package com.magmaguy.elitemobs.items.uniqueitems;

import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.Arrays;
import java.util.List;

public class HuntingHelmet extends UniqueItem {

    @Override
    public String definePath() {
        return "Hunting Helmet";
    }

    @Override
    public String defineType() {
        return Material.DIAMOND_HELMET.toString();
    }

    @Override
    public String defineName() {
        return "&4Elite Mob Hunting Helmet";
    }


    @Override
    public List<String> defineLore() {
        return Arrays.asList("Only for the sharpest of eyes.");
    }

    @Override
    public List<String> defineEnchantments() {
        return Arrays.asList("VANISHING_CURSE,1", "PROTECTION_ENVIRONMENTAL,1", CustomEnchantmentCache.hunterEnchantment.assembleConfigString(2));
    }

    @Override
    public List<String> definePotionEffects() {
        return Arrays.asList("NIGHT_VISION,1,self,continuous");
    }

    @Override
    public String defineDropWeight() {
        return "dynamic";
    }

    @Override
    public String defineScalability() {
        return "dynamic";
    }

    @Override
    public void assembleConfigItem(Configuration configuration) {
        super.assembleConfigItem(configuration);
    }


}
