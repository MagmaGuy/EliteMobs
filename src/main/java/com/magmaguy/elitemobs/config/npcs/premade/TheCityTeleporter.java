package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheCityTeleporter extends NPCsConfigFields {
    public TheCityTeleporter() {
        super("the_city_teleporter",
                true,
                "<g:#7A5A4A:#8A6A5A>Dwarf Olav</g>",
                "<g:#6A4A3A:#7A5A4A><The City></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,307.4,78,201.4,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_city_dungeon.yml");
        setCustomModel("em_ag_dwarfolav");
        setSyncMovement(false);
    }
}
