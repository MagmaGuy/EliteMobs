package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class SalamanderCharmConfig extends CustomItemsConfigFields {
    public SalamanderCharmConfig() {
        super("salamander_charm",
                true,
                Material.LEATHER,
                "&bSalamander Charm",
                Arrays.asList("&aAlmost as hot as MagmaGuy's", "&anew mixtape!"));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(List.of("FIRE_RESISTANCE,0,self,continuous"));
        setDropWeight("1");
    }
}
