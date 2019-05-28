package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class SalamanderCharmConfig extends CustomLootConfigFields {
    public SalamanderCharmConfig() {
        super("salamander_charm",
                true,
                Material.LEATHER.toString(),
                "&bSalamander Charm",
                Arrays.asList("&aAlmost as hot as MagmaGuy's", "&anew mixtape!"),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("LUCK,1,self,continuous"),
                "1",
                null,
                "custom");
    }
}
