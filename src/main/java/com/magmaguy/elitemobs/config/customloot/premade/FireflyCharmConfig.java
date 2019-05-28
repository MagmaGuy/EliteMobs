package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class FireflyCharmConfig extends CustomLootConfigFields {
    public FireflyCharmConfig() {
        super("firefly_charm",
                true,
                Material.GLASS_BOTTLE.toString(),
                "&bFirefly Charm",
                Arrays.asList("&aLight up the night!"),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("NIGHT_VISION,1,self,continuous", "GLOWING,1,target,onHit"),
                "1",
                null,
                "custom");
    }
}
