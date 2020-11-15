package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class SoulbindConfig extends EnchantmentsConfigFields {
    public SoulbindConfig() {
        super("soulbind",
                true,
                "Soulbind",
                1,
                10);
        super.getAdditionalConfigOptions().put("loreStrings", "&7Soulbound to &f$player");
        super.getAdditionalConfigOptions().put("hologramString", "$player&f's");
    }
}
