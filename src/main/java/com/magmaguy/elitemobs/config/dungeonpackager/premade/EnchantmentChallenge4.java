package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class EnchantmentChallenge4 extends DungeonPackagerConfigFields {
    public EnchantmentChallenge4() {
        super("enchantment_challenge_4_sanctum",
                false,
                "&2[lvl 000-200] &fEnchantment Challenge 04",
                List.of("&fAn enchantment challenge dungeon!"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_enchantment_challenge_4",
                World.Environment.NORMAL,
                true,
                "em_id_enchantment_challenge_4,18.5,95,-15.5,45,0.0",
                "em_id_enchantment_challenge_4,14.5,65,-12.5,45,0",
                0,
                "Difficulty: &4solo hard content!",
                "&bChallenge time!",
                "&bYou have left the enchantment challenge!",
                List.of("filename=enchantment_boss_leet_summoner.yml"),
                "em_id_enchantment_challenge_4",
                1);
        setDifficulties(List.of(
                Map.of("name", "normal", "id", 0)));
        setEnchantmentChallenge(true);
        setMaxPlayerCount(1);
    }
}
