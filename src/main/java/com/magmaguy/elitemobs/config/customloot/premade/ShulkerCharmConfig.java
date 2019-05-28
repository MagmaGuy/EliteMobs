package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ShulkerCharmConfig extends CustomLootConfigFields {
    public ShulkerCharmConfig() {
        super("shulker_charm",
                true,
                Material.FEATHER.toString(),
                "&bShulker Charm",
                Arrays.asList("&aI believe I can fly", "&aI believe I can touch the sky", "&aAnd you're coming with me!"),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("LEVITATION,1,self,onHit", "LEVITATION,2,target,onHit"),
                "1",
                null,
                "custom");
    }
}
