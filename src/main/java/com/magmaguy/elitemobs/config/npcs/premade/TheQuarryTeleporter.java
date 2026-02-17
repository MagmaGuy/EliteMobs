package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheQuarryTeleporter extends NPCsConfigFields {
    public TheQuarryTeleporter() {
        super("the_quarry_teleporter",
                true,
                "<g:#7A6A5A:#8A7A6A>Dwarf Berge</g>",
                "<g:#6A5A4A:#7A6A5A><The Quarry></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,303.4,78,201.3,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_quarry_dungeon.yml");
        setCustomModel("em_ag_dwarfberge");
        setSyncMovement(false);
    }
}
