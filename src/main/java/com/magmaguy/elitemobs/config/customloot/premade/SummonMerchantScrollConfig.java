package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class SummonMerchantScrollConfig extends CustomLootConfigFields {
    public SummonMerchantScrollConfig() {
        super("summon_merchant_scroll",
                true,
                Material.PAPER.toString(),
                "&6Summon Merchant Scroll",
                Arrays.asList("&aNeed to sell an item?", "&aRight-click to activate", "&aor yell &9Jeeves!"),
                Arrays.asList("SUMMON_MERCHANT,1"),
                null,
                "2",
                null,
                "custom");
    }
}
