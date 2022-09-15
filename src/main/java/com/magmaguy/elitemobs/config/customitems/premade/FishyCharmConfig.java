package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class FishyCharmConfig extends CustomItemsConfigFields {
    public FishyCharmConfig() {
        super("fishy_charm",
                true,
                Material.COD,
                "&bFishy Charm",
                Arrays.asList("&aThere's just something not", "&aquite right with this one..."));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(List.of("WATER_BREATHING,0,self,continuous"));
        setDropWeight("1");
    }
}
