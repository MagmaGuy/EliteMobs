package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class BerserkerCharmConfig extends CustomItemsConfigFields {
    public BerserkerCharmConfig() {
        super("berserker_charm",
                true,
                Material.TOTEM_OF_UNDYING,
                "&bBerserker Charm",
                Arrays.asList("&aLose yourself in the face of", "&aoverwhelming adversity in more", "&aways than one..."));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("BLINDNESS,0,self,onHit", "FAST_DIGGING,0,self,onHit"));
        setDropWeight("1");
    }
}
