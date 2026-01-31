package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class UnderGroveTeleporter extends NPCsConfigFields {
    public UnderGroveTeleporter() {
        super("under_grove_teleporter",
                true,
                "Grove Keeper",
                "<Under Grove Teleporter>",
                Villager.Profession.FARMER,
                "em_adventurers_guild,293.5,92,309.5,-180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp under_grove_lair.yml");
        setCustomModel("em_ag_grovekeeper");
        setSyncMovement(false);
    }
}
