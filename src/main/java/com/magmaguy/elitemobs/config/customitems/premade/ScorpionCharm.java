package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ScorpionCharm extends CustomItemsConfigFields {
    public ScorpionCharm() {
        super("scorpion_charm",
                true,
                Material.POISONOUS_POTATO,
                "&bScorpion Charm",
                new ArrayList<>(List.of("&aFloat like a butterfly,", "&asting like a bee,", "&apoison like a potato!")));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(List.of("POISON,0,target,onHit"));
        setDropWeight("1");
    }
}
