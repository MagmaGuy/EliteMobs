package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class UnbindScrollConfig extends CustomLootConfigFields {
    public UnbindScrollConfig() {
        super("unbind_scroll",
                true,
                Material.PAPER.toString(),
                "&5Unbind Scroll",
                Arrays.asList("&5Allows users to unbind one",
                        "&5soulbound item at an anvil!",
                        "&5Use wisely!"),
                Arrays.asList("UNBIND,1"),
                null,
                null,
                null,
                "unique");
    }
}
