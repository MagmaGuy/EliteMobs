package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class BinderOfWorldsLair extends DungeonPackagerConfigFields {
    public BinderOfWorldsLair() {
        super("binder_of_worlds_lair",
                false,
                "&5Binder Of Worlds",
                Arrays.asList("&5The ultimate challenge. Be prepared!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_binder_of_worlds",
                World.Environment.THE_END,
                true,
                "em_binder_of_worlds,-40,62,24,-130,-11",
                0,
                "Difficulty: &6Nightmare\n" +
                        "$bossCount level $highestTier final boss!\n" +
                        "&5This is the hardest fight in EliteMobs!",
                "&8[EM] &5Reality unravels around you. You face the final challenge. &4You are not prepared!",
                "&8[EM] &5You have left the unravelling. Did you find what you sought? Was it worth it?",
                "the_binder_of_worlds");
    }
}
