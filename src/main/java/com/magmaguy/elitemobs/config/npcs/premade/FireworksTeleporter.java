package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class FireworksTeleporter extends NPCsConfigFields {
    public FireworksTeleporter() {
        super("fireworks_teleporter",
                true,
                "Pyrotechnician Pete",
                "<Fireworks Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,289.5,92,307.5,-90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp fireworks_lair.yml");
    }
}
