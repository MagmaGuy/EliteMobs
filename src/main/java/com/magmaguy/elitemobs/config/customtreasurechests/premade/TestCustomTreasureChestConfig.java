package com.magmaguy.elitemobs.config.customtreasurechests.premade;

import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.Arrays;

public class TestCustomTreasureChestConfig extends CustomTreasureChestConfigFields {

    public TestCustomTreasureChestConfig() {
        super("test_custom_treasure_config",
                true,
                Material.CHEST,
                BlockFace.NORTH,
                0,
                null,
                TreasureChest.DropStyle.SINGLE,
                1,
                Arrays.asList("magmaguys_toothpick.yml:1"),
                0.5,
                Arrays.asList("test_boss.yml:1"),
                0,
                null,
                Arrays.asList("BARRIER"));
    }

}
