package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class DepthsSeekerConfig extends CustomLootConfigFields {
    public DepthsSeekerConfig() {
        super("depths_seeker",
                true,
                Material.FISHING_ROD.toString(),
                "&2Depths seeker",
                Arrays.asList("&9Come from depths immeasurable", "&9and looted from monster most vile,", "&9there is no telling what horrors", "&9this fishing rod has seen."),
                Arrays.asList("LURE,3", "LUCK,3", "DURABILITY,10", "FIRE_ASPECT,1", "VANISHING_CURSE,1"),
                Arrays.asList("WATER_BREATHING,1,self,continuous", "LUCK,1,self,continuous"),
                "dynamic",
                "limited",
                "unique");
    }
}
