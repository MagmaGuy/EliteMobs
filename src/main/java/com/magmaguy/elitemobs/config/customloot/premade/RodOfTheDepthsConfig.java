package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class RodOfTheDepthsConfig extends CustomLootConfigFields {
    public RodOfTheDepthsConfig(){
        super("rod_of_the_depths",
                true,
                Material.FISHING_ROD.toString(),
                "&3Rod of the Depths",
                Arrays.asList("&3You want to hear about where", "&3I got this from? Sit down lad,", "&3I've got a tale for ye..."),
                Arrays.asList("LURE,3", "LUCK,1", "DURABILITY,6", "VANISHING_CURSE,1"),
                Arrays.asList("INVISIBILITY,0,self,onHit", "SLOW,0,self,onHit", "BLINDNESS,0,self,onHit"),
                "dynamic",
                "fixed",
                "unique");
    }
}
