package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BinderOfWorldsSanctum extends DungeonPackagerConfigFields {
    public BinderOfWorldsSanctum() {
        super("binder_of_worlds_sanctum",
                true,
                "&2[lvl 200] &5The Binder Of Worlds",
                Arrays.asList("&5The ultimate challenge. Be prepared!",
                        "&6Credits: MagmaGuy, 69OzCanOfBepis & Frostcone"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_binder_of_worlds",
                World.Environment.THE_END,
                true,
                "em_id_binder_of_worlds,-40.5,74,87.5,-155.0,0.0",
                "em_id_binder_of_worlds,-40,62,24,-130,-11",
                0,
                "Difficulty: &6Nightmare\n" +
                        "$bossCount level $highestTier final boss!\n" +
                        "&5This is the hardest fight in EliteMobs!",
                "&8[EM] &5Reality unravels around you. You face the final challenge. &4You are not prepared!",
                "&8[EM] &5You have left the unravelling. Did you find what you sought? Was it worth it?",
                List.of("filename=em_id_binder_of_worlds_phase_1.yml"),
                "the_binder_of_worlds",
                50,
                true);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 205, "id", 0),
                Map.of("name", "hard", "levelSync", 200, "id", 1),
                Map.of("name", "mythic", "levelSync", 195, "id", 2)));
    }
}
