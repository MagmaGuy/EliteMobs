package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class RabbitCharmConfig extends CustomLootConfigFields {
    public RabbitCharmConfig() {
        super("rabbit_charm",
                true,
                Material.RABBIT_FOOT.toString(),
                "&bRabbit Charm",
                Arrays.asList("&aWith this charm, just about", "&aany destination is only a hop,", "&askip and a jump away!"),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("JUMP,3,self,continuous"),
                "1",
                null,
                "custom");
    }
}
