package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FrostPalaceSanctum extends ContentPackagesConfigFields {
    public FrostPalaceSanctum() {
        super("frost_palace_sanctum",
                true,
                "&2[lvl 050] &3The Frost Palace",
                new ArrayList<>(List.of("&fA 7 phase fight against",
                        "the queen of ice!",
                        "&6Credits: MagmaGuy, Delio")),
                "https://nightbreak.io/plugin/elitemobs/#the-frost-palace",
                DungeonSizeCategory.SANCTUM,
                "em_id_frost_palace",
                World.Environment.NORMAL,
                true,
                "em_id_frost_palace,195.5,-31,-51.5,90,30",
                "em_id_frost_palace,183.5,-58,-51.5,90.0,0.0",
                0,
                "Difficulty: &45-man hard content!",
                "&bYou are stepping into the Frost Palace! Stay frosty!",
                "&bYou have left the Frost Palace!",
                List.of("filename=frost_palace_frost_queen_p0.yml"),
                "frost_palace",
                50,
                true);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 55, "id", 0),
                Map.of("name", "hard", "levelSync", 50, "id", 1),
                Map.of("name", "mythic", "levelSync", 45, "id", 2)));
        setSetupMenuDescription(List.of(
                "&2A Sanctum for players around level 50!",
                "&2The first Sanctum we made, with customs",
                "&2models and a soundtrack!"));
    }
}
