package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class PirateShipMinidungeon extends DungeonPackagerConfigFields {
    public PirateShipMinidungeon() {
        super("pirate_ship_minidungeon",
                false,
                "&6The Pirate Ship",
                DungeonLocationType.SCHEMATIC,
                Arrays.asList("&fA fun, challenging minidungeon full of",
                        "&fbosses made for players starting to get good,",
                        "&fat EliteMobs!",
                        "&6Credits: MagmaGuy & Realm of Lotheridon"),
                Arrays.asList(
                        "pirate_ship_tier_10.yml:-2.5,2.5,48.5",
                        "pirate_ship_tier_10.yml:0.5,2.5,47.5",
                        "pirate_ship_tier_10.yml:-0.5,2.5,50.5",
                        "pirate_ship_tier_10.yml:1.5,2.5,48.5",
                        "pirate_ship_tier_15.yml:-0.5,8.5,22.5",
                        "pirate_ship_tier_15.yml:0.5,8.5,21.5",
                        "pirate_ship_tier_15.yml:0.5,8.5,22.5",
                        "pirate_ship_tier_15.yml:-0.5,8.5,22.5",
                        "pirate_ship_tier_15.yml:0.5,8.5,22.5",
                        "pirate_ship_tier_20_melee_1.yml:0.5,3.5,67.5",
                        "pirate_ship_tier_20_melee_1.yml:-0.5,3.5,67.5",
                        "pirate_ship_tier_20_melee_2.yml:9.5,3.5,76.5",
                        "pirate_ship_tier_20_melee_2.yml:9.5,3.5,77.5",
                        "pirate_ship_tier_20_melee_2.yml:8.5,3.5,77.5",
                        "pirate_ship_tier_20_melee_2.yml:8.5,3.5,76.5",
                        "pirate_ship_tier_20_melee_2.yml:9.5,3.5,76.5",
                        "pirate_ship_tier_20_melee_2.yml:8.5,3.5,77.5",
                        "pirate_ship_tier_20_miniboss.yml:-9.5,3.5,76.5",
                        "pirate_ship_tier_20_ranged.yml:0.5,8.5,66.5",
                        "pirate_ship_tier_20_ranged.yml:0.5,8.5,65.5",
                        "pirate_ship_tier_25_miniboss_1.yml:-0.5,1.5,25.5",
                        "pirate_ship_tier_30_melee.yml:-0.5,-2.5,40.5",
                        "pirate_ship_tier_30_melee.yml:0.5,-2.5,43.5",
                        "pirate_ship_tier_30_melee.yml:0.5,-2.5,46.5",
                        "pirate_ship_tier_30_melee.yml:-0.5,-2.5,48.5",
                        "pirate_ship_tier_30_melee.yml:0.5,-2.5,50.5",
                        "pirate_ship_tier_30_melee.yml:0.5,-2.5,53.5",
                        "pirate_ship_tier_30_ranged.yml:0.5,-2.5,40.5",
                        "pirate_ship_tier_30_ranged.yml:-0.5,-2.5,43.5",
                        "pirate_ship_tier_30_ranged.yml:-0.5,-2.5,45.5",
                        "pirate_ship_tier_30_ranged.yml:0.5,-2.5,47.5",
                        "pirate_ship_tier_30_ranged.yml:0.5,-2.5,50.5",
                        "pirate_ship_tier_30_miniboss_1.yml:-0.5,-2.5,31.5",
                        "pirate_ship_tier_35.yml:-0.5,13.5,84.5",
                        "pirate_ship_tier_35.yml:0.5,13.5,84.5",
                        "pirate_ship_tier_35.yml:-0.5,13.5,84.5",
                        "pirate_ship_tier_35.yml:0.5,13.5,84.5",
                        "pirate_ship_tier_35.yml:0.5,13.5,85.5",
                        "pirate_ship_tier_35.yml:0.5,13.5,84.5",
                        "pirate_ship_tier_35_miniboss_1.yml:-0.5,8.5,85.5",
                        "pirate_ship_tier_35_miniboss_2.yml:-0.5,8.5,84.5",
                        "pirate_ship_tier_40_miniboss_1.yml:13.5,9.5,78.5",
                        "pirate_ship_tier_45_boss.yml:-11.5,8.5,78.5"),
                Arrays.asList(),
                "patreon.com/magmaguy",
                DungeonSizeCategory.MINIDUNGEON,
                null,
                "elitemobs_pirate_ship_minidungeon.schem",
                null,
                true,
                new Vector(16, -6, -1),
                new Vector(-20, 58, 90),
                new Vector(-17, -2, 49),
                null,
                null,
                3,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6One of the best hunting grounds for" +
                        "&6aspiring adventurers!");
    }
}
