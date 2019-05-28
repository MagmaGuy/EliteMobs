package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class CheetahCharmConfig extends CustomLootConfigFields {
    public CheetahCharmConfig() {
        super("cheetah_charm",
                true,
                Material.GOLDEN_BOOTS.toString(),
                "&bCheetah Charm",
                Arrays.asList("&aCheetahs can reach speeds of", "&a120 km/h... for about 60", "&aseconds. Make them count!"),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("SPEED,3,self,continuous", "HUNGER,1,self,continuous"),
                "1",
                null,
                "custom");
    }
}
