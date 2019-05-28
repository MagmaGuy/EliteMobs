package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ChameleonCharmConfig extends CustomLootConfigFields {
    public ChameleonCharmConfig() {
        super("chameleon_charm",
                true,
                Material.LEATHER.toString(),
                "&bChameleon Charm",
                Arrays.asList("&aThe colorful Chameleon can", "&ablend in just about anywhere,", "&aslowly creeping around..."),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("INVISIBILITY,1,self,onHit", "SLOW,2,self,onHit", "BLINDNESS,1,self,onHit"),
                "1",
                null,
                "custom");
    }
}
