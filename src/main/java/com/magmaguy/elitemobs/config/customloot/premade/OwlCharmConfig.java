package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class OwlCharmConfig extends CustomLootConfigFields {
    public OwlCharmConfig() {
        super("owl_charm",
                true,
                Material.ELYTRA.toString(),
                "&bOwl Charm",
                Arrays.asList("&aBecome the ultimate nocturnal", "&apredator!"),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("GLOWING,1,target,onHit", "JUMP,3,self,continuous", "NIGHT_VISION,1,self,continuous", "WEAKNESS,1,self,continuous"),
                "1",
                null,
                "custom");
    }
}
