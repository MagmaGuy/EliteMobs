package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class ColosseumLair extends DungeonPackagerConfigFields {
    public ColosseumLair() {
        super("colosseum_lair",
                false,
                "&6The Colosseum",
                Arrays.asList("&fFeaturing the first true World boss, first",
                        "&fmulti-phased battle, first mounted boss,",
                        "&ffirst disguised boss... a truly epic fight!",
                        "&6Credits: MagmaGuy & Maldini"),
                Arrays.asList("colosseum_tier_70_boss_p1.yml:-0.5,-1.5,47.5",
                        "colosseum_tier_70_gladiator.yml:5.5,-7.5,22.5",
                        "colosseum_tier_70_gladiator.yml:1.5,-7.5,22.5",
                        "colosseum_tier_70_gladiator.yml:0.5,-7.5,30.5",
                        "colosseum_tier_70_gladiator.yml:-5.5,-7.5,41.5",
                        "colosseum_tier_70_gladiator.yml:1.5,-7.5,41.5",
                        "colosseum_tier_70_gladiator.yml:-11.5,-7.5,41.5",
                        "colosseum_tier_70_gladiator.yml:-14.5,-7.5,23.5",
                        "colosseum_tier_70_gladiator.yml:-30.5,-7.5,31.5",
                        "colosseum_tier_70_gladiator.yml:-30.5,-7.5,31.5"),
                new ArrayList<>(),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_colosseum.schem",
                true,
                new Vector(40, -14, 0),
                new Vector(-41, 31, 88),
                "0,0,0,0,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&cA true challenge for big groups of players!",
                "&8[EM] &6Champions and challengers prepare! &eProve your worth against the most mighty of foes!",
                "&8[EM] &6Farewell traveller! &eReturn soon to show your might once more!",
                SchematicPackage.SchematicRotation.SOUTH.toString(),
                "the_colosseum");
    }
}
