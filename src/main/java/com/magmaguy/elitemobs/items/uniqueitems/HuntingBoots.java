package com.magmaguy.elitemobs.items.uniqueitems;

import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.Arrays;
import java.util.List;

public class HuntingBoots extends UniqueItem {

    @Override
    public String definePath() {
        return "Hunting Boots";
    }

    @Override
    public String defineType() {
        return Material.DIAMOND_BOOTS.toString();
    }

    @Override
    public String defineName() {
        return "&4Elite Mob Hunting Boots";
    }


    @Override
    public List<String> defineLore() {
        return Arrays.asList("Only for those fleetest of foot.");
    }

    @Override
    public List<String> defineEnchantments() {
        return Arrays.asList("VANISHING_CURSE,1", "PROTECTION_ENVIRONMENTAL,1", CustomEnchantmentCache.hunterEnchantment.assembleConfigString(2));
    }

    @Override
    public List<String> definePotionEffects() {
        return Arrays.asList("SPEED,2,self,continuous");
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
