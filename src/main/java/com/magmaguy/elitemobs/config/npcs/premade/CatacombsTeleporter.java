package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class CatacombsTeleporter extends NPCsConfigFields {
    public CatacombsTeleporter() {
        super("catacombs_teleporter",
                true,
                "Crypt Keeper",
                "<Catacombs Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,283.5,92,283.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp catacombs_lair.yml");
    }
}
