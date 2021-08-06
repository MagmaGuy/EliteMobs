package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class SlowpokeCharmConfig extends CustomItemsConfigFields {
    public SlowpokeCharmConfig() {
        super("slowpoke_charm",
                true,
                Material.CARROT_ON_A_STICK,
                "&bSlowpoke Charm",
                Arrays.asList("&aLet's just hope it doesn't hit", "&aitself in its confusion..."));
        setEnchantments(Arrays.asList("VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("SLOW,0,self,onHit", "CONFUSION,0,target,onHit"));
        setDropWeight("1");
    }
}
