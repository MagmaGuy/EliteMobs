package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class ShadowOfTheBinderOfWorldsLair extends DungeonPackagerConfigFields {
    public ShadowOfTheBinderOfWorldsLair() {
        super("shadow_of_the_binder_of_worlds_lair",
                true,
                "&2[lvl 200] &5Shadow of the Binder Of Worlds",
                new ArrayList<>(List.of("&5The penultimate challenge. Be prepared!",
                        "&6Credits: MagmaGuy, 69OzCanOfBepis & Frostcone")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_shadow_of_the_binder_of_worlds",
                World.Environment.THE_END,
                true,
                "em_shadow_of_the_binder_of_worlds,-42.5,63.2,-19.5,-65,0",
                0,
                "Difficulty: &6Nightmare\n" +
                        "$bossCount level $highestTier final boss!\n" +
                        "&5This is the hardest fight in EliteMobs!",
                "&8[EM] &5Reality unravels around you. You face the final challenge. &4You are not prepared!",
                "&8[EM] &5You have left the unravelling. Did you find what you sought? Was it worth it?",
                "the_binder_of_worlds",
                true);
    }
}
