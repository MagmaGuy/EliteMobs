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
                "$eventBossLevel &eTreasure Goblin",
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
                Arrays.asList(
                        "&e&l---------------------------------------------",
                        "&eThe Treasure Goblin has been pillaged!",
                        "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                        "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                        "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                        "&aSlayers: $players",
                        "&e&l---------------------------------------------"),
                "&4A Treasure Goblin has escaped!",
                "&cTreasure Goblin: $distance blocks away!",
                Arrays.asList(),
                true,
                true,
                Arrays.asList(Material.GOLD_NUGGET.toString()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                2);
    }

}
