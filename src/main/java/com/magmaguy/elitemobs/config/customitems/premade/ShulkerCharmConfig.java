package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ShulkerCharmConfig extends CustomItemsConfigFields {
    public ShulkerCharmConfig() {
        super("shulker_charm",
                true,
                Material.FEATHER,
                "&bShulker Charm",
                new ArrayList<>(List.of("&aI believe I can fly", "&aI believe I can touch the sky", "&aAnd you're coming with me!")));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(new ArrayList<>(List.of("LEVITATION,0,self,onHit", "LEVITATION,1,target,onHit")));
        setDropWeight("1");
    }
}
