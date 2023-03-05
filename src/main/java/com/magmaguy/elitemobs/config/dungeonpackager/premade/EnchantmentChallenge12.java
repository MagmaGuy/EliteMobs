package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EnchantmentChallenge12 extends DungeonPackagerConfigFields {
    public EnchantmentChallenge12() {
        super("enchantment_challenge_12_sanctum",
                false,
                "Enchantment Challenge 12",
                Arrays.asList("&fAn enchantment challenge dungeon!"),
                DiscordLinks.premiumMinidungeons,
                DungeonPackagerConfigFields.DungeonSizeCategory.SANCTUM,
                "em_id_enchantment_challenge_12",
                World.Environment.THE_END,
                true,
                "em_id_enchantment_challenge_12,-15.5,94,15.5,-135,0.0",
                "em_id_enchantment_challenge_12,14.5,65,-12.5,45,0",
                0,
                "Difficulty: &4solo hard content!",
                "&bChallenge time!",
                "&bYou have left the enchantment challenge!",
                List.of("filename=enchantment_boss_mad_arcanist.yml"),
                "em_id_enchantment_challenge_12",
                1);
        setDifficulties(List.of(
                Map.of("name", "normal", "id", 0)));
        setEnchantmentChallenge(true);
        setMaxPlayerCount(1);
    }
}
