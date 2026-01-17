package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class DarkSpireTeleporter extends NPCsConfigFields {
    public DarkSpireTeleporter() {
        super("dark_spire_teleporter",
                true,
                "Dark Warlock",
                "<Dark Spire Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,289.5,92,302.5,-90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp dark_spire_minidungeon.yml");
    }
}
