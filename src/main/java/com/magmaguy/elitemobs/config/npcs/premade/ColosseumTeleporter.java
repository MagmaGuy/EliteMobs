package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class ColosseumTeleporter extends NPCsConfigFields {
    public ColosseumTeleporter() {
        super("colosseum_teleporter",
                true,
                "Arena Master",
                "<Colosseum Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,296.5,102,308.5,-160,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_colosseum_lair.yml");
    }
}
