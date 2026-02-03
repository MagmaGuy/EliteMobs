package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheMinesTeleporter extends NPCsConfigFields {
    public TheMinesTeleporter() {
        super("the_mines_teleporter",
                true,
                "<g:#5A5A5A:#6A6A6A>Prospector Voultar</g>",
                "<g:#4A4A4A:#5A5A5A><The Mines></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,308.4,78,204.4,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_mines_dungeon.yml");
        setCustomModel("em_ag_prospectorvoultar");
        setSyncMovement(false);
    }
}
