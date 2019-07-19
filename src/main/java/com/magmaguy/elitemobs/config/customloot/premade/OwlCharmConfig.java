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
                Arrays.asList("GLOWING,0,target,onHit", "JUMP,2,self,continuous", "NIGHT_VISION,0,self,continuous", "WEAKNESS,0,self,continuous"),
                "1",
                null,
                "custom");
    }
}
