package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PrimisBloodTempleSanctum extends DungeonPackagerConfigFields {
    public PrimisBloodTempleSanctum() {
        super("primis_blood_temple_sanctum",
                true,
                "&2[lvl 020] &aPrimis - Blood Temple'",
                Arrays.asList("&fThe Fire Elemental awaits!",
                        "&6Credits: Dali_, Frostcone, MagmaGuy"),
                DiscordLinks.premiumMinidungeons,
                DungeonPackagerConfigFields.DungeonSizeCategory.SANCTUM,
                "em_id_bloodtemple",
                World.Environment.NORMAL,
                true,
                "em_id_bloodtemple,326.5,87.0,-738.5,-44,35",
                "em_id_bloodtemple,324.5,73.0,-711.5,-90,5",
                0,
                "Difficulty: &4solo hard content!",
                "&bWelcome to the Blood Temple!",
                "&bYou have left the Blood Temple!",
                List.of("filename=primis_final_elemental_p1.yml"),
                "em_id_bloodtemple",
                20,
                true);
        setDifficulties(List.of(
                Map.of("name", "normal", "id", 0)));
        setListedInTeleports(false);
    }
}
