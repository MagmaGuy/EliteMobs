package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OasisPyramidSanctum extends DungeonPackagerConfigFields {
    public OasisPyramidSanctum() {
        super("oasis_pyramid_sanctum",
                true,
                "&2[lvl 055] &6The Oasis Pyramid",
                new ArrayList<>(List.of("&fThe final dungeon of the Oasis adventure!",
                        "&6Credits: 69OzCanOfBepis, Frostcone, MagmaGuy")),
                DiscordLinks.premiumMinidungeons,
                DungeonPackagerConfigFields.DungeonSizeCategory.SANCTUM,
                "em_id_oasis_pyramid",
                World.Environment.NORMAL,
                true,
                "em_id_oasis_pyramid,-9.5,77.0,-153.5,-115,17",
                "em_id_oasis_pyramid,0.5,75.0,-169.5,-180,3",
                0,
                "Difficulty: &4solo hard content!",
                "&bWelcome to the Pyramid!",
                "&bYou have left the Pyramid!",
                List.of("filename=oasis_pharaoh_p1.yml"),
                "em_id_oasis_pyramid",
                55,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "id", 0)));
        setListedInTeleports(false);
    }
}
