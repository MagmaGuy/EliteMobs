package com.magmaguy.elitemobs.config.customtreasurechests.premade;

import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class TestCustomTreasureChestConfig extends CustomTreasureChestConfigFields {

    public TestCustomTreasureChestConfig() {
        super("test_custom_treasure_config",
                true,
                Material.CHEST.toString(),
                "NORTH",
                0,
                null,
                "single",
                1,
                Arrays.asList("magmaguys_toothpick.yml:1"),
                0.5,
                Arrays.asList("test_boss.yml"),
                0,
                null,
                Arrays.asList("BARRIER"));
    }

}
