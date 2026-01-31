package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheCityTeleporter extends NPCsConfigFields {
    public TheCityTeleporter() {
        super("the_city_teleporter",
                true,
                "Dwarf Olav",
                "<The City Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,307.4,78,201.4,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_city_sanctum.yml");
        setCustomModel("em_ag_dwarfolav");
        setSyncMovement(false);
    }
}
