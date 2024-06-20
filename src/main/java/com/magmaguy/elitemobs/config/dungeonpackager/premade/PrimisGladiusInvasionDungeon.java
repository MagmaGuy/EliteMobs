package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PrimisGladiusInvasionDungeon extends DungeonPackagerConfigFields {
    public PrimisGladiusInvasionDungeon() {
        super("primis_gladius_invasion_dungeon",
                true,
                "&2[lvl 000-020] &aPrimis - Gladius Invasion",
                Arrays.asList("&fIt is time to take Gladius back!", "&6Credits: 69OzCanOfBepis, Frostcone, MagmaGuy"),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.DUNGEON,
                "em_id_primis_gladius",
                World.Environment.NETHER,
                true,
                "em_id_primis_gladius,-108.5,69.0,40.5,-90.499999,0",
                "em_id_primis_gladius,-100.5,69.0,40.5,-69.499999,0",
                0,
                "Difficulty: &4solo hard content!",
                "&bWelcome to the Gladius Invasion!",
                "2&bYou have left the Gladius Invasion!",
                List.of("filename=primis_gladius_bell_id.yml"),
                "em_id_primis_gladius",
                1,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "id", 0, "levelSync", 18),
                Map.of("name", "hard", "id", 1, "levelSync", 15),
                Map.of("name", "mythic", "id", 2, "levelSync", 12)
        ));
        setMaxPlayerCount(5);
    }
}
