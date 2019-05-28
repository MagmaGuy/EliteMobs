package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class LuckyCharmsConfig extends CustomLootConfigFields {
    public LuckyCharmsConfig() {
        super("lucky_charms",
                true,
                Material.COOKIE.toString(),
                "&bLucky Charms",
                Arrays.asList("&aPart of a complete breakfast!", "&cNote: Absolutely not a part of", "&ca complete breakfast."),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("LUCK,1,self,continuous"),
                "1",
                null,
                "custom");
    }
}
