package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class HallosseumTeleporter extends NPCsConfigFields {
    public HallosseumTeleporter() {
        super("hallosseum_teleporter",
                true,
                "Haunted Harbinger",
                "<Hallosseum Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,297.5,92,307.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp hallosseum_lair.yml");
    }
}
