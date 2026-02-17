package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class ThePalaceTeleporter extends NPCsConfigFields {
    public ThePalaceTeleporter() {
        super("the_palace_teleporter",
                true,
                "<g:#A89040:#B8A050>Royal Guard Sven</g>",
                "<g:#988030:#A89040><The Palace></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,305.4,78,201.3,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_palace_sanctum.yml");
        setCustomModel("em_ag_royalguardsven");
        setSyncMovement(false);
    }
}
