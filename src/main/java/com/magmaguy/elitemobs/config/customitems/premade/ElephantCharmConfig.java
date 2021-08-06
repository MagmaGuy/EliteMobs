package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ElephantCharmConfig extends CustomItemsConfigFields {
    public ElephantCharmConfig() {
        super("elephant_charm",
                true,
                Material.ANVIL,
                "&bElephant Charm",
                Arrays.asList("&aGain the power of an elephant", "&astampede, for the cost of the", "&aspeed of an elephant stampede!"));
        setEnchantments(Arrays.asList("VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("INCREASE_DAMAGE,1,self,onHit", "SLOW,3,self,onHit"));
        setDropWeight("1");
    }
}
