package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class TreasureGoblinConfig extends CustomBossConfigFields {

    public TreasureGoblinConfig() {
        super("treasure_goblin",
                EntityType.ZOMBIE.toString(),
                true,
                "&eTreasure Goblin",
                "dynamic",
                30,
                true,
                4,
                4,
                Material.GOLDEN_HELMET,
                Material.GOLDEN_CHESTPLATE,
                Material.GOLDEN_LEGGINGS,
                Material.GOLDEN_BOOTS,
                null,
                null,
                true,
                Arrays.asList("gold_explosion.yml", "gold_shotgun.yml", "hyper_loot.yml", "spirit_walk.yml"),
                "&cA Treasure Goblin has been sighted!",
                "&aA Treasure Goblin has been slain by $players!",
                "&4A Treasure Goblin has escaped!",
                "&cTreasure Goblin: $location",
                Arrays.asList(),
                true,
                true,
                Arrays.asList(Material.GOLD_NUGGET.toString()),
                null,
                null);
    }

}
