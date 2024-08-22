package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CheetahCharmConfig extends CustomItemsConfigFields {
    public CheetahCharmConfig() {
        super("cheetah_charm",
                true,
                Material.GOLDEN_BOOTS,
                "&bCheetah Charm",
                new ArrayList<>(List.of("&aCheetahs can reach speeds of", "&a120 km/h... for about 60", "&aseconds. Make them count!")));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(new ArrayList<>(List.of("SPEED,2,self,continuous", "HUNGER,0,self,continuous")));
        setDropWeight("1");
    }
}
