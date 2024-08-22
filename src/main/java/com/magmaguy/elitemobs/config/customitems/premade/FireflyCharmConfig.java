package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class FireflyCharmConfig extends CustomItemsConfigFields {
    public FireflyCharmConfig() {
        super("firefly_charm",
                true,
                Material.GLASS_BOTTLE,
                "&bFirefly Charm",
                List.of("&aLight up the night!"));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(new ArrayList<>(List.of("NIGHT_VISION,0,self,continuous", "GLOWING,0,target,onHit")));
        setDropWeight("1");
    }
}
