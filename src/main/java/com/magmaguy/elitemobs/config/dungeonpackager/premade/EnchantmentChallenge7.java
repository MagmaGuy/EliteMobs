package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class EnchantmentChallenge7 extends DungeonPackagerConfigFields {
    public EnchantmentChallenge7() {
        super("enchantment_challenge_7_sanctum",
                true,
                "&2[lvl 000-200] &fEnchantment Challenge 07",
                List.of("&fAn enchantment challenge dungeon!"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_enchantment_challenge_7",
                World.Environment.NORMAL,
                true,
                "em_id_enchantment_challenge_7,18.5,95,-15.5,45,0.0",
                "em_id_enchantment_challenge_7,14.5,65,-12.5,45,0",
                0,
                "Difficulty: &4solo hard content!",
                "&bChallenge time!",
                "&bYou have left the enchantment challenge!",
                List.of("filename=enchantment_boss_rock_solid_cold.yml"),
                "em_id_enchantment_challenge_7",
                1,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "id", 0)));
        setEnchantmentChallenge(true);
        setMaxPlayerCount(1);
    }
}
