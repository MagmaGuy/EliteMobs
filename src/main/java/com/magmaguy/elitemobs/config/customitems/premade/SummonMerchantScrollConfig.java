package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class SummonMerchantScrollConfig extends CustomItemsConfigFields {
    public SummonMerchantScrollConfig() {
        super("summon_merchant_scroll",
                true,
                Material.PAPER,
                "&6Summon Merchant Scroll",
                new ArrayList<>(List.of("&aNeed to sell an item?", "&aRight-click to activate", "&aor yell &9Jeeves!")));
        setEnchantments(List.of("SUMMON_MERCHANT,1"));
        setDropWeight("5");
    }
}
