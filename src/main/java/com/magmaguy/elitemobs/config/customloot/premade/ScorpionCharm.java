package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ScorpionCharm extends CustomLootConfigFields {
    public ScorpionCharm() {
        super("scorpion_charm",
                true,
                Material.POISONOUS_POTATO.toString(),
                "&bScorpion Charm",
                Arrays.asList("&aFloat like a butterfly,", "&asting like a bee,", "&apoison like a potato!"),
                Arrays.asList("VANISHING_CURSE,1"),
                Arrays.asList("POISON,1,target,onHit"),
                "1",
                null,
                "custom");
    }
}
