package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class SlowpokeCharmConfig extends CustomLootConfigFields {
    public SlowpokeCharmConfig() {
        super("slowpoke_charm",
                true,
                Material.CARROT_ON_A_STICK.toString(),
                "&bSlowpoke Charm",
                Arrays.asList("&aLet's just hope it doesn't hit", "&aitself in its confusion..."),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("SLOW,1,self,onHit", "CONFUSION,1,target,onHit"),
                "1",
                null,
                "custom");
    }
}
