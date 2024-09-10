package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CraftenminesLabSanctum extends ContentPackagesConfigFields {
    public CraftenminesLabSanctum() {
        super("craftenmines_lab_sanctum",
                true,
                "&2[lvl 030] &aCraftenmines Sanctum",
                new ArrayList<>(List.of("&aFace the creator of the devious creations!",
                        "&6Credits: MagmaGuy, 69OzCanOfBepis")),
                "https://nightbreak.io/plugin/elitemobs/#craftenmines-laboratory",
                DungeonSizeCategory.SANCTUM,
                "em_id_craftenmines_lab",
                World.Environment.NORMAL,
                true,
                "em_id_craftenmines_lab,-26.5,68,0.5,-90.0,0.0",
                "em_id_craftenmines_lab,-19,66,0,-90,0",
                0,
                "Difficulty: &6Hard\n" +
                        "Difficulty: &45-man hard content!",
                "&8[EM] &5Someone must stop Dr. Craftenmine and his horrific creations!",
                "&8[EM] &5You have left Dr. Craftenmine's laboratory!",
                List.of("filename=dr_craftenmine_p1.yml"),
                "em_id_craftenmines_lab",
                30,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 25, "id", 0),
                Map.of("name", "hard", "levelSync", 30, "id", 1),
                Map.of("name", "mythic", "levelSync", 35, "id", 2)));
        setSetupMenuDescription(List.of(
                "&2A crazy Sanctum for players around level 30!",
                "Includes custom models and a custom soundtrack!"));
    }
}
