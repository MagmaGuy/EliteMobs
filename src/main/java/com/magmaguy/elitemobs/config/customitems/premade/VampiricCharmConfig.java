package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class VampiricCharmConfig extends CustomItemsConfigFields {
    public VampiricCharmConfig() {
        super("vampiric_charm",
                true,
                Material.STICK,
                "&bVampiric Charm",
                Arrays.asList("&aNo one can remember where this", "&acame from, and no one can,", "&aforget how dangerous it is..."));
        setEnchantments(Arrays.asList("VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("HEAL,0,self,onHit", "BLINDNESS,0,self,onHit", "SLOW,0,self,onHit"));
        setDropWeight("1");
    }
}
