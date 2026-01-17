package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class BeastsSanctuaryTeleporter extends NPCsConfigFields {
    public BeastsSanctuaryTeleporter() {
        super("beasts_sanctuary_teleporter",
                true,
                "Beast Warden",
                "<Beasts Sanctuary Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,289.5,92,297.5,180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp beasts_sanctuary_lair.yml");
    }
}
