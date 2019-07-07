package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class DwarvenGreedConfig extends CustomLootConfigFields {
    public DwarvenGreedConfig() {
        super("dwarven_greed",
                true,
                Material.WOODEN_PICKAXE.toString(),
                "&4Dwarven Greed",
                Arrays.asList("&cThose who delve too greedily", "&cand too deep may wake ancient", "&chorrors of shadow and flame", "&cbest left undisturbed."),
                Arrays.asList("LOOT_BONUS_BLOCKS,4", "DURABILITY,6", "DIG_SPEED,6", "VANISHING_CURSE,1"),
                Arrays.asList("FAST_DIGGING,2,self,continuous", "NIGHT_VISION,1,self,continuous"),
                "dynamic",
                "limited",
                "unique");
    }
}
