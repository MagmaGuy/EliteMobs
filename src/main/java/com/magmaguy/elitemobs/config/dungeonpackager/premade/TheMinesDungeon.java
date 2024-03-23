package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TheMinesDungeon extends DungeonPackagerConfigFields {
    public TheMinesDungeon() {
        super("the_mines_dungeon",
                false,
                "&2[lvl 020] &3The Mines Dungeon",
                Arrays.asList("&fReady to step up your dungeon game?",
                        "&6Credits: MagmaGuy, Frostcone, 69OzCanOfBepis, Realm of Lotheridon, Dali_"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.DUNGEON,
                "em_id_the_mines",
                World.Environment.NORMAL,
                true,
                "em_id_the_mines,250.5,116,-113.5,-42.0,7.0",
                "em_id_the_mines,256.5,116,-106.5,-29,8",
                0,
                "Difficulty: &45-man hard content!",
                "&bYou''ve been asked to investigate what lurks in the mines. Find your way to the depths of the mine!",
                "&bYou have left The Mines!",
                List.of("filename=the_mines_soulweaver_daine_p0.yml",
                        "filename=the_mines_boss_soulfire_p1.yml",
                        "filename=the_mines_mini_boss_tulpa.yml",
                        "filename=the_mines_mini_boss_forger.yml",
                        "filename=the_mines_mini_boss_tusk.yml"),
                "em_id_the_mines",
                20);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 25, "id", 0),
                Map.of("name", "hard", "levelSync", 20, "id", 1),
                Map.of("name", "mythic", "levelSync", 15, "id", 2)));
    }
}
