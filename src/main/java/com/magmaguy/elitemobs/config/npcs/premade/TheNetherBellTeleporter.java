package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheNetherBellTeleporter extends NPCsConfigFields {
    public TheNetherBellTeleporter() {
        super("the_nether_bell_teleporter",
                true,
                "Ancient Corpse",
                "<[50] The Nether Bell Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,293.5,78.18,196.5,-90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_nether_bell_sanctum.yml");
        setDisguise("BOGGED");
    }
}
