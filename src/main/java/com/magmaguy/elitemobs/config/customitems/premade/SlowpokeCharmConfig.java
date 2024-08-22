package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class SlowpokeCharmConfig extends CustomItemsConfigFields {
    public SlowpokeCharmConfig() {
        super("slowpoke_charm",
                true,
                Material.CARROT_ON_A_STICK,
                "&bSlowpoke Charm",
                new ArrayList<>(List.of("&aLet's just hope it doesn't hit", "&aitself in its confusion...")));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(new ArrayList<>(List.of("SLOW,0,self,onHit", "CONFUSION,0,target,onHit")));
        setDropWeight("1");
    }
}
