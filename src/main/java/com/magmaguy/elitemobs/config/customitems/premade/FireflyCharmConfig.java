package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class FireflyCharmConfig extends CustomItemsConfigFields {
    public FireflyCharmConfig() {
        super("firefly_charm",
                true,
                Material.GLASS_BOTTLE,
                "&bFirefly Charm",
                Arrays.asList("&aLight up the night!"));
        setEnchantments(Arrays.asList("VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("NIGHT_VISION,0,self,continuous", "GLOWING,0,target,onHit"));
        setDropWeight("1");
    }
}
