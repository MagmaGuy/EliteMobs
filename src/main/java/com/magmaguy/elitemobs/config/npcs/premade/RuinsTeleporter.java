package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class RuinsTeleporter extends NPCsConfigFields {
    public RuinsTeleporter() {
        super("ruins_teleporter",
                true,
                "Ruinwarden",
                "<Ruins Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,302.7,92,292.5,-90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_ruins.yml");
        setCustomModel("em_ag_ruinwarden");
        setSyncMovement(false);
    }
}
