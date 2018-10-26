package com.magmaguy.elitemobs.items.uniqueitems;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class DwarvenGreed extends UniqueItem {
    @Override
    public String definePath() {
        return "Dwarven Greed";
    }

    @Override
    public String defineType() {
        return Material.DIAMOND_PICKAXE.toString();
    }

    @Override
    public String defineName() {
        return "&4Dwarven Greed";
    }

    @Override
    public List<String> defineLore() {
        return Arrays.asList(
                "&cThose who delve too greedily",
                "&cand too deep may wake ancient",
                "&chorrors of shadow and flame",
                "&cbest left undisturbed."
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

    @Override
    public String defineScalability() {
        return "dynamic";
    }

}
