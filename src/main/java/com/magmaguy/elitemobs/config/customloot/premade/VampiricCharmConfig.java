package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class VampiricCharmConfig extends CustomLootConfigFields {
    public VampiricCharmConfig() {
        super("vampiric_charm",
                true,
                Material.STICK.toString(),
                "&bVampiric Charm",
                Arrays.asList("&aNo one can remember where this", "&acame from, and no one can,", "&aforget how dangerous it is..."),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("HEAL,1,self,onHit", "BLINDNESS,1,self,onHit", "SLOW,1,self,onHit"),
                "1",
                null,
                "custom");
    }
}
