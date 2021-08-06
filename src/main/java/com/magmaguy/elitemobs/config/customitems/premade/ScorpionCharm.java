package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ScorpionCharm extends CustomItemsConfigFields {
    public ScorpionCharm() {
        super("scorpion_charm",
                true,
                Material.POISONOUS_POTATO,
                "&bScorpion Charm",
                Arrays.asList("&aFloat like a butterfly,", "&asting like a bee,", "&apoison like a potato!"));
        setEnchantments(Arrays.asList("VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("POISON,0,target,onHit"));
        setDropWeight("1");
    }
}
