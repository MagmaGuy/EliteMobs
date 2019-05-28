package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class FishyCharmConfig extends CustomLootConfigFields {
    public FishyCharmConfig() {
        super("fishy_charm",
                true,
                Material.COD.toString(),
                "&bFishy Charm",
                Arrays.asList("&aThere's just something not", "&aquite right with this one..."),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("WATER_BREATHING,1,self,continuous"),
                "1",
                null,
                "custom");
    }
}
