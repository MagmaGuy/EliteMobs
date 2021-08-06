package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ShulkerCharmConfig extends CustomItemsConfigFields {
    public ShulkerCharmConfig() {
        super("shulker_charm",
                true,
                Material.FEATHER,
                "&bShulker Charm",
                Arrays.asList("&aI believe I can fly", "&aI believe I can touch the sky", "&aAnd you're coming with me!"));
        setEnchantments(Arrays.asList("VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("LEVITATION,0,self,onHit", "LEVITATION,1,target,onHit"));
        setDropWeight("1");
    }
}
