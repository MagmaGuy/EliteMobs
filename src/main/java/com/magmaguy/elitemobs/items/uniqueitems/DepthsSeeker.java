package com.magmaguy.elitemobs.items.uniqueitems;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class DepthsSeeker extends UniqueItem{

    @Override
    public String definePath() {
        return "Depths Seeker";
    }

    @Override
    public String defineType() {
        return Material.FISHING_ROD.toString();
    }

    @Override
    public String defineName() {
        return "&9Depths Seeker";
    }

    @Override
    public List<String> defineLore() {
        return Arrays.asList(
                "&9Come from depths immeasurable",
                "&9and looted from monster most vile,",
                "&9there is no telling what horrors",
                "&9this fishing rod has seen."
        );
    }

    @Override
    public List<String> defineEnchantments() {
        return Arrays.asList(
                "LURE,3",
                "LUCK,3",
                "DURABILITY,10",
                "FIRE_ASPECT,1",
                "VANISHING_CURSE,1"
        );
    }

    @Override
    public List<String> definePotionEffects() {
        return Arrays.asList(
                "WATER_BREATHING,1,self,continuous",
                "LUCK,1,self,continuous"
        );
    }

    @Override
    public String defineDropWeight() {
        return "unique";
    }

    @Override
    public String defineScalability() {
        return "limited";
    }

}
