package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class LuckyCharmsConfig extends CustomItemsConfigFields {
    public LuckyCharmsConfig() {
        super("lucky_charms",
                true,
                Material.COOKIE,
                "&bLucky Charms",
                new ArrayList<>(List.of("&aPart of a complete breakfast!", "&cNote: Absolutely not a part of", "&ca complete breakfast.")));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(List.of("LUCK,0,self,continuous"));
        setDropWeight("1");
    }
}
