package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheNetherWastesTeleporter extends NPCsConfigFields {
    public TheNetherWastesTeleporter() {
        super("the_nether_wastes_teleporter",
                true,
                "Nether Shroom",
                "<The Nether Wastes Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,299.5,78,201.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_nether_wastes_dungeon.yml");
        setCustomModel("em_ag_nethershroom");
        setSyncMovement(false);
    }
}
