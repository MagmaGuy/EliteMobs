package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class BerserkerCharmConfig extends CustomItemsConfigFields {
    public BerserkerCharmConfig() {
        super("berserker_charm",
                true,
                Material.TOTEM_OF_UNDYING,
                "&bBerserker Charm",
                new ArrayList<>(List.of("&aLose yourself in the face of", "&aoverwhelming adversity in more", "&aways than one...")));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(new ArrayList<>(List.of("BLINDNESS,0,self,onHit", "FAST_DIGGING,0,self,onHit")));
        setDropWeight("1");
    }
}
