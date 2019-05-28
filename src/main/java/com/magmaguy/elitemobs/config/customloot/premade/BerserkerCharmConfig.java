package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class BerserkerCharmConfig extends CustomLootConfigFields {
    public BerserkerCharmConfig() {
        super("berserker_charm",
                true,
                Material.TOTEM_OF_UNDYING.toString(),
                "&bBerserker Charm",
                Arrays.asList("&aLose yourself in the face of", "&aoverwhelming adversity in more", "&aways than one..."),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("BLINDNESS,1,self,onHit", "FAST_DIGGING,1,self,onHit"),
                "1",
                null,
                "custom");
    }
}
