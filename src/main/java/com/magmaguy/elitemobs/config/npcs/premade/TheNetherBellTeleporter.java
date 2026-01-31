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
                "<The Nether Bell Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,300.4,78,198.3,180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_nether_bell_sanctum.yml");
        setCustomModel("em_ag_ancientcorpse");
        setSyncMovement(false);
    }
}
