package com.magmaguy.elitemobs.items.uniqueitems;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class TheFeller extends UniqueItem {

    @Override
    public String definePath() {
        return "The Feller";
    }

    @Override
    public String defineType() {
        return Material.DIAMOND_AXE.toString();
    }

    @Override
    public String defineName() {
        return "&2The Feller";
    }

    @Override
    public List<String> defineLore() {
        return Arrays.asList(
                "&aEven in your sleep,",
                "&aYou can feel this axe's",
                "&9saplust"
        );
    }

    @Override
    public List<String> defineEnchantments() {
        return Arrays.asList(
                "LOOT_BONUS_BLOCKS,4",
                "SILK_TOUCH,1",
                "DURABILITY,6",
                "DIG_SPEED,6",
                "VANISHING_CURSE,1"
        );
    }

    @Override
    public List<String> definePotionEffects() {
        return Arrays.asList(
                "FAST_DIGGING,2,self,continuous",
                "NIGHT_VISION,1,self,continuous"
        );
    }

    @Override
    public String defineDropWeight() {
        return "unique";
    }

}
