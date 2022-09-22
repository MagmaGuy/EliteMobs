package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;

public class FrostPalaceSanctum extends DungeonPackagerConfigFields {
    public FrostPalaceSanctum() {
        super("frost_palace_sanctum",
                false,
                "Frost Palace",
                Arrays.asList("&fA 7 phase fight against",
                        "the queen of ice!",
                        "&6Credits: MagmaGuy, Delio"),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_frost_queen",
                World.Environment.NORMAL,
                true,
                "em_id_frost_queen,195.5,-31,-51.5,90,30",
                "em_id_frost_queen,183.5,-58,-51.5,90.0,0.0",
                0,
                "Difficulty: &45-man hard content!",
                "&bYou are stepping into the Frost Palace! Stay frosty!",
                "&bYou have left the Frost Palace!",
                List.of("filename=frost_palace_frost_queen_p0.yml"),
                "frost_palace",
                50);
    }
}
