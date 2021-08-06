package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class CheetahCharmConfig extends CustomItemsConfigFields {
    public CheetahCharmConfig() {
        super("cheetah_charm",
                true,
                Material.GOLDEN_BOOTS,
                "&bCheetah Charm",
                Arrays.asList("&aCheetahs can reach speeds of", "&a120 km/h... for about 60", "&aseconds. Make them count!"));
        setEnchantments(Arrays.asList("VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("SPEED,2,self,continuous", "HUNGER,0,self,continuous"));
        setDropWeight("1");
    }
}
