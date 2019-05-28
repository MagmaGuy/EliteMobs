package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ElephantCharmConfig extends CustomLootConfigFields {
    public ElephantCharmConfig() {
        super("elephant_charm",
                true,
                Material.ANVIL.toString(),
                "&bElephant Charm",
                Arrays.asList("&aGain the power of an elephant", "&astampede, for the cost of the", "&aspeed of an elephant stampede!"),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("INCREASE_DAMAGE,2,self,onHit", "SLOW,4,self,onHit"),
                "1",
                null,
                "custom");
    }
}
